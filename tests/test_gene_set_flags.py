"""Command line test for the burden test options: they must be given together, and the files must exist.

Runs poutine (the one on PATH) on the MTB dataset. Only the two runs that succeed get as far as treetime; the rest are
rejected while the command line is parsed.
"""
import subprocess
from pathlib import Path

import pytest

DATA = Path("tests") / "data" / "mtb-reference"  # pytest is run from the repo root
BASE = ["-f", DATA / "mtb_maf_05.fasta", "-t", DATA / "mtb_maf05.newick", "-p", DATA / "mtb.phenos",
        "-m", DATA / "mtb_maf_05.map", "-r", "1000", "-S", "42", "-T", "1", "-X"]
ANNO = ["--anno-file", DATA / "mtb_maf_05.regenie.annot"]
SET_LIST = ["--set-list", DATA / "mtb_maf_05.regenie.setlist"]
MASK = ["--mask-def", DATA / "mtb_maf_05.regenie.mask"]
LOG = "poutine.log"


def poutine(out_dir, *options):
    """Run poutine and return the finished process; a failed run is not an error here."""
    command = ["poutine", *BASE, "-d", out_dir, "-l", LOG, *options]
    return subprocess.run(command, capture_output=True, text=True)


def missing_line(result):
    """The "Missing required argument(s)" line. The usage text after it names every option, so search only this line."""
    return next(line for line in result.stderr.splitlines() if "Missing required" in line)


@pytest.mark.parametrize("extra, logged", [([], "false"), (["--no-reference"], "true")])
def test_options_are_logged(tmp_path, extra, logged):
    result = poutine(tmp_path, *ANNO, *SET_LIST, *MASK, "--burden", *extra)
    assert result.returncode == 0
    assert f"Is --no-reference in use?: {logged}" in (tmp_path / LOG).read_text()


@pytest.mark.parametrize("options, missing", [
    ([*ANNO, *SET_LIST, *MASK], ["--burden"]),
    ([*SET_LIST, "--burden"], ["--anno-file", "--mask-def"]),
    (["--burden"], ["--anno-file", "--set-list", "--mask-def"]),
    (["--no-reference"], ["--anno-file", "--set-list", "--mask-def", "--burden"]),
])
def test_options_must_be_given_together(tmp_path, options, missing):
    result = poutine(tmp_path, *options)
    assert result.returncode != 0
    assert all(name in missing_line(result) for name in missing)


def test_missing_file(tmp_path):
    result = poutine(tmp_path, "--anno-file", tmp_path / "nope", *SET_LIST, *MASK, "--burden")
    assert result.returncode != 0
    assert "File '" + str(tmp_path / "nope") + "' for option '--anno-file' cannot be found" in result.stderr
