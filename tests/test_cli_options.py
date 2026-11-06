"""Command line test: what poutine does with its options and with combinations of them.

Pins the behaviour of the parameter checks, so that they can move between classes without changing what the user sees.
Runs poutine (the one on PATH) on the toy dataset, but only as far as the command line: a run is stopped as soon as it
prints START, which is after the options are parsed and checked and the output files are created. Nothing is computed,
so no run needs treetime or takes more than a second or two.

- every option error alone: exit code 2 and the error message, before the run starts
- pairs of option errors: exit code 2 and one of the two messages. Which of them comes first is not pinned.
- the output options, none, each alone and all together: either the directory error or the same files and the same
  logged options
- values at the edge of what is allowed
"""
import contextlib
import itertools
import os
import re
import signal
import subprocess
import threading
from collections import namedtuple
from pathlib import Path

import pytest

TESTS = Path("tests").resolve()  # pytest is run from the repo root
TOY = TESTS / "data" / "toy"
MTB = TESTS / "data" / "mtb-reference"
NOPE = TESTS / "no_such_file"
STAMP = re.compile(r"\d{4}-\d{2}-\d{2}_\d{2}h-\d{2}m-\d{2}s")
SORTED = [".sorted_by_a1_maxT", ".sorted_by_a2_maxT"]
START = "Starting poutine session"  # printed once the options are parsed and checked, before anything is computed
Run = namedtuple("Run", "returncode output started")
TMP = object()  # in a change: the test's tmp_path (an output directory that already exists)
DROP = object()  # in a change: remove the option
BURDEN = {"--anno-file": MTB / "mtb_maf_05.regenie.annot", "--set-list": MTB / "mtb_maf_05.regenie.setlist",
          "--mask-def": MTB / "mtb_maf_05.regenie.mask", "--burden": None}


def base_options(out_dir):
    """The options of a run that succeeds. A value of None is a flag."""
    return {"-u": None, "-f": TOY / "ancestral" / "ancestral_sequences.fasta",
            "-t": TOY / "ancestral" / "ancestral_tree.newick", "-p": TOY / "phenos.txt", "-m": TOY / "sites.map",
            "-r": 1000, "-S": 42, "-T": 1, "-d": out_dir}


def changed(options, change, tmp_path):
    """The options with the change applied: new values replace old ones, DROP removes an option."""
    options = {**options, **{key: tmp_path if value is TMP else value for key, value in change.items()}}
    return {key: value for key, value in options.items() if value is not DROP}


def kill_group(process):
    """Kill poutine and the java that its launcher started."""
    with contextlib.suppress(ProcessLookupError):
        os.killpg(process.pid, signal.SIGKILL)


def poutine(options, cwd):
    """Run poutine with the options until it prints START, then stop it. A failed run is not an error here."""
    args = [[key] if value is None else [key, str(value)] for key, value in options.items()]
    command = ["poutine", *itertools.chain.from_iterable(args)]
    process = subprocess.Popen(command, cwd=cwd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True,
                               start_new_session=True)
    watchdog = threading.Timer(60, kill_group, [process])  # in case a run hangs
    watchdog.start()
    lines = []
    for line in process.stdout:
        lines.append(line)
        if START in line:
            kill_group(process)
    watchdog.cancel()
    output = "".join(lines)
    return Run(process.wait(), output, START in output)


def first_error_line(run):
    return next(line for line in run.output.splitlines() if line.strip())


def assert_rejected(run, messages):
    """Exit code 2, one of the messages in the first line of the error, and the run never started."""
    assert run.returncode == 2, run.output
    assert any(message in first_error_line(run) for message in messages), run.output
    assert not run.started


