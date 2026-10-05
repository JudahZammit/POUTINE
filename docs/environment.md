# Development environment and lock file

How the POUTINE build and run environment is defined, pinned and updated. Supported platform: **Linux (linux-64) only**. Treetime does not run on Windows, and macOS is not a target (decision 2026-10-05).

## Files

| File | Role |
|---|---|
| [environment.yml](../environment.yml) | What we ask for: JDK 21, Maven 3.9.16 and treetime 0.12.1 (provisional), from conda-forge and bioconda. Edit this to change a requirement. |
| [conda-lock.yml](../conda-lock.yml) | What we get: every package in the environment (104 for linux-64) with exact version, build and checksum. **Generated; do not edit by hand.** |
| [pom.xml](../pom.xml) | The Java build. Its `maven-enforcer-plugin` rules reject a Maven older than 3.9 or a JDK older than 17. |

Only three packages are pinned in `environment.yml`; everything else (Python, numpy, pandas, scipy, biopython and so on, all pulled in by treetime) is fixed by the lock file. Treetime drives the ancestral reconstruction POUTINE consumes, so its scientific stack counts as part of the result's provenance.

## Create the environment

From the lock file (reproducible, what CI and everyone should use):

```
micromamba create -n poutine -f conda-lock.yml      # or: conda-lock install -n poutine conda-lock.yml
micromamba activate poutine                         # or: conda activate poutine
mvn package                                         # builds target/poutine-1.0.0.jar
```

From `environment.yml` (re-solves, so versions can drift from the lock; only for trying changes):

```
micromamba create -n poutine -f environment.yml
```

Resolved versions in the committed lock (2026-10-05): Python 3.14.7, numpy 2.5.3, pandas 3.0.6, scipy 1.18.1, biopython 1.88, openjdk 21.0.10, maven 3.9.16, treetime 0.12.1.

## Update the lock

Install the tool once, in its own environment (it is not part of the POUTINE environment):

```
micromamba create -n locktool -c conda-forge conda-lock
```

Then, from the repo root:

```
# after changing environment.yml: re-solve everything
micromamba run -n locktool conda-lock lock --micromamba -f environment.yml --lockfile conda-lock.yml

# bump a single package without touching the rest
micromamba run -n locktool conda-lock lock --micromamba --lockfile conda-lock.yml --update PACKAGE
```

The target platform comes from the `platforms:` key in `environment.yml` (conda and micromamba ignore that key).

**Any change to the lock can change results** (it can change treetime's dependencies). After regenerating it, rebuild and rerun the fixtures in [tests/data/](../tests/data/): the deterministic output columns must be unchanged, and a full (non-`-u`) run must reproduce the committed ancestral reconstruction's downstream results. Record the reason for the change in the commit message.

## Verified (2026-10-05)

Installed from the lock alone into a clean environment, `mvn package` built the jar, and the jar's deterministic output columns matched the legacy committed-class build on both fixtures, with `-u` and with a full treetime run (toy and `mtb-reference`).

## Gotchas

- **Memory and `/tmp`.** Solving and installing needs a lot of temporary space and RAM. On the WSL machine used here, `/tmp` is a 1.9 GB RAM-backed tmpfs on a 3.8 GB machine, and conda runs were killed (exit 137) or failed with an opaque solver error. Point `TMPDIR` and `MAMBA_ROOT_PREFIX` at a disk-backed directory (for example under `~/.cache`) before running micromamba or conda-lock.
- **Treetime pin is provisional.** The README says 0.8.6 and the preprint used 0.7.6; 0.12.1 is in the lock. Changing it means changing `environment.yml` and re-locking; the decision is tracked in the treetime comparison in `PLAN.md`.
- **Without a platform restriction the stack diverges.** When we also locked for macOS, conda-forge and bioconda resolved an older stack there (Python 3.10 to 3.12, numpy 1.26.4, pandas 2.2.2) than on Linux (Python 3.14, numpy 2.5.3, pandas 3.0.6). That is why the lock is deliberately Linux-only: one lock, one stack.
