# PLAN: streamline installation

Branch: `streamline-installation`. Temporary file; harvest into `docs/` / `CLAUDE.md` and delete before merging to `master` (see [docs/feature-branch-workflow.md](docs/feature-branch-workflow.md)).

This branch is **step 1** of the [pre-feature hardening milestone](docs/milestone-pre-feature-hardening.md). The milestone doc holds the overall plan and the other steps; the reasoning is in [docs/reproducibility.md](docs/reproducibility.md).

## Goal

A consistent, pinned environment and a build that anyone can reproduce, so dependencies can be updated deliberately instead of by replacing committed jars.

Out of scope: seeds, golden test, linting, refactoring, CI, bioconda recipe (later milestone steps). Nothing on this branch may change POUTINE's output.

## Design

- **Build system:** Maven (recommended; Gradle is an acceptable alternative). `commons-math3` and `picocli` come from Maven Central at pinned versions. `coevolution` and `Fasta_*` depend on the prerequisites below.
- **JDK:** pin to 17 or later (LTS) via `maven.compiler.release`. Current bytecode is Java 14.
- **Python side:** `environment.yml` (conda) pinning Python, `phylo-treetime` and OpenJDK, with a lock file if feasible.
- **Entrypoint:** a `poutine` command replaces `poutine.sh`: shaded runnable jar plus a POSIX `sh` launcher that finds the jar relative to itself, forwards `"$@"`, honors `POUTINE_JAVA_OPTS`, and checks for `java` and `treetime` with clear errors. Full requirements and rejected alternatives are in the milestone's [Entrypoint](docs/milestone-pre-feature-hardening.md#entrypoint) section.

## Prerequisites (from milestone step 0)

These block the build and need answers first.

- [ ] Where does `coevolution.jar` (`org.gersteinlab.coevolution`) come from: upstream repo, license, public artifact?
- [ ] Where is the source for `Fasta_Manager` / `Fasta_Record`? If none exists, decompile or reimplement and check behavior.
- [ ] Do the committed `compiled/` classes or `src/` reflect current behavior? A build from `src/` must not silently differ from what `poutine.sh` runs today.
- [ ] Which treetime version? README pins 0.8.6; the local `.venv` had 0.12.1. Changing it changes ancestral reconstruction and therefore results, so this needs an explicit decision.

## Steps

- [ ] Resolve the prerequisites above.
- [ ] Add `pom.xml` (or Gradle equivalent) and build `src/` into a runnable jar.
- [ ] Confirm the built jar reproduces the committed `compiled/` behavior on a run (compare output columns that do not depend on randomness).
- [ ] Add `environment.yml` and, if feasible, a lock file. Record the treetime decision and why.
- [ ] Produce a shaded runnable jar (`Main-Class: Homoplasy_Counter`).
- [ ] Add the `poutine` launcher in `bin/` and an install route outside conda (install script or release tarball).
- [ ] Source the version for `--version` from the build instead of the hard-coded `1.0.0`.
- [ ] Turn `poutine.sh` into a deprecation shim that calls `poutine`; add a CI/smoke check that `poutine --help` works from a non-repo directory.
- [ ] Remove tracked `compiled/` in its own commit, once the build reproduces it.
- [ ] Update the README Installation section and `CLAUDE.md` build/run commands.
- [ ] Update the milestone doc's step 1 status.

## Open questions

- Maven or Gradle?
- Minimum JDK: 17?
- How should a user get `treetime` if not through conda (pip pin in the README)?

## Notes and gotchas

- The documented recompile command needs `compiled` on the classpath because the `Fasta_*` sources are missing. This goes away once the sources are recovered.

## What's next

Nothing implemented yet. Start with the prerequisites; the `coevolution` and `Fasta_*` answers gate the build.