# Each fault: name, group (faults in one group are not combined), change to the base options, message.
FAULTS = [
    ("replicates=0", "replicates", {"-r": 0}, "Invalid value '0' for option '--replicates'"),
    ("replicates=abc", "replicates", {"-r": "abc"}, "'abc' is not an int"),
    ("min_hcount=-1", "min_hcount", {"-c": -1}, "Invalid value '-1' for option '--min_hcount"),
    ("rng-seed=-1", "rng-seed", {"-S": -1}, "Invalid value '-1' for option '--rng-seed'"),
    ("threads=0", "threads", {"-T": 0}, "Invalid value '0' for option '--threads'"),
    ("fasta missing", "genotype", {"-f": NOPE}, "for option '--fasta' cannot be found"),
    ("vcf missing", "genotype", {"-f": DROP, "-v": NOPE}, "for option '--vcf' cannot be found"),
    ("no genotype file", "genotype", {"-f": DROP}, "Missing required argument(s)"),
    ("vcf and fasta", "genotype", {"-v": NOPE}, "expected only one match"),
    ("phenos missing", "phenos", {"-p": NOPE}, "for option '--phenos' cannot be found"),
    ("phenos not given", "phenos", {"-p": DROP}, "Missing required argument(s)"),
    ("tree missing", "tree", {"-t": NOPE}, "for option '--tree' cannot be found"),
    ("tree not given", "tree", {"-t": DROP}, "Missing required argument(s)"),
    ("map missing", "map", {"-m": NOPE}, "for option '--map' cannot be found"),
    ("map not given", "map", {"-m": DROP}, "Missing required argument(s)"),
    ("anno-file missing", "burden", {**BURDEN, "--anno-file": NOPE}, "for option '--anno-file' cannot be found"),
    ("set-list missing", "burden", {**BURDEN, "--set-list": NOPE}, "for option '--set-list' cannot be found"),
    ("mask-def missing", "burden", {**BURDEN, "--mask-def": NOPE}, "for option '--mask-def' cannot be found"),
    ("burden alone", "burden", {"--burden": None}, "Missing required argument(s)"),
    ("no-reference alone", "burden", {"--no-reference": None}, "Missing required argument(s)"),
    ("out-dir exists", "out-dir", {"-d": TMP}, "already exists.  Use --force-overwrite option to use existing"),
    ("log is out", "out-names", {"-o": "x", "-l": "x"}, "already exists.  Use --force-overwrite option to overwrite"),
    ("log is sorted out", "out-names", {"-o": "x", "-l": "x" + SORTED[0]}, "already exists.  Use --force-overwrite"),
    ("out name empty", "out-names", {"-o": ""}, "already exists.  Use --force-overwrite option to overwrite"),
    ("log name empty", "out-names", {"-l": ""}, "already exists.  Use --force-overwrite option to overwrite"),
    ("anc-recon-out-dir empty", "out-names", {"-u": DROP, "-a": ""}, "already exists.  Use --force-overwrite"),
]
PAIRED = ["replicates=0", "fasta missing", "phenos not given", "anno-file missing", "out-dir exists", "log is out"]
PAIRS = [(a, b) for a, b in itertools.combinations([fault for fault in FAULTS if fault[0] in PAIRED], 2)
         if a[1] != b[1]]


@pytest.mark.parametrize("name, group, change, message", FAULTS, ids=[fault[0] for fault in FAULTS])
def test_one_error(tmp_path, name, group, change, message):
    result = poutine(changed(base_options(tmp_path / "out"), change, tmp_path), tmp_path)
    assert_rejected(result, [message])


@pytest.mark.parametrize("first, second", PAIRS, ids=[f"{a[0]} + {b[0]}" for a, b in PAIRS])
def test_two_errors(tmp_path, first, second):
    options = changed(changed(base_options(tmp_path / "out"), first[2], tmp_path), second[2], tmp_path)
    assert_rejected(poutine(options, tmp_path), [first[3], second[3]])


def files_by_name(directory):
    """The files in the directory by name, with timestamps in the name shown as <T>."""
    return {STAMP.sub("<T>", file.name): file for file in directory.iterdir()}


