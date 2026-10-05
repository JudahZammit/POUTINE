# PLAN: streamline installation

Branch: `streamline-installation`. Temporary file; harvest into `docs/` / `CLAUDE.md` and delete before merging to `master` (see [docs/feature-branch-workflow.md](docs/feature-branch-workflow.md)).

This branch is **step 1** of the [pre-feature hardening milestone](docs/milestone-pre-feature-hardening.md). The milestone doc holds the overall plan and the other steps; the reasoning is in [docs/reproducibility.md](docs/reproducibility.md).

## Goal

A consistent, pinned environment and a build that anyone can reproduce, so dependencies can be updated deliberately instead of by replacing committed jars.

Out of scope: seeds, golden test, linting, refactoring, CI, bioconda recipe (later milestone steps). Nothing on this branch may change POUTINE's output.

## Design

- **Build system:** Maven (recommended; Gradle is an acceptable alternative), itself pinned via the conda environment rather than a committed `mvnw` wrapper. `commons-math3` and `picocli` come from Maven Central at pinned versions. `coevolution` is vendored from source and `Fasta_*` is reconstructed (see prerequisites), so everything builds from `src/`.
- **JDK:** one version, Java 21: the conda lock's JDK builds and runs the jar, `maven.compiler.release` is 21 (raised from the committed classes' 14 on 2026-10-05), and the enforcer requires JDK 21.
- **Python side:** `environment.yml` (conda) plus a generated `conda-lock.yml` fixing the whole dependency tree; Linux (linux-64) only, by decision on 2026-10-05. See [docs/environment.md](docs/environment.md).
- **Entrypoint:** a `poutine` command replaces `poutine.sh`: shaded runnable jar plus a POSIX `sh` launcher that finds the jar relative to itself, forwards `"$@"`, honors `POUTINE_JAVA_OPTS`, and checks for `java` and `treetime` with clear errors. Full requirements and rejected alternatives are in the milestone's [Entrypoint](docs/milestone-pre-feature-hardening.md#entrypoint) section.

## Validation strategy: published results first, golden output last

There is no existing golden output, so the acceptance criterion is the program's **published results**. The order is deliberate:

1. **Validate the current code against a published dataset.** Run today's build (current dependencies) on a dataset whose POUTINE results are published, and check the results are comparable.
2. **Update all dependencies** (next section).
3. **Rerun the same dataset** and check it is still comparable to the published results, and that nothing moved relative to the pre-update run.
4. **Pin the validated behavior as the golden output** (milestone step 2).

