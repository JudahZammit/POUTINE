# Toy dataset

A small **synthetic** dataset for smoke-testing POUTINE. It is not real data and is not the planned golden dataset (see the [milestone](../../../docs/milestone-pre-feature-hardening.md)); it exists so the program can be exercised end to end without any external files.

## Contents

| File | What it is |
|---|---|
| `tree.nwk` | Random binary tree, 40 leaves (`s0`..`s39`), with branch lengths |
| `sites.fa` | Variable-sites multi-FASTA, 300 biallelic sites |
| `sites.map` | PLINK `.map` file, physical positions 100, 200, ..., 30000 |
| `phenos.txt` | Random 0/1 phenotypes, tab-delimited, no header |
| `ancestral/ancestral_sequences.fasta`, `ancestral/ancestral_tree.newick` | Treetime ancestral reconstruction of the files above, for `-u` runs |
| `gen_toy_dataset.py` | Generator, kept for provenance |

## Provenance

- Simulated with `gen_toy_dataset.py` (`random.seed(1)`), using a high mutation rate on a random tree, so almost every site is homoplasic (298 of 300 have at least one homoplasy). Real data will be far sparser.
- The phenotypes are random, so no true association is expected.
- The `ancestral/` files came from `treetime ancestral ... --gtr infer` with **phylo-treetime 0.12.1** (Python 3.14). They are committed so tests can bypass treetime with `-u`.
- The committed files are the source of truth. Python's `random` stream is not guaranteed identical across Python versions, so do not expect to regenerate them byte for byte on another version.

## Running

From the repo root, with `treetime` on `PATH` (for example `.venv/bin`):

```
# bypass treetime, using the committed ancestral reconstruction
poutine -u -f tests/data/toy/ancestral/ancestral_sequences.fasta \
    -t tests/data/toy/ancestral/ancestral_tree.newick \
    -p tests/data/toy/phenos.txt -m tests/data/toy/sites.map \
    -r 1000 -T 2 -d /some/output/dir

# full run including treetime
poutine -f tests/data/toy/sites.fa -t tests/data/toy/tree.nwk \
    -p tests/data/toy/phenos.txt -m tests/data/toy/sites.map \
    -r 1000 -T 2 -d /some/output/dir
```

Use `-o`, `-l` and `-X` to pin output filenames; by default they contain timestamps.

## What to expect

- `CLEAN EXIT` as the last console line; about 3 seconds with 1000 replicates.
- The `.out` file has a header plus 298 sites.
- Until seeding exists, columns 1 to 9 and the observed binomial p-values (`obs_binom_pvalue_a1/a2`) are identical between runs; `r_*`, pointwise, `r_maxT_*` and familywise columns differ. The stochastic columns are unseeded until milestone step 3.
