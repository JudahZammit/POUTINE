# Reproducibility

Why POUTINE's output is not currently reproducible, and the design that makes it so without changing the science. This supports the [pre-feature hardening milestone](milestone-pre-feature-hardening.md).

Line numbers refer to [src/Homoplasy_Counter.java](../src/Homoplasy_Counter.java) as of the start of the milestone and will drift.

## Risks found in the current code

| # | Risk | Where | Consequence |
|---|---|---|---|
| R1 | **No RNG seed.** Phenotype permutation uses `Collections.shuffle(list)` with the JVM's shared default `Random`. | `permute_phenos`, ~line 4911 (called per replicate at ~3232) | Two runs on identical input give different `r_*`, pointwise and familywise values, so there is nothing to diff a golden file against. |
| R2 | **Data race on result counters.** Replicate threads do `curr_test_stat.r_a1++` / `r_a2++` on shared objects without synchronization. | `calc_binomial_test_stats_memoization_concurrent`, ~lines 4240 and 4274 | Increments can be lost when `--threads > 1`. Seeding alone will not make `r_a1`, `r_a2` or the pointwise p-values reproducible across thread counts. Fixing it is a small, correct-direction behavior change and must be documented as one. |
| R3 | Possible similar races elsewhere. | `r_space_diagnostics.tally_current_replicate(...)`, ~line 3245 | Check whether it is synchronized and whether it feeds any output. |
| R4 | **`HashMap` iteration order** decides which sample gets which shuffled label. | `permute_phenos` iterates `phenos.entrySet()` | Stable for a given JDK and key set, but implementation-defined. Make the order explicit (sorted sample IDs). |
| R5 | **Unpinned treetime.** The README says `phylo-treetime==0.8.6`, but the local `.venv` had 0.12.1 (Python 3.14, numpy 2.5, pandas 3.0). | `ancestral_reconstruction()`, ~line 824 | A different treetime can give different ancestral states and so different homoplasy counts. |
| R6 | **Committed `compiled/` may be stale** relative to `src/` (the last `src/` commit, `dfc16ba`, did not touch `compiled/`). | `compiled/` | Unknown which behavior is "current". |
| R7 | **Output filenames and logs contain timestamps.** | `more_cmdline_magic()` | Tests must pin `-d`, `-o`, `-l`, `-X` and compare only the `.out` file. The `.log` and `.debug` files contain times and must not be diffed. |
| R8 | **Missing source** for `Fasta_Manager` / `Fasta_Record` (class files only). | `compiled/` | Blocks a from-source build, bioconda packaging and any refactor touching FASTA parsing. |
| R9 | Floating-point and library determinism across JDKs and platforms. | commons-math3 `BinomialTest` | JDK 17+ makes floating point strict by default, so pinning JDK 17 or later should make results portable. Verify in CI on Linux and macOS rather than assume. |

## Validation strategy

There is no pre-existing golden output, so the reference is the program's **published results**. The order is:

1. Run the current code (current dependencies) on a dataset with published POUTINE results and check the results are comparable. Keep this run's output as the pre-update reference.
2. Update dependencies one at a time. After each, the deterministic columns (below) must match the pre-update reference exactly.
3. Revalidate against the published results.
4. Pin the validated output as the golden output.

"Comparable to published" is statistical (same top hits, matching homoplasy counts, p-values within Monte Carlo error), so it cannot catch small shifts. That is why the exact deterministic-column comparison is the hard gate, and why the pre-update reference is captured first. The published results came from older software (the preprint used TreeTime 0.7.6), so a mismatch may come from treetime rather than POUTINE; use precomputed ancestral files with `-u` where available to separate the two. The criteria and dataset candidates are in the `PLAN.md` on `streamline-installation`.

## How "output must not change" is enforced

The golden test has two tiers, because results are only partly deterministic today.

- **Deterministic columns, exact.** `segsite_ID`, `physical_pos`, `allele1/2`, `a1/a2_count`, `*_extant_only`, `obs_homoplasy_counts`, `obs_binom_pvalue_a1/a2`. These do not depend on the RNG and must match the pinned golden output byte for byte, forever. The golden output is captured from the build validated against the published results (above), before seeding and refactoring, and is the true guarantee that those later steps did not alter the science.
- **Stochastic columns, seeded.** `r_a1/a2`, `pointwise_*`, `r_maxT_*`, `familywise_*`. Once seeding exists, a baseline is captured with a fixed seed and compared exactly. This guards against later regressions but is **not** identical to any legacy run, since legacy runs were never seeded.
- **Bridge, statistical equivalence.** To show the seeded code samples the same null distribution as the legacy code, compare legacy output (many unseeded runs, or one large-replicate run) with seeded output, and require the legacy values to fall within Monte Carlo error. This is a one-off validation recorded in `docs/`, not a CI test.
- **Treetime is frozen out of the golden test** by running with `-u` on a precomputed ancestral FASTA and newick committed to the repo. Treetime gets its own looser smoke test (it runs and its output parses). This separates "our code changed" from "treetime changed".
- **Thread invariance** is proven by running the seeded golden test with `-T 1` and `-T 4` and requiring identical output. This is the direct test of R2.

## Seeding design

Chosen so results do not depend on thread scheduling.

- New option `--seed <long>`. If omitted, generate one, **log it** and print it, so every run can be replayed.
- Derive an independent stream per replicate from `(seed, replicate_num)` (for example `new Random(mix(seed, replicate_num))` or `SplittableRandom`) and pass it to `Collections.shuffle(list, rnd)`. Each replicate writes only to its own slot of `maxT_nulldist_a1_a2_combined`, so completion order cannot matter.
- Replace the racy `r_a1++` / `r_a2++` with `LongAdder` or `AtomicInteger`, or with per-replicate local tallies merged at the end. Pick the simplest first.
- **Rejected:** one shared seeded `Random`. It is thread-safe, but draws would be consumed in a scheduling-dependent order, so output would still vary with thread count.

## Gotchas for later work

- `maxT_nulldist_a1_a2_combined` is indexed by `replicate_num`. That is what makes per-replicate seeding scheduling-independent; do not change it to an append-style structure.
- The binomial p-value cache key is `trials_cases` only, so it assumes `p_success_a1 == p_success_a2`. That holds today. **It will not hold for burden tests with their own null probabilities** and must be revisited in the feature work.
- Only biallelic sites are counted; tri/quad-allelic and monomorphic sites are tallied separately. Include those counts in golden log checks if they are easy to extract.
