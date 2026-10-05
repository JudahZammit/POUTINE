# Treetime version comparison

Treetime produces the ancestral reconstruction that POUTINE counts homoplasies on, so its version is part of every result's provenance. This records which versions were compared, how, and what was found.

## Versions

| Version | Why it matters | Source |
|---|---|---|
| 0.7.6 | Used by the preprint ("TreeTime version 0.7.6", `--gtr infer`) | PyPI only (not on bioconda; bioconda goes 0.7.5, then 0.8.1) |
| 0.8.6 | The version the README tells users to install | bioconda and PyPI |
| **0.12.1** | **Latest release** (PyPI 2026-04-23, bioconda 0.12.1). Pinned in [conda-lock.yml](../conda-lock.yml). | bioconda and PyPI |

Versions 0.9.x to 0.11.x were not tested. The README says 0.9.0 and later put curly braces in output filenames; that did not happen with 0.12.1 (or with 0.7.6 and 0.8.6).

## Result (2026-10-05)

On both fixtures, [tests/data/toy](../tests/data/toy/README.md) (40 samples, 300 sites) and [tests/data/mtb-reference](../tests/data/mtb-reference/README.md) (124 samples, 2,946 sites), run exactly as POUTINE runs it (`treetime ancestral --aln ... --tree ... --gtr infer`):

- **Ancestral sequences are identical across all three versions.** Zero differing site-states for leaf and internal nodes: 0 of about 12,000 and 11,700 (toy), and 0 of 365,304 and 329,952 (`mtb-reference`). Node names and order match.
- **Trees differ only in rounding.** Same topology and node names. Branch lengths are printed with 6 decimals in 0.7.6 and 0.8.6 and with 7 in 0.12.1, so they differ by at most 5e-7. (0.7.6 and 0.8.6 are identical to each other.)
- **The inferred substitution model differs in the last digit.** About 1e-4 on a few rates (for example 0.8488 vs 0.8487), which comes from numerical or rounding differences. 0.7.6 and 0.8.6 are textually identical.
- **Extra outputs in 0.12.1.** It also writes `auspice_tree.json` and `branch_mutations.txt`. POUTINE does not read them.
- **POUTINE's output is unchanged.** The full pipeline run under each treetime version (treetime put first on `PATH`) gives deterministic output columns (1 to 9 and `obs_binom_pvalue_a1/a2`) identical to the legacy build, on both fixtures.

**Conclusion:** for these datasets the treetime version, from the preprint's 0.7.6 to the latest 0.12.1, does not change POUTINE's results. Pinning the latest release, 0.12.1, is safe. The pin stays provisional until the same check is repeated on the preprint's published dataset (1,330 genomes), which is larger and could expose ties that small data do not.

## How to reproduce

Old versions need an older Python stack. These environments worked (all on conda-forge, 0.8.6 from bioconda):

```
LIBS="python=3.9 numpy=1.23.5 pandas=1.5.3 scipy=1.9.3 biopython=1.79"
micromamba create -n tt086 -c conda-forge -c bioconda --override-channels $LIBS treetime=0.8.6
micromamba create -n tt076 -c conda-forge --override-channels $LIBS pip
micromamba run -n tt076 pip install --no-deps phylo-treetime==0.7.6
micromamba install -n tt076 -c conda-forge --override-channels matplotlib-base=3.7   # 0.7.6 imports matplotlib
```

Run each version on the same inputs, then compare:

```
treetime ancestral --aln tests/data/mtb-reference/mtb_maf_05.fasta --tree tests/data/mtb-reference/mtb_maf05.newick \
    --outdir out-0.8.6 --gtr infer
python tests/tools/compare_ancestral.py out-0.7.6 out-0.8.6 "mtb: 0.7.6 vs 0.8.6"
```

To run all of POUTINE under an old treetime, put that environment's `bin` first on `PATH` (POUTINE calls plain `treetime`) and use the Java from the locked environment:

```
PATH=<root>/envs/tt076/bin:<root>/envs/poutine/bin:$PATH java -jar target/poutine-1.0.0.jar -f ... -t ... -p ... -m ...
```

To confirm which treetime a run used: 0.7.6 and 0.8.6 write 6-decimal branch lengths and no `auspice_tree.json`; 0.12.1 writes 7 decimals and the extra files.

## Caveats

- Two datasets, one of them synthetic. Not a proof for all inputs.
- Only the deterministic output columns were compared (the others are unseeded; see [reproducibility.md](reproducibility.md)).
- The older environments use older numpy, pandas and Python than the lock. The comparison therefore covers treetime's code and its numerical stack together, which is the realistic situation for anyone running the preprint-era toolchain.
