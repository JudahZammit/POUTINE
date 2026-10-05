# Milestone: pre-feature hardening

Everything that must be in place before the burden test and non-LoF mutation groups are built. The overriding concern is **scientific reproducibility**: no change made here, or in the feature work that follows, may silently change the results POUTINE produces today.

The reasoning behind the test and seeding approach is in [reproducibility.md](reproducibility.md). Per-branch detail lives in that branch's temporary `PLAN.md` (see [feature-branch-workflow.md](feature-branch-workflow.md)).

## Scope

In scope: the seven workstreams below.

Out of scope: the burden test itself, new mutation groups, any change to the statistics, `--vcf`, indels, accessory-genome counting.

## Order of work

There is no existing golden output, so the order is: **validate the current code against published results, update dependencies, revalidate, then pin the validated behavior as the golden output**. Refactoring comes last. Steps 1 and 2 both depend on step 0. The detail is in the `PLAN.md` on `streamline-installation` and the reasoning in [reproducibility.md](reproducibility.md#validation-strategy).

| Step | Workstream | Branch / status |
|---|---|---|
| 0 | Baseline and groundwork | not started |
| 1 | Streamlined, pinned installation | `streamline-installation` (in progress, see its `PLAN.md`) |
| 2 | Golden output test | not started |
| 3 | Random seeds | not started |
| 4 | Linting | not started |
| 5 | Refactoring | not started |
| 6 | CI pipeline | not started |
| 7 | Conda / bioconda packaging | not started |

## Step 0: Baseline and groundwork (no behavior change)

Done so far (2026-10-05): POUTINE runs end to end with Java 21, Python 3.14 and treetime 0.12.1, on a synthetic fixture committed at [tests/data/toy/](../tests/data/toy/README.md). On that fixture a fresh build of `src/` matches the committed classes on the deterministic columns, and treetime 0.12.1 produces no curly-brace filenames. The fixture is a smoke test only, not the golden dataset.

- [ ] Decide the source of truth: build `src/` at the milestone start and compare with the committed `compiled/` classes on the golden dataset, comparing the deterministic columns exactly. Record the outcome. (Toy fixture: match; confirm on the golden dataset.)
- [x] Recover or reimplement the source for `Fasta_Manager` / `Fasta_Record`: `javap -c -p` them, check how `Homoplasy_Counter` uses them, and check for an upstream source. Verify identical behavior against the golden deterministic columns. Done 2026-10-05: reconstructed from bytecode, identical disassembly, matching output on two datasets (see [third-party-and-licensing.md](third-party-and-licensing.md)).
- [ ] Identify the source of `coevolution.jar` (`org.gersteinlab.coevolution`): upstream repo, license, and whether it is available from a public artifact repository.
- [ ] Obtain a dataset with published POUTINE results (candidates: the *M. tuberculosis* discovery and reference sets from the preprint), plus the exact settings used. Record provenance and license in `tests/data/README.md`.
- [ ] Write down the acceptance criteria for "comparable to published" before running.
- [ ] Run the current build on it, compare with the published results, and keep the output as the pre-update reference.

## Step 1: Streamlined, pinned installation

Tracked in detail in the `PLAN.md` on `streamline-installation`.

- [x] Build system with pinned, updatable dependencies instead of committed jars (`pom.xml`, shaded jar; verified 2026-10-05).
- [ ] Update dependencies (picocli, JDK target, treetime decision; commons-math3 stays pinned) one at a time, comparing deterministic columns exactly against the pre-update reference, then revalidate against the published results.
- [x] Pinned JDK, a conda `environment.yml` and a generated Linux-only `conda-lock.yml` (see [environment.md](environment.md)) (Python, treetime, OpenJDK, Maven), with `maven-enforcer-plugin` rules in the `pom.xml`. No Maven Wrapper: the conda environment is the single pinned environment for developers and CI.
- [ ] Resolved treetime version, with the reasons recorded.
- [ ] An installed `poutine` command replaces `poutine.sh` (see [Entrypoint](#entrypoint) below).
- [ ] Committed `compiled/` removed once the build reproduces it.
- [ ] README Installation section updated.

### Entrypoint

Goal: after installation the user types `poutine <options>` from any directory, with no repo checkout, no relative classpath and no manual `PATH` edit. This replaces `poutine.sh`, which only works from the repo root, hard-codes the classpath, and passes `$@` unquoted (so paths containing spaces break).

- [ ] **Runnable jar.** The build produces a single shaded jar with `Main-Class: Homoplasy_Counter` in its manifest, so the program runs as `java -jar poutine.jar`. This removes the four-jar classpath string.
- [ ] **`poutine` launcher** (POSIX `sh`, installed into `bin/`):
  - locates the jar relative to its own real path (resolving symlinks), so it works wherever it is installed or linked;
  - forwards arguments as `"$@"`;
  - honors `JAVA_HOME` and an optional `POUTINE_JAVA_OPTS` (for example `-Xmx`), so users can size the heap without editing the script;
  - checks up front that `java` (minimum version per the JDK decision) and `treetime` are on `PATH`, and prints an actionable message if not. Skip the `treetime` check when `-u` is given, since treetime is then not used.
- [ ] **Version from the build.** `--version` already exists (picocli, `@Command(name = "poutine", ...)`) but the version string is hard-coded as `1.0.0`. Source it from the build (for example the jar manifest) so the launcher, `--version` and the package version cannot drift apart. A reproducibility record should carry this version.
- [ ] **Delivery.** Conda installs `poutine` onto `PATH` automatically (step 7). Outside conda, a documented `make install` / install script (or a release tarball with `bin/poutine` and `share/poutine/poutine.jar`) does the same.
- [ ] **`poutine.sh`:** keep for one release as a thin shim that calls `poutine` and prints a deprecation notice, then delete. Update the README and `CLAUDE.md` to use `poutine`.
- [ ] **Smoke test.** CI runs `poutine --help` and `poutine --version` from a directory other than the repo root.

Rejected alternatives: a GraalVM native binary (treetime still has to be shelled out to, so users still need the Python environment, and the gain is small); a pip-installable Python wrapper (adds a second packaging system for a Java program); a bare `java -jar` instruction in the README (no dependency checks, no `PATH` entry).

## Step 2: Golden output test

- [ ] `tests/data/`: variable-sites FASTA, newick, phenotype file, `.map`, plus a precomputed ancestral FASTA and newick for `-u`.
- [ ] Pin the **deterministic-column** golden output from the build that was validated against the published results and has had its dependencies updated (step 1). Commit it before steps 3 to 5.
- [ ] Harness that runs POUTINE with pinned `-d/-o/-l/-X`, extracts the `.out` file and diffs it.
- [ ] A treetime smoke test, separate from the golden test.
- [ ] Document how to regenerate golden files, and the rule that regenerating them needs a written justification in the commit.

## Step 3: Random seeds

- [ ] `--seed` option and per-replicate stream derivation.
- [ ] Fix the data race on `r_a1` / `r_a2`; audit the other shared state.
- [ ] Make permutation order explicit rather than dependent on `HashMap` iteration.
- [ ] Capture the seeded baseline, and add the `-T 1` vs `-T 4` identity check.
- [ ] Run the statistical-equivalence validation against legacy output and write it up in `docs/`.
- [ ] Log the seed in the session log (and in the `.out` header only if that is safe for downstream parsers).

## Step 4: Linting

- [ ] Compile with `-Xlint:all` and triage the warnings.
- [ ] Static analysis (SpotBugs and/or Error Prone), report-only first, then fail CI only on new findings.
- [ ] Checkstyle configured to accept the deliberate `snake_case` names. Do not auto-format the large file until the golden test exists, then do it as one standalone mechanical commit.
- [ ] Optional: shellcheck for `poutine.sh`, markdown lint for `docs/`.

## Step 5: Refactoring (minimum needed to make the feature safe)

- [ ] Delete dead code first (commented blocks, unused resampling variants, Fisher/phyC branches, R shell-outs, `concurrecy_test`), one reviewable commit per area.
- [ ] Split the class along its real seams: input parsing and validation, tree and homoplasy counting, resampling engine, statistics, output writing, CLI.
- [ ] Introduce the seams the feature needs: a test-statistic abstraction (single-site binomial now, burden later) and a mutation-group abstraction (single site now, arbitrary groups later). Legacy behavior stays the default implementation.
- [ ] Remove static mutable state (for example `RuntimeSettings.num_threads`) where it blocks testing.
- [ ] The golden test passes unchanged after every refactor commit.
- [ ] A few focused unit tests on the extracted pieces, such as homoplasy counting on a tiny hand-built tree.

## Step 6: CI pipeline (GitHub Actions)

- [ ] Jobs: build, lint, unit tests, golden test (`-T 1` and `-T 4`), treetime smoke test.
- [ ] Create the environment from `environment.yml`, with caching, so CI runs what users get.
- [ ] Matrix: Linux only (decision 2026-10-05). Windows is unsupported because treetime does not run there, and macOS is not a target.
- [ ] Keep it fast: small dataset, moderate replicate count.
- [ ] A job that builds the conda package (step 7).

## Step 7: Conda packaging

- [ ] Minimum: `micromamba create -n poutine -f conda-lock.yml` plus a documented local install that puts `poutine` on `PATH`.
- [ ] Target: a bioconda recipe that builds from source, installs jars to `share/poutine`, ships the `poutine` launcher from step 1 in `bin/`, depends on `openjdk` and a pinned `phylo-treetime`, and runs the golden test in the recipe's `test:` section.
- [ ] Prerequisites from step 0: buildable from source, known dependency licenses and sources, and a tagged release to build from. **Licensing is a hard gate:** bioconda requires that licences allow redistribution, and the vendored coevolution classes and `Fasta_*` have no known licence. Track it in [third-party-and-licensing.md](third-party-and-licensing.md).
- [ ] Decide on a Dockerfile (the README says one "will likely feature soon"); lower priority than conda.

## Open questions

- **Published dataset:** which one, and where do its processed inputs come from? The preprint's discovery set (1,330 genomes, PRJNA413593) has public raw reads but apparently no public processed inputs; the 123-genome reference set may be more practical. Can the data, or a subsample, be committed (size, license, privacy)? The golden test may need a smaller derived subset for CI speed.
- **Published settings:** replicates, `--min_hcount`, POUTINE version and treetime version (preprint: 0.7.6) behind the published tables.
- **Treetime version:** keep 0.8.6 (needs an older Python/numpy stack, to be verified) or move to a current release? 0.12.1 already ran cleanly on the toy fixture with no curly-brace filenames; what remains is confirming the results match 0.8.6 on the golden data. Moving changes the baseline, so it needs an explicit, documented decision.
- **Is `compiled/` or `src/` the truth?** Settled in step 0.
- **Licence for `coevolution`, and authorship of `Fasta_Manager` / `Fasta_Record`:** ask the previous maintainer and the Gerstein lab. Source for both is now in `src/` (vendored and reconstructed respectively).
- **Maven vs Gradle:** Maven recommended; confirm there is no preference.
- **`.out` header:** may it record the seed, or must output files stay byte-identical with the seed only in the log?
- **Minimum supported JDK:** 17, if strict floating-point portability is wanted.
