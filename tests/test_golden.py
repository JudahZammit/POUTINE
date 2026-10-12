"""Golden output test: poutine with a fixed seed and one thread must reproduce the committed results.

Runs poutine (the one on PATH) with treetime on each dataset, then compares poutine.out and the two sorted files with
the golden copies in tests/golden/<dataset>/. See tests/golden/README.md for how the golden files were made.
"""
import subprocess
from pathlib import Path

import pandas as pd
import pytest
from pandas.testing import assert_frame_equal

# Relative tolerance for the float columns. They are counts divided by (replicates + 1), so only the last printed digit
# can differ between JDKs; a one-count change is ~1e-3 (toy) or ~1e-4 (mtb), far above this.
RTOL = 1e-9

SEED = 42
TESTS = Path("tests")  # pytest is run from the repo root
DATASETS = {
    "toy": dict(dir="toy", fasta="sites.fa", tree="tree.nwk", phenos="phenos.txt", map="sites.map", replicates=1000),
    "mtb": dict(dir="mtb-reference", fasta="mtb_maf_05.fasta", tree="mtb_maf05.newick", phenos="mtb.phenos",
                map="mtb_maf_05.map", replicates=10000),
}
STRING_COLUMNS = ["segsite_ID", "allele1", "allele2", "obs_homoplasy_counts"]
FLOAT_COLUMNS = ["pointwise_pvalue_a1", "pointwise_pvalue_a2", "obs_binom_pvalue_a1", "obs_binom_pvalue_a2",
                 "familywise_pvalue_a1", "familywise_pvalue_a2"]


@pytest.fixture(scope="module", params=list(DATASETS))
def run(request, tmp_path_factory):
    """Run poutine once on a dataset and return (dataset name, output directory)."""
    name = request.param
    cfg = DATASETS[name]
    data = TESTS / "data" / cfg["dir"]
    out_dir = tmp_path_factory.mktemp(name)
    subprocess.run(
        ["poutine", "-f", data / cfg["fasta"], "-t", data / cfg["tree"], "-p", data / cfg["phenos"],
         "-m", data / cfg["map"], "-r", str(cfg["replicates"]), "-S", str(SEED), "-T", "1",
         "-d", out_dir, "-o", "poutine.out", "-l", "poutine.log", "-X"],
        check=True)  # pytest shows poutine's output if the run fails
    return name, out_dir


def read_results(path):
    return pd.read_csv(path, sep="\t", dtype={column: str for column in STRING_COLUMNS})


def assert_same_results(actual, expected):
    """Counts and strings must match exactly; the derived p-values to within RTOL."""
    assert list(actual.columns) == list(expected.columns)
    exact = [column for column in expected.columns if column not in FLOAT_COLUMNS]
    assert_frame_equal(actual[exact], expected[exact], check_exact=True)
    assert_frame_equal(actual[FLOAT_COLUMNS], expected[FLOAT_COLUMNS], check_exact=False, rtol=RTOL, atol=0)


@pytest.mark.parametrize("filename", ["poutine.out", "poutine.out.sorted_by_a1_maxT", "poutine.out.sorted_by_a2_maxT"])
def test_golden(run, filename):
    name, out_dir = run
    assert_same_results(read_results(out_dir / filename), read_results(TESTS / "golden" / name / filename))
