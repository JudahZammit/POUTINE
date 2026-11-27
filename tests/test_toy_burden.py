import subprocess

import pytest

from test_golden import SEED, TESTS, read_results

DATA = TESTS / "data" / "toy_burden"
GENES = {"geneA": "1", "geneB": "2"}

REFERENCE_FLAGS = {"reference_variants_counted": [], "reference_variants_ignored": ["--no-reference"]}

EXPECTED_MODIFIED_LEAVES = {
    "reference_variants_counted": {"geneA": 3, "geneB": 0},
    "reference_variants_ignored": {"geneA": 2, "geneB": 0},
}

EXPECTED_MODIFIED_NODES = {
    "reference_variants_counted": {"geneA": 5, "geneB": 0},
    "reference_variants_ignored": {"geneA": 4, "geneB": 0},
}


def command(out_dir, reference_mode):
    return ["poutine", "-u", "-f", DATA / "ancestral" / "ancestral_sequences.fasta",
            "-t", DATA / "ancestral" / "ancestral_tree.newick", "-p", DATA / "toy.phenos",
            "-m", DATA / "toy.map", "-r", "100", "-S", str(SEED), "-T", "1",
            "-d", out_dir, "-o", "poutine.out", "-l", "poutine.log", "-X", "--burden",
            "--anno-file", DATA / "toy.regenie.annot", "--set-list", DATA / "toy.regenie.setlist",
            "--mask-def", DATA / "toy.regenie.mask", *REFERENCE_FLAGS[reference_mode]]


@pytest.fixture(scope="module", params=list(REFERENCE_FLAGS))
def run(request, tmp_path_factory):
    out_dir = tmp_path_factory.mktemp("toy_burden")
    subprocess.run(command(out_dir, request.param), check=True)
    return request.param, read_results(out_dir / "poutine.out")


def modified_count(results, gene, suffix):
    rows = results[results["segsite_ID"] == GENES[gene]]
    if rows.empty:
        return 0
    row = rows.iloc[0]
    return row[f"a1_count{suffix}"] if row["allele1"] == "T" else row[f"a2_count{suffix}"]


@pytest.mark.parametrize("gene", list(GENES))
def test_modified_leaf_count(run, gene):
    reference_mode, results = run
    assert modified_count(results, gene, "_extant_only") == EXPECTED_MODIFIED_LEAVES[reference_mode][gene]


@pytest.mark.parametrize("gene", list(GENES))
def test_modified_node_count(run, gene):
    reference_mode, results = run
    assert modified_count(results, gene, "") == EXPECTED_MODIFIED_NODES[reference_mode][gene]
