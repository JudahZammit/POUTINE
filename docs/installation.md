# Installing POUTINE from source

POUTINE is supported on **Linux only** (treetime does not run on Windows, and macOS is not a target). The conda package is `noarch: generic`, so nothing stops `conda install` on another platform. Linux-only is documented, not enforced (decision 2026-10-06).

There is no published package yet, so POUTINE is installed from a clone of this repository. Both routes below start from the clone and end with a `poutine` command inside a conda environment.

## Which route?

| Route | Use it when | Environments | Needs |
|---|---|---|---|
| [Light: `build.sh`](#light-route-buildsh-into-a-conda-environment) | You want to use POUTINE. | One. | Java 21, Maven, treetime, network access to Maven Central. |
| [Full: `conda build`](#full-route-conda-build) | You maintain the package, or want to test the package that will be published. | The one you install into, plus any one that has conda-build. | The above, plus conda-build and a machine with several GB of free RAM. |
| [Developing](#developing-jar-in-the-checkout) | You are changing the code. | One. | As Light. |

A user of a published package will need one command and one environment, with no conda-build. The two-environment look of the full route is only the maintainer's build tool (conda-build) being separate from the environment the package is installed into. They can be the same environment.

## Light route: `build.sh` into a conda environment

```
git clone https://github.com/Peter-Two-Point-O/POUTINE.git
cd POUTINE
conda create -n poutine -c conda-forge -c bioconda openjdk=21 maven=3.9.16 treetime=0.12.1
conda activate poutine
PREFIX=$CONDA_PREFIX bash build.sh
poutine --version        # POUTINE 1.0.0
```

What it does:

1. `conda create` makes an environment with the JDK (21), Maven (only for building) and treetime 0.12.1 with its Python dependencies. treetime is only needed when POUTINE runs the ancestral reconstruction, not with `-u`. The treetime package is on bioconda, so that channel is required.
2. `build.sh` runs `mvn -B clean package` (a shaded jar with all Java dependencies inside it), then copies the jar to `$PREFIX/share/poutine/` and the launcher to `$PREFIX/bin/poutine`. conda-build sets `PREFIX` itself; here you set it to the active environment.
3. `bin/poutine` finds the jar in `../share/poutine*/` relative to its own location, so the clone is no longer needed once `build.sh` has finished.

To update, pull and run `PREFIX=$CONDA_PREFIX bash build.sh` again. To uninstall, `conda env remove -n poutine`.

## Full route: `conda build`

This builds the actual conda package from [meta.yaml](../meta.yaml) and [build.sh](../build.sh), the same recipe that a channel build would use.

```
git clone https://github.com/Peter-Two-Point-O/POUTINE.git
cd POUTINE
conda install -n base conda-build                 # or use any environment that has conda-build
conda build . -c conda-forge -c bioconda
conda create -n poutine --use-local -c conda-forge -c bioconda poutine
conda activate poutine
poutine --version
treetime --version                                # 0.12.1
```

- `conda build` solves a private build environment (`openjdk 21.*`, `maven >=3.9`), runs `build.sh` in it, and writes `poutine-<version>-0` for `noarch` into conda-build's output folder (the `local` channel). `--croot <dir>` moves the work and output folders, for example onto scratch space.
- `--use-local` means the same as `-c local`.
- `-c bioconda` is needed at install time so that `treetime 0.12.1` resolves. Without it you get "nothing provides treetime".
- The recipe has no `test:` section, so conda-build never runs `poutine`. The `poutine --version` and `treetime --version` checks above are the test.
- Keep `version` in `meta.yaml` equal to `<version>` in `pom.xml`, because the jar name comes from the pom and `build.sh` copies `poutine-*.jar`.
- Build from a clone with no environment directory inside it. `source: path: .` copies the whole directory, with no `.gitignore` filtering (read from conda-build 26.9.1, `source.py`), so an in-repo environment such as `.conda` (about 1 GB), `.git` and `target/` would all be copied into the build.

**Memory.** On the 5 GB WSL machine, `conda build`/`conda render` with `-c conda-forge -c bioconda` reached 3.6 GB RSS in about 5 minutes while only loading the channel indexes, and was killed (2026-10-05). conda-build 26.9.1 hard-codes `omit_defaults=False`, so the `defaults` channels are loaded too. Run the full route on a machine with several GB free. Whether dropping bioconda from the build (`-c conda-forge` only, which is enough for the build environment) makes it fit is untested.

## Developing: jar in the checkout

For working on the code, skip the install and run the jar straight from the checkout:

```
conda activate poutine                            # an environment as created in the light route
mvn package                                       # builds target/poutine-<version>.jar
bin/poutine --help
ln -s "$PWD/bin/poutine" "$CONDA_PREFIX/bin/poutine"      # optional: a plain "poutine" command
```

The symlink survives rebuilding the jar, so after a code change you only rerun `mvn package`. Do not combine the symlink with the light route in the same environment: see the troubleshooting table. How the launcher works: [launcher.md](launcher.md).