Candidate datasets, from the [preprint](https://www.biorxiv.org/content/10.1101/2021.06.30.450606v1) (read from the v1 text only; check the final publication, if any, for differences):

| Set | Organism / phenotype | Size | Published POUTINE results | Data |
|---|---|---|---|---|
| Discovery set | *M. tuberculosis*, isoniazid resistance | 1,330 genomes | "Three significant SNVs plus three secondary hits", with max(T)-corrected p-values in the paper's Table 1 | Raw reads: GenBank BioProject PRJNA413593 (British Columbia Public Health Laboratory). Processed inputs are not public as far as I can tell. |
| Reference set | *M. tuberculosis*, "any drug resistance" | 123 genomes | to be confirmed | Sourced from the paper's citation [10] |

What "comparable" means, to be fixed before running:

- The same top hits (same sites, same major/minor allele) appear, and the same hits are significant after max(T) correction.
- Homoplasy counts (output columns 5 to 9) match the published values where reported.
- p-values agree within Monte Carlo error. For a resampling p-value `p` from `m` replicates, the standard error is about `sqrt(p(1-p)/m)`. Accept differences within roughly three combined standard errors, using at least as many replicates as the paper. The paper does not state its replicate count (see open questions).

Limits of this check, and why we still add two more:

- It is statistical, so it cannot detect small shifts. The hard check for the dependency updates is therefore the **exact** comparison of the deterministic columns (1 to 9 and `obs_binom_pvalue_a1/a2`) between the pre-update and post-update runs on the same inputs. This costs nothing extra and attributes any difference to the update rather than to the original code.
- **Capture the pre-update output on the published dataset before changing anything**, and commit what is small enough. Without it, a post-update mismatch cannot be split into "never matched the paper" versus "the update broke it".
- The published results came from older software (the paper used **TreeTime 0.7.6**). A mismatch with the paper may come from treetime, not POUTINE. Where the authors can provide the original ancestral reconstruction, run with `-u` to remove treetime from the comparison.

## Dependency updates

Goal: move to current versions where that is safe. The rule is **one dependency per commit, and a dependency that can change results needs a before/after comparison and an explicit decision**. Updates happen after step 1 of the validation strategy above; until the published dataset is in place, the toy fixture's deterministic columns (see [tests/data/toy/README.md](tests/data/toy/README.md)) is the interim check.

Versions below were looked up on 2026-10-05 (Maven Central, PyPI).

| Dependency | Now | Latest found | Can it change results? | Plan |
|---|---|---|---|---|
| JDK | Java 14 bytecode (running on 21 here) | 21 LTS in use | Low. JDK 17+ makes floating point strict by default. | **Done 2026-10-05: one number, Java 21** (`release 21`, build and runtime JDK 21 from the conda lock, enforcer requires 21). The jar's deterministic columns match the legacy build on both fixtures (with `-u` and a full treetime run). An intermediate `release 17` build was also verified, including on a real JDK 17.0.18 runtime, before we chose to use one number. Linux only, so no cross-platform check. |
| picocli | 4.5.1 (2020) | 4.7.7 (2025-04) | No (CLI only). Could change help text or validation messages. | **Keep 4.5.1, pinned (decision 2026-10-05).** Nothing in 4.7.x is needed, and a bump risks changing user-visible text (`--help`, validation messages) for no benefit. Revisit only if a needed feature or a security fix requires it; if so, first record the output of a fixed set of CLI invocations (`--help`, `--version`, bad values, missing options) and diff it before and after. |
| commons-math3 | 3.6.1 (2016) | 3.6.1, **already the last 3.x** | **Yes.** `BinomialTest` produces every p-value. | **Keep, pinned.** The successors are `commons-math4-legacy` (only `4.0-beta1`, 2022) and `commons-statistics-inference` (1.3). Migrating is a separate, optional decision; it would need a bit-for-bit comparison of old vs new binomial p-values over every `(trials, cases, p)` the program can produce. Not part of this branch. |
| `coevolution.jar` | Gerstein lab *Coevolution* (2007-2008; `Created-By: 1.6.0`, Ant; 67 classes) | Not on Maven Central; project site retired, source recovered from the Wayback Machine (see Prerequisites) | **Yes.** `NewickTree` / `NewickTreeNode` define the tree the homoplasy logic walks. | **Cannot be "updated".** Source recovered and verified identical to the shipped jar. POUTINE uses only 5 classes (~1,000 lines), so vendor just those into `src/` once the license question is settled; any rebuild must be checked against the fixture. |
| phylo-treetime | README says 0.8.6; `.venv` has 0.12.1 | 0.12.1 (also 0.9.x to 0.11.x exist) | **Yes.** It produces the ancestral states everything downstream uses. | See "Treetime update" below. |
| Python, numpy, pandas, scipy, biopython | Python 3.14, numpy 2.5, pandas 3.0, scipy 1.18, biopython 1.88 (transitive via treetime) | as installed | Only through treetime | Pin them via the conda lock file rather than choosing individually. |

### Treetime update

The only path where an "update" can alter scientific output, so it gets its own procedure:

1. Create environments for the versions worth comparing: **0.7.6** (used by the paper), **0.8.6** (the README pin) and **0.12.1** (current). Older ones may need an older Python/numpy; find out what.
2. Run each on the toy fixture (`sites.fa` + `tree.nwk`, `--gtr infer`) and diff `ancestral_sequences.fasta` and the labelled tree.
3. If the ancestral sequences are identical, moving to 0.12.1 is safe for this data. If they differ, run POUTINE on both and compare homoplasy counts (columns 5 to 9); record how many sites change.
4. Repeat on the published dataset. The toy data is too small to settle this, and the published dataset is the one that matters.
5. Record the decision and its evidence in `docs/`. The golden test uses `-u`, so it is insulated from treetime either way; the treetime smoke test is what tracks this.

Pinning matters more than the version chosen: whichever version wins goes into `environment.yml` and the lock file, and the README's "use 0.8.6" advice is replaced (the curly-brace bug did not appear with 0.12.1 on the fixture).

## Prerequisites (from milestone step 0)

These block the build and need answers first.

- [ ] Where does `coevolution.jar` (`org.gersteinlab.coevolution`) come from: upstream repo, license, public artifact? **Source found (2026-10-05), license still open.** It is the Gerstein lab's *Coevolution* system (Yip, Patel, Kim, Engelman, McDermott, Gerstein, Bioinformatics 24(2):290-292, 2008; author of the classes: Kevin Yuk-Lap Yip). The original site (`coevolution.gersteinlab.org`) is retired, but the Wayback Machine holds the distribution package, which contains the Java source (67 files under `src/`) plus `build.xml` (Ant). Fetch: `http://web.archive.org/web/20230528053327id_/http://coevolution.gersteinlab.org/coevolution/dist/coevolution.jar` (SHA-256 of the package `5262850e3fd83c141fe22546c86675e6b368e5b500dfee135f8a627e29b53a3b`; the file named `.jar` is really the whole webapp bundle, and the library jar is inside it at `WEB-INF/lib/coevolution.jar`). **Verified:** all 67 classes in that bundled library jar are byte-identical to POUTINE's `compiled/coevolution.jar`, and recompiling just the needed source with `javac --release 8` gives identical deterministic output on the toy fixture. **POUTINE only uses 5 classes** (~1,000 lines): `NewickTree`, `NewickTreeNode`, `NewickTreeReader`, `NewickTreeTokenizer`, `DataFormatException`. **No license text or copyright notice** appears anywhere in the package, so redistribution rights are unknown. **The five files are now vendored unmodified** under `src/main/java/org/gersteinlab/coevolution/` (see its `NOTICE.md`) so development can proceed, and a jar-free build from them gave identical deterministic output on the toy fixture. The licence must be resolved before any bioconda release; the options, what to ask for and the checklist are in [docs/third-party-and-licensing.md](docs/third-party-and-licensing.md). The committed `compiled/coevolution.jar` is still what `poutine.sh` runs until the Maven build replaces it.
- [x] Where is the source for `Fasta_Manager` / `Fasta_Record`? **Searched, not found (2026-10-05):** the source was never committed (`git log --all` shows only the `.class` files, first added by the POUTINE author in `73bd73a`, "Pre-Alpha release", 2020-09-01); no other branch has it; the author's GitHub account has only the POUTINE repo; no public hit for the class names. Authorship evidence: `SourceFile` attributes name `Fasta_Manager.java` / `Fasta_Record.java` (default package, like POUTINE), the bytecode message style (`There is a bug in the code . . .`) matches POUTINE's own `. . .` console style, and the classes were added in the author's first release. So they are most likely the POUTINE author's own code (confirm with them). Next: reconstruct source from the bytecode (`javap -c -p`), only ~10 small members used in one place (`build_seg_sites`, ~line 1252), and verify against the fixtures. **Done:** reconstructed into `src/main/java/Fasta_Manager.java` and `src/main/java/Fasta_Record.java`; at the original's Java 8 target they compile to instruction-for-instruction identical bytecode (checked with `javap -c -p`, constant-pool indices normalised), and a build from `src/` alone (no `compiled/` classes, no `coevolution.jar`) matches the committed build exactly on the deterministic columns for the toy fixture and the 124-sample MTB reference set. Authorship is still to be confirmed with the previous maintainer.
- [ ] Do the committed `compiled/` classes or `src/` reflect current behavior? A build from `src/` must not silently differ from what `poutine.sh` runs today. **Partly answered:** on the toy dataset (`-u`), `src/` compiled fresh gave identical deterministic columns to the committed classes. One small input, so not conclusive; recheck on the real golden dataset.
- [x] Which treetime version? **Decided 2026-10-05: the latest, 0.12.1** (it was already in the lock). Compared with 0.7.6 and 0.8.6 with identical results on both fixtures; see the treetime step below and [docs/treetime-version-comparison.md](docs/treetime-version-comparison.md). The README's "use 0.8.6" advice and its curly-brace warning are obsolete for this version (the README rewrite is a later step). Still to repeat on the published dataset.

## Steps

Ordered to follow the validation strategy: build, validate, update, revalidate, pin.

- [ ] Resolve the prerequisites above.
- [ ] Obtain the published dataset and results (see open questions); record provenance in `tests/data/`.
- [ ] Write down the acceptance criteria (what "comparable" means) *before* running.
- [ ] Run the **current** build on it, compare with the published results, and keep the pre-update output as the reference.
- [x] **Pin Maven through conda, not a wrapper.** Put `maven` in `environment.yml` next to `openjdk` and `phylo-treetime` (conda-forge ships `maven`, latest 3.9.16 on 2026-10-05, which is also what is installed locally under `~/.local/opt`), and add `maven-enforcer-plugin` rules (`requireMavenVersion`, `requireJavaVersion`) to the `pom.xml` so a wrong Maven or JDK fails clearly. Decision 2026-10-05: no `mvnw`; the conda environment is already the single pinned environment for developers and CI, and the bioconda recipe builds with conda's `maven` anyway. Revisit only if contributors who will not use conda start building POUTINE (adding a wrapper later is cheap). **Done 2026-10-05:** `environment.yml` (openjdk 21, maven 3.9.16, treetime 0.12.1 provisional) solves with micromamba in ~90 s (resolved: python 3.14.7, numpy 2.5.3, pandas 3.0.6, scipy 1.18.1, biopython 1.88, openjdk 21.0.10), and the `pom.xml` enforcer rules were checked to fail clearly on a wrong Maven or JDK.
- [x] Move sources into Maven's layout (`src/main/java`) with `git mv`; file contents unchanged (git sees 100% renames, except the relative link in the coevolution `NOTICE.md`). Both fixtures still match the committed build on the deterministic columns.
- [x] Add `pom.xml` (or Gradle equivalent) and build `src/` into a runnable jar, first with the **current** dependency versions (picocli 4.5.1, commons-math3 3.6.1), so the build itself is proven before anything is upgraded. **Done 2026-10-05:** `pom.xml` (groupId `io.github.peter-two-point-o`, kept as a placeholder by decision on 2026-10-05 since it only matters if we ever publish to Maven Central, and it should change if the repo moves to another GitHub owner; artifactId `poutine`, version 1.0.0; release 14; every plugin version pinned; commons-math3 3.6.1 and picocli 4.5.1 unchanged). `mvn package` gives `target/poutine-1.0.0.jar` (2.7 MB, shaded, `Main-Class: Homoplasy_Counter`, `Multi-Release: true` for picocli).
- [x] Dependency updates. **Done 2026-10-05:** JDK target raised to Java 21 everywhere, verified on both fixtures. commons-math3 3.6.1 and picocli 4.5.1 stay pinned by decision (no update available or needed; see the table above). Treetime is handled by its own comparison below. Each update was checked by rebuilding in the locked environment and comparing the deterministic columns exactly against the legacy build.
- [x] Treetime: pin the latest release and compare it with the older versions. **Done 2026-10-05** ([docs/treetime-version-comparison.md](docs/treetime-version-comparison.md)): the latest published version is 0.12.1 (PyPI 2026-04-23, bioconda), which is what the lock already pinned. Ran 0.7.6 (the preprint's), 0.8.6 (the README's) and 0.12.1 on both fixtures with `treetime ancestral --gtr infer`: **identical ancestral sequences** (0 differing site-states of about 700,000), trees equal up to rounding (6 vs 7 decimals), substitution-model rates equal to about 1e-4, and POUTINE's deterministic output columns identical to the legacy build under each version. The pin stays provisional only until the same check is repeated on the 1,330-genome published dataset. Script: `tests/tools/compare_ancestral.py`.
- [ ] Revalidate against the published results after all updates; record the outcome in `docs/`.
- [ ] Hand off to milestone step 2: pin the validated output as the golden output.
- [x] `coevolution` sourced: five classes vendored from the archived upstream source (build from them verified).
- [ ] `coevolution` licence: ask the Gerstein lab and the previous maintainer (see docs/third-party-and-licensing.md); fall back to replacing the classes if no licence can be obtained.
- [x] Make the build compile the vendored sources and drop `coevolution.jar` from the classpath. Done: the Maven build compiles them and nothing from `coevolution.jar` is used.
- [ ] Write `docs/dependencies.md`: what each dependency is for, the pinned version, why it was or was not updated, and how to update it safely.
- [x] Confirm the built jar reproduces the committed `compiled/` behavior on a run (compare output columns that do not depend on randomness). **Done 2026-10-05:** the jar's deterministic columns equal the committed build's on the toy fixture and on `mtb-reference`, both with `-u` and via a full treetime run, run from `/tmp` as well. Bytecode comparison: of 20 `Homoplasy_Counter*` classes, 14 disassemble identically and 6 differ only in compiler-version artifacts (javac 21 vs the original; enum `$values()`, string-concat codegen, the old `$1` switch-map class). Members are identical apart from those synthetics and all 318 string constants match, so `compiled/` is not stale relative to `src/` as far as we can tell. This is strong evidence, not proof of identical logic.
- [x] Add `environment.yml`. Treetime pinned at the latest release, 0.12.1 (see the treetime step).
- [x] Generate and commit the conda lock file. **Done 2026-10-05:** `conda-lock.yml` for linux-64 only (104 packages; Python 3.14.7, numpy 2.5.3, pandas 3.0.6, scipy 1.18.1, biopython 1.88, openjdk 21.0.10, maven 3.9.16, treetime 0.12.1). Verified by installing from the lock alone into a clean environment, building with `mvn package`, and matching the legacy build's deterministic columns on both fixtures, with `-u` and with a full treetime run. Locking for macOS too gave an older numpy/pandas/Python stack there, which is why the lock is Linux-only. How-to and gotchas: [docs/environment.md](docs/environment.md).
- [x] Produce a shaded runnable jar (`Main-Class: Homoplasy_Counter`). Done (see `pom.xml` above).
- [ ] Add the `poutine` launcher in `bin/` and an install route outside conda (install script or release tarball).
- [ ] Source the version for `--version` from the build instead of the hard-coded `1.0.0`.
- [ ] Turn `poutine.sh` into a deprecation shim that calls `poutine`; add a CI/smoke check that `poutine --help` works from a non-repo directory.
- [ ] Remove tracked `compiled/` in its own commit, once the build reproduces it.
- [ ] Update the README Installation section and `CLAUDE.md` build/run commands.
- [ ] Update the milestone doc's step 1 status.

## Open questions

- **Which published dataset?** The 1,330-genome discovery set has public raw reads but, as far as I can tell, no public processed inputs (variable-sites FASTA, newick, phenotypes, `.map`). Rebuilding them means variant calling and tree building, which would not reproduce the authors' inputs exactly. Ask the previous maintainer or the authors for the processed inputs and the exact run settings. The 123-genome reference set is smaller and may be the practical choice if it is available.
- **Found in git history: an MTB "reference set".** The POUTINE author's first commits (`73bd73a`, 2020-09-01) shipped `pre-alpha/reference_set/` with real data, deleted later in `8557ac2` (2021-06-24, message: "Only used during alpha testing", so apparently housekeeping, not a data-permission issue; still confirm). Recover with `git show 73bd73a:pre-alpha/reference_set/<file>`. Files: `mtb_maf_05.fasta` (124 samples, 2,946 biallelic sites, MAF >= 5%), `mtb_maf05.newick`, `mtb.phenos` (47 cases, 77 controls), `mtb_maf_05.map` (physical positions on H37Rv). Sample names agree across all three files. The preprint's reference set is "123 genomes, any drug resistance"; this one has 124 samples, so it is probably the same set (the extra sample may be the reference genome), but that is unconfirmed. **A run on it works** (current build, treetime 0.12.1, `-r 10000 -T 4`): `CLEAN EXIT` in 6 seconds, 1,560 of 2,946 sites homoplasic, and the top max(T) hits sit at H37Rv positions 1473246, 761155 and 4247429. Those look like well-known resistance loci (rrs 1401, rpoB codon 450, embB codon 306; unverified here, check against the H37Rv annotation), which is a good biological sanity check. Its published results, if any, are still needed (supplement of the preprint?). **Now committed at [tests/data/mtb-reference/](tests/data/mtb-reference/README.md)** with its provenance, checksums and caveats (relation to the paper and redistribution permission still unconfirmed). This could serve as the validation and golden dataset, at least for CI, with the 1,330-genome discovery set as the larger check.
- **Cluster search (in progress, by the user):** look for the previous maintainer's run directories. Ideal finds, in order of value:
  - the exact **input files**: variable-sites FASTA, newick, phenotype file, `.map`;
  - the **ancestral reconstruction output** (`ancestral_sequences.fasta`, `ancestral_tree.newick`, `annotated_tree.nexus`, `sequence_evolution_model.txt`), which allows `-u` runs that bypass treetime;
  - the **results files** (`*.out`, `*.out.sorted_by_a1_maxT`, `*.out.sorted_by_a2_maxT`) and the `*.log`, which record the command line, version and settings;
  - a **copy of the code as it was run** (jar/classes or git state) and the treetime version (a conda/pip freeze or module name);
  - the manuscript table source files (the numbers behind Table 1).
  Record paths, file dates and checksums in `tests/data/` provenance notes. Check data-sharing permissions before committing clinical-isolate data or phenotypes to a public repo; a derived, anonymized subset may be needed.
- **What was published, exactly?** Need the full results table(s), the POUTINE version or commit used, `--replicates`, `--min_hcount`, treetime version (preprint says 0.7.6) and the ancestral files if the authors kept them.
- Maven or Gradle?
- ~~Minimum JDK / bytecode target~~ Decided 2026-10-05: one number, Java 21, because the install baseline is conda only (the conda JDK is what runs the jar). Revisit only if non-conda installs on older system JDKs become a requirement.
- Is a commons-math3 to commons-statistics migration ever wanted? (Recommended: no, not in this milestone; `BinomialTest` defines every p-value.)
- Add Dependabot/Renovate once CI exists (milestone step 6), with the golden test as the gate for every update PR?
- How should a user get `treetime` if not through conda (pip pin in the README)?

## Notes and gotchas

- The program now builds from `src/` with only commons-math3 and picocli on the classpath (`-sourcepath src/main/java`); `compiled/Fasta_*.class` and `coevolution.jar` are no longer needed to build. Build command is in `CLAUDE.md`.
- **POUTINE verified running** (2026-10-05, Java 21, Python 3.14, treetime 0.12.1): `--help`, a full run with treetime, and a `-u` run all end in `CLEAN EXIT` on the toy dataset. No setup beyond `treetime` on `PATH` was needed.
- `tests/data/toy/` holds a synthetic fixture with a precomputed ancestral reconstruction; use it as the smoke test for every step on this branch (see its [README](tests/data/toy/README.md)). The recipe: run with `-u` and pinned `-d/-o/-l`, then compare columns 1 to 9 and `obs_binom_pvalue_a1/a2` against a previous run.
- Until seeding exists (milestone step 3), the stochastic columns differ on every run, so only the deterministic columns can be compared.

## What's next

`environment.yml`, `conda-lock.yml`, `pom.xml` and the shaded jar are in place and verified against both fixtures, and the dependency updates are finished (Java 21 everywhere; commons-math3 and picocli deliberately pinned). Next: the treetime comparison (0.7.6 vs 0.8.6 vs 0.12.1), the `poutine` launcher, the README Installation rewrite (Linux only, conda install; drop the macOS and Windows claims), and finally removing `compiled/`. The MTB reference set (see the open questions) is the likely validation dataset; its published results and redistribution permission still need confirming.
