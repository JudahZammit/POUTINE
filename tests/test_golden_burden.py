"""Golden output test for the burden test: poutine --burden on the MTB dataset must reproduce the committed results.

Same idea as test_golden.py (fixed seed, one thread, compare with the committed copies in tests/golden/mtb_burden/), but
it skips treetime (-u, with the ancestral files committed in the dataset), so it only needs poutine. In these results a
site is a gene: segsite_ID is the gene's line number in the set list and physical_pos is its position there.
"""
import subprocess

import pytest

from test_golden import SEED, TESTS, assert_same_results, read_results

DATA = TESTS / "data" / "mtb-reference"
GOLDEN = TESTS / "golden" / "mtb_burden"
REPLICATES = 10000
BURDEN_FILES = {"--anno-file": "mtb_maf_05.regenie.annot", "--set-list": "mtb_maf_05.regenie.setlist",
                "--mask-def": "mtb_maf_05.regenie.mask"}


def burden_args():
    return [arg for flag, name in BURDEN_FILES.items() for arg in (flag, DATA / name)]


def command(out_dir):
    return ["poutine", "-u", "-f", DATA / "ancestral" / "ancestral_sequences.fasta",
            "-t", DATA / "ancestral" / "ancestral_tree.newick", "-p", DATA / "mtb.phenos",
            "-m", DATA / "mtb_maf_05.map", "-r", str(REPLICATES), "-S", str(SEED), "-T", "1",
            "-d", out_dir, "-o", "poutine.out", "-l", "poutine.log", "-X", "--burden", *burden_args()]


@pytest.fixture(scope="module")
def out_dir(tmp_path_factory):
    """Run poutine once and return the output directory."""
    out_dir = tmp_path_factory.mktemp("mtb_burden")
    subprocess.run(command(out_dir), check=True)  # pytest shows poutine's output if the run fails
    return out_dir


@pytest.mark.parametrize("filename", ["poutine.out", "poutine.out.sorted_by_a1_maxT", "poutine.out.sorted_by_a2_maxT"])
def test_golden_burden(out_dir, filename):
    assert_same_results(read_results(out_dir / filename), read_results(GOLDEN / filename))
