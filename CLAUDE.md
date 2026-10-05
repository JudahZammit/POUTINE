# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

POUTINE is a homoplasy-counting GWAS tool for microbial genomes (GPL-3, alpha). The program is one large Java class, [src/main/java/Homoplasy_Counter.java](src/main/java/Homoplasy_Counter.java) (~6,300 lines, default package), plus two small FASTA helper classes and five vendored Newick classes. Built with Maven.

## Build and run

Use the conda environment, created from the lock file: `micromamba create -n poutine -f conda-lock.yml && micromamba activate poutine` (JDK, Maven, treetime and its whole Python stack, Linux only; no Maven Wrapper). `environment.yml` is the human-edited spec and `conda-lock.yml` is generated from it, so never edit the lock by hand; re-lock and rerun the fixtures after any change. Details and gotchas (small `/tmp`!) are in [docs/environment.md](docs/environment.md). There are no automated tests yet and no linter; the checks are the fixtures in `tests/data/` (below). Only commons-math3 and picocli are external Java dependencies, both from Maven Central via the `pom.xml` (pinned at the versions POUTINE has always shipped with).

- Build: `mvn package` produces `target/poutine-1.0.0.jar`, a single runnable jar (shaded). The `pom.xml` fails the build unless Maven >= 3.9 and JDK >= 21 are used.
- Run the built jar from any directory: `java -jar target/poutine-1.0.0.jar <options>` (`--help` lists them). The planned `poutine` launcher will replace this.
- **`./poutine.sh` is the legacy route**: it runs the `.class` files and jars still committed in `compiled/`, with a relative classpath, so it must be run from the repo root. `compiled/` is going away once the Maven build has fully replaced it; don't add to it and don't overwrite its classes.
- The committed `compiled/` classes and the Maven build agree: members and all 318 string constants are identical, and both produce identical output on both fixtures. Bytecode differs only in compiler-version artifacts (javac 21 vs the original ~14: enum `$values()`, string-concat and switch-map codegen), so `compiled/` is not stale relative to `src/` as far as we can tell.
- One Java version everywhere: Java 21. The conda JDK builds and runs the jar, `maven.compiler.release` is 21 (raised from the committed classes' 14 on 2026-10-05), and the enforcer rejects older JDKs. After changing the target, check the class versions inside the jar (Java 21 = class version 65; see docs/environment.md) and rerun the fixtures.
- `src/main/java/org/gersteinlab/coevolution/` holds five vendored, unmodified third-party files (Newick parsing) with **no known licence**. Don't edit them, and don't publish releases or packages until [docs/third-party-and-licensing.md](docs/third-party-and-licensing.md) is resolved.
- `src/main/java/Fasta_Manager.java` and `src/main/java/Fasta_Record.java` were **reconstructed from the old bytecode** (the original source was never committed) and compile to instruction-for-instruction identical classes at Java 8 target. They keep the original's quirks on purpose (errors call `System.exit`, `getHeader()` drops the first character, a header line over 1000 characters breaks `mark/reset`), so don't "fix" them without a golden test. The committed `compiled/Fasta_*.class` are now redundant.
- External requirement: `treetime` must be on `PATH` (the program shells out to `treetime ancestral ...`); the conda environment provides the latest release, 0.12.1. On both fixtures its ancestral sequences and POUTINE's output are identical to 0.7.6 (the preprint's) and 0.8.6 (the README's); see [docs/treetime-version-comparison.md](docs/treetime-version-comparison.md). The README's `0.8.6` advice and curly-brace warning are obsolete for 0.12.1. Treetime does not work on Windows.
- Fixtures for exercising it are in `tests/data/`: `toy/` (synthetic, 40 samples) and `mtb-reference/` (real, 124 samples, runs in seconds), each with a README and precomputed ancestral files for `-u` runs. Compare the deterministic output columns (1-9 and `obs_binom_pvalue_a1/a2`) between builds; the others are unseeded and vary run to run. Input format: a variable-sites multi-FASTA, a Newick tree, a phenotype file (tab-delimited, no header, `sample<TAB>0|1`) and a physical-positions/PLINK `.map` file. `--vcf` is declared but not implemented.

## Architecture

`call()` ([Homoplasy_Counter.java:230](src/main/java/Homoplasy_Counter.java#L230)) is the whole pipeline, run as picocli `Callable<Integer>`:

1. `more_cmdline_magic()` / `log_cmdline_global_vars()`: resolve and validate output paths (timestamping, `-X` overwrite guard), open log/out/debug writers.
2. **Ancestral reconstruction** (skipped with `-u`): `ancestral_reconstruction()` runs treetime via `ProcessBuilder`, then `nexus_to_newick()` parses treetime's `annotated_tree.nexus` into a newick with labelled internal nodes. With `-u`, the user supplies that ancestral FASTA and newick directly (a previous run writes `ancestral_tree.newick` for this purpose).
3. Build structures: `build_tree` (NewickTree), `build_seg_sites` (node name -> `char[]` of alleles), `get_physical_positions`, `read_phenos`. `check_num_seg_sites` / `check_sample_names` fail fast on mismatches between files.
4. `count_all_homoplasy_events()`: per segregating site, find allele-change events on the tree (MRCA/clade logic, `identify_major_minor_alleles`) and produce a `Homoplasy_Events` per site. Only biallelic sites are counted; tri/quad-allelic and monomorphic sites are tallied separately.
5. `assoc()` -> `assoc_test_stat_binomial_test()`: restricts to sites with `>= --min_hcount` homoplasies, runs permutation resampling, then `output_significance_assessments_binom()` writes the results file and the two `.sorted_by_a{1,2}_maxT` files. Output column definitions are in the README.
6. `end_session()`.

### Resampling (the performance-critical part)

The live path is `resample_all_mutations_binom_test_combined_nulldists_memoization_concurrent()`. It submits `m` (`--replicates`) `Replicate` tasks to a fixed thread pool of `--threads` workers, and a `CountDownLatch` waits for them. Each replicate permutes phenotypes, recounts homoplasies per site, computes binomial p-values for both alleles (a1/a2 pooled into one family-wise null) and records the replicate's minimum p-value (maxT). Binomial p-values are memoized in a shared `ConcurrentHashMap`. The pointwise and familywise (maxT FWER) p-values come from comparing the observed statistics to these nulls.

### Code that is switched off, not deleted

The file keeps many earlier iterations, selected by constants and commented-out calls, not by CLI flags. Check what is actually reached before editing:
- `assoc()` has Fisher's-exact and phyC branches commented out. Only the binomial test runs.
- `R_SPACE_TYPE` is a hard-coded `final` (`ALL_MUTATIONS`). `HOMOPLASIC_MUTATIONS_ONLY` and the older `resample_all_mutations_binom_test*` variants are unused.
- `qvalues_option = false`, and `EXTANT_NODES_ONLY = true`. The README says q-values and all R usage were removed, but the `qvalues_R` / `fishers_exact_R` methods that shell out to `Rscript` remain, with a hard-coded `R_dir` pointing at the author's home directory. Don't assume R is needed or working.
- Large `/* ... */` and `//` blocks of old code (including a block of dead code inside `call()`) are common.

## Documentation

- Anything useful to a human working on this repo (design rationale, algorithm notes, gotchas, how-tos, file formats) goes in `docs/`, written as clean, well-organized Markdown with clear headings. Split topics into separate files and don't dump everything into one. Keep `CLAUDE.md` for concise guidance aimed at Claude, and link to `docs/` instead of duplicating it.
- Active milestone: [docs/milestone-pre-feature-hardening.md](docs/milestone-pre-feature-hardening.md) (reproducibility, golden test, seeds, lint, refactor, CI, conda, all before the burden-test feature). Output must not change; see [docs/reproducibility.md](docs/reproducibility.md) for the known risks (unseeded RNG, racy `r_a1++`/`r_a2++`).
- Feature branches may carry a temporary `PLAN.md`. Before deleting it at merge, harvest any design patterns, gotchas or lasting decisions from it into `docs/` (or `CLAUDE.md` if they're short and apply to every session), then delete it. Full process: [docs/feature-branch-workflow.md](docs/feature-branch-workflow.md).

## Shutdown protocol

Claude can't detect that a session is ending, so run this when the user runs `/shutdown` (see `.claude/commands/shutdown.md`), says to wrap up, shut down or end the session, or when a task is finished and the user signals they're done:

1. Review the session for anything a future session or a human contributor would need that the code and git history don't already show: decisions and their reasons, approaches tried and rejected, gotchas, corrected misunderstandings, new commands or conventions.
2. Route it, following the Documentation rules above:
   - Short rules that apply to every session go in `CLAUDE.md`.
   - Longer explanations and rationale go in a topic file in `docs/`.
   - If a `PLAN.md` exists, update its checklist, open questions and "what's next" so the next session can resume from it.
3. Fix or remove anything in `CLAUDE.md` or `docs/` that this session showed to be wrong or stale. Don't just append.
4. Skip anything derivable from the code, anything only relevant to this conversation, and speculation. Record only what was verified.
5. Summarize what was added and where, and list any uncommitted changes. Don't commit or push unless asked.

## Conventions seen in the code

- snake_case method and variable names, and class names like `Homoplasy_Events`, with inner classes for data holders. This is not standard Java style, so match it.
- CLI options are picocli `@Option` fields grouped into `@ArgGroup` static classes (`InputFiles`, `AlgoParams`, `RuntimeSettings`, `OutputOptions`). Numeric options are validated in setter methods that throw `ParameterException`.
- Console output uses picocli `Ansi.AUTO.string("@|fg(N) ... |@")` markup, and anything notable is mirrored to the session log via `outputOptions.log`.
