# MTB reference set

A real *Mycobacterium tuberculosis* dataset that shipped with POUTINE's first release. It is small enough to run in seconds, which makes it the leading candidate for the validation and golden-output dataset (see the [milestone](../../../docs/milestone-pre-feature-hardening.md)).

## Where it came from

These four files were recovered, unmodified, from this repository's git history:

- Added by `73bd73a` ("Pre-Alpha release", Peter E Chen, 2020-09-01) under `pre-alpha/reference_set/`.
- Deleted by `8557ac2` (2021-06-24) with the message "Only used during alpha testing". The files are identical in `73bd73a` and in the parent of the deletion.
- Recover with `git show 73bd73a:pre-alpha/reference_set/<file>`.

| File | Git blob (first 12) | SHA-256 (first 16) | Content |
|---|---|---|---|
| `mtb_maf_05.fasta` | `5e5c8f14c458` | `159113d2e4c56030` | 124 samples, 2,946 variable sites, MAF >= 5% (the name suggests this filter) |
| `mtb_maf05.newick` | `96e058c14cef` | `04f6298bb3fcf026` | Phylogeny of the same 124 samples |
| `mtb.phenos` | `b438a110768b` | `bb8769b471a9edec` | 47 cases (1), 77 controls (0) |
| `mtb_maf_05.map` | `186a7f71143a` | `2a1eff4afe1aae6f` | PLINK-style `.map`, physical positions on H37Rv (chromosome field `23`) |

Sample names are consistent across the FASTA, tree and phenotype files (124 in each).

## Caveats (unconfirmed)

- **Relation to the paper.** The [preprint](https://www.biorxiv.org/content/10.1101/2021.06.30.450606v1) analyses a "reference set" of 123 *M. tuberculosis* genomes (phenotype: any drug resistance), sourced from its citation [10]. This set has 124 samples and 47 cases; it is probably the same data (the extra sample may be the reference genome), but that has **not** been confirmed, and no published results for it have been compared yet.
- **Permission to redistribute.** The files were public in the repository's history, and the deletion message suggests tidying, not a data-permission issue. This is also unconfirmed; check with the previous maintainer before any public release or conda package that ships it.
- The data only exist in this form, with no record of how the genotypes, tree or phenotypes were produced.

## Ancestral reconstruction (`ancestral/`)

`ancestral_sequences.fasta` and `ancestral_tree.newick` are **our** output, not original data: treetime (`phylo-treetime` 0.12.1, Python 3.14, `--gtr infer`) run on the files above on 2026-10-05, as POUTINE runs it. They are committed so tests can use `-u` and bypass treetime. They are not what the paper used (the preprint reports TreeTime 0.7.6).

| File | SHA-256 (first 16) |
|---|---|
| `ancestral/ancestral_sequences.fasta` | `fd61eec93fb1d959` |
| `ancestral/ancestral_tree.newick` | `84d3f63ae9a4c2e0` |

## Running

From the repo root, with `treetime` on `PATH` for the second form:

```
# bypass treetime
poutine -u -f tests/data/mtb-reference/ancestral/ancestral_sequences.fasta \
    -t tests/data/mtb-reference/ancestral/ancestral_tree.newick \
    -p tests/data/mtb-reference/mtb.phenos -m tests/data/mtb-reference/mtb_maf_05.map \
    -r 10000 -T 4 -d /some/output/dir -o mtb.out -l mtb.log

# full run including treetime
poutine -f tests/data/mtb-reference/mtb_maf_05.fasta \
    -t tests/data/mtb-reference/mtb_maf05.newick \
    -p tests/data/mtb-reference/mtb.phenos -m tests/data/mtb-reference/mtb_maf_05.map \
    -r 10000 -T 4 -d /some/output/dir
```

## Observed results

First runs, 2026-10-05, current build, unseeded, `-r 10000`. Stochastic columns vary between runs, so only the counts are exact.

- `CLEAN EXIT` in about 6 seconds with 4 threads. All 2,946 sites are biallelic; 1,560 have at least one homoplasy.
- The `.out` file has 1,560 data rows. Columns 1 to 9 and `obs_binom_pvalue_a1/a2` were identical between the full treetime run and the `-u` run, and between the committed classes and a build from `src/`.
- Top minor-allele (a2) hits, family-wise p-value roughly (it varies run to run):

| H37Rv position | Alleles (a1/a2) | a1 / a2 homoplasy counts | Familywise p (a2) |
|---|---|---|---|
| 1473246 | A / G | 0 / 9 | ~0.001 |
| 761155 | C / T | 3 / 12 | ~0.007 |
| 949535 | C / T | 3 / 10 | ~0.007 |
| 685461 | G / C | 3 / 9 | ~0.03 |
| 4247429 | A / G | 3 / 8 | ~0.03 |
| 1847919 | G / C | 3 / 11 | ~0.05 |

From memory (not yet checked against an H37Rv annotation), 1473246, 761155 and 4247429 correspond to well-known resistance loci (*rrs* position 1401, *rpoB* codon 450 and *embB* codon 306). If that holds, it is a useful biological sanity check, but it is not a comparison with published values.