def expected_names(given):
    """The names of the files that a run with these output options (-s -X -D -o -l) makes."""
    suffix = ".<T>" if "-s" in given else ""
    out = "res.out" + suffix if "-o" in given else "poutine_session_<T>.out"
    log = "res.log" + suffix if "-l" in given else "poutine_session_<T>.log"
    debug = ["poutine_session_<T>.debug"] if "-D" in given else []
    return {out, *[out + end for end in SORTED], log, *debug}


def expected_log_lines(given):
    """The lines that the log of a run with these output options (-s -X -D) has to report all the options."""
    flags = {"Is --timestamp option in use?": "-s", "Is --force-overwrite option in use?": "-X", "DEBUG MODE": "-D"}
    lines = {f"{text}: {str(flag in given).lower()}" for text, flag in flags.items()}
    return lines | {"# replicates: 1000", "rng seed: 42", "# threads used: 1", "burden testing: not in use",
                    "min # homoplasic mutations required at each segregating site: 1",
                    "Is --use-precomputed-anc-recon in use?: true"}


def find_output_dir(tmp_path, dir_state, given):
    """The directory poutine wrote to: the default one, or -d (with the timestamp added if -s)."""
    pattern = "poutine_session_*" if dir_state == "default" else "out.*" if "-s" in given else "out"
    (found,) = tmp_path.glob(pattern)
    return found


OUTPUT_OPTIONS = {"-s": None, "-X": None, "-D": None, "-o": "res.out", "-l": "res.log"}
COMBINATIONS = [(), *[(option,) for option in OUTPUT_OPTIONS], tuple(OUTPUT_OPTIONS)]  # none, each alone, all


@pytest.mark.parametrize("dir_state", ["default", "new", "existing"])
@pytest.mark.parametrize("given", COMBINATIONS, ids=[" ".join(subset) or "none" for subset in COMBINATIONS])
def test_output_options(tmp_path, dir_state, given):
    """An existing -d is only an error without -s and -X; otherwise the files and the logged options are as expected."""
    options = base_options(tmp_path / "out")
    if dir_state == "default":
        del options["-d"]  # poutine makes its own directory in the cwd
    if dir_state == "existing":
        (tmp_path / "out").mkdir()
    run = poutine({**options, **{key: OUTPUT_OPTIONS[key] for key in given}}, tmp_path)
    if dir_state == "existing" and not {"-s", "-X"} & set(given):
        return assert_rejected(run, ["Directory '"])
    assert run.started, run.output
    files = files_by_name(find_output_dir(tmp_path, dir_state, given))
    assert set(files) == expected_names(given)
    log = next(file for name, file in files.items() if ".log" in name)
    assert expected_log_lines(given) <= set(log.read_text().splitlines())


@pytest.mark.parametrize("extra", [[], ["--no-reference"]], ids=["burden", "burden --no-reference"])
def test_burden_options_are_logged(tmp_path, extra):
    run = poutine({**base_options(tmp_path / "out"), **BURDEN, **dict.fromkeys(extra)}, tmp_path)
    assert run.started, run.output
    log = next((tmp_path / "out").glob("*.log")).read_text()
    assert f"Is --no-reference in use?: {str(bool(extra)).lower()}" in log


@pytest.mark.parametrize("option, value, logged", [
    ("-r", 1, "# replicates: 1"),
    ("-c", 0, "min # homoplasic mutations required at each segregating site: 0"),
    ("-S", 0, "rng seed: 0"),
    ("-T", 2, "# threads used: 2"),
], ids=["replicates=1", "min_hcount=0", "rng-seed=0", "threads=2"])
def test_values_at_the_edge(tmp_path, option, value, logged):
    run = poutine({**base_options(tmp_path / "out"), option: value}, tmp_path)
    assert run.started, run.output
    assert logged in next((tmp_path / "out").glob("*.log")).read_text().splitlines()


@pytest.mark.parametrize("flag, text", [("-h", "Usage: poutine"), ("-V", "POUTINE")])
def test_help_and_version(tmp_path, flag, text):
    run = poutine({flag: None}, tmp_path)
    assert run.returncode == 0
    assert text in run.output
