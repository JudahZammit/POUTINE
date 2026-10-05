# The `poutine` launcher

`bin/poutine` is the way to run POUTINE. It replaces the old `poutine.sh`, which ran class files committed in `compiled/` through a relative classpath and so only worked from the repo root.

## Use

Inside the conda environment ([environment.md](environment.md)), after building once with `mvn package`:

```
bin/poutine --help
bin/poutine -f sites.fa -t tree.nwk -p phenos.txt -m sites.map -d results/
```

It works from any directory and through symlinks. To get a plain `poutine` command, link it into the environment (or any directory on `PATH`):

```
ln -s "$PWD/bin/poutine" "$CONDA_PREFIX/bin/poutine"     # with the conda environment active
```

The jar is found relative to the real location of the script, so the link can sit anywhere. Rebuilding the jar needs no re-install.

## What it does

1. **Finds the jar**, in this order: `$POUTINE_JAR`; `<script dir>/../share/poutine*/poutine*.jar` (the installed layout, for the conda package); `<script dir>/../target/poutine-*.jar` (a source checkout after `mvn package`). With several matches the last, highest one wins.
2. **Finds Java:** `$JAVA_HOME/bin/java` if it exists (conda sets `JAVA_HOME`), else `java` on `PATH`. `JAVA_HOME` therefore wins over `PATH`. Requires Java 21 or newer, with a clear message otherwise.
3. **Checks `treetime` is on `PATH`**, unless it will not be used: the check is skipped for `-u` / `--use-precomputed-anc-recon` (also inside clustered short options such as `-Du`), and for help and version output (`-h`, `--help`, `-V`, `--version`).
4. **Runs** `java $POUTINE_JAVA_OPTS -jar <jar> "$@"` with `exec`, so POUTINE's exit code is the launcher's exit code and arguments (including paths with spaces) pass through untouched.

Environment variables: `POUTINE_JAR`, `POUTINE_JAVA_OPTS` (extra JVM options such as `-Xmx16g`, split on spaces), `JAVA_HOME`.

Launcher errors exit with code 1 and print `poutine: <message>` to standard error. POUTINE's own usage errors keep picocli's exit code 2.

## Version

`--version` and the first line of every session log print the Maven project version. The `pom.xml` writes it into `poutine.properties` inside the jar by resource filtering, and `Poutine_Version` reads it, so the jar, `--version`, the logs and the package version cannot drift apart. A build outside Maven prints `unknown (not built with Maven)`. The text of `--version` is byte-identical to the old hard-coded output.

## `poutine.sh`

Now only a deprecation shim: it prints a notice to standard error and forwards everything to `bin/poutine`. It no longer runs the committed `compiled/` classes, so a build is needed first. The legacy command, for comparing against the committed classes while they still exist, is:

```
java -cp "compiled:compiled/coevolution.jar:compiled/commons-math3-3.6.1.jar:compiled/picocli-4.5.1.jar" Homoplasy_Counter <options>
```

## Tests

[tests/tools/test_launcher.sh](../tests/tools/test_launcher.sh) (28 checks, shellcheck-clean; needs the conda environment and a built jar):

```
micromamba run -n poutine sh tests/tools/test_launcher.sh
```

It covers jar and Java lookup, symlinks, paths with spaces, `POUTINE_JAVA_OPTS`, `JAVA_HOME`, the treetime and Java-version checks (including the `-u` and help exemptions), exit-code pass-through, and running under `dash` and `bash --posix`. It does not compare scientific output.

Also checked when the launcher was written: a set of 15 CLI invocations (help, version, no arguments, invalid values, unknown and missing options, clustered flags) gives byte-identical output and exit codes before and after this change.

## Gotchas

- **Do not run the test script without clearing `JAVA_HOME` in your own experiments.** conda sets it, and the launcher prefers it over `PATH`, so a fake or restricted `PATH` is ignored. The test script unsets it for that reason.
- **The treetime skip is deliberately loose.** Any short-option cluster containing `u`, `h` or `V` skips the check (so a value attached to `-d`, like `-dhome`, also does). The cost of a false skip is only that POUTINE's own treetime error appears instead of the launcher's.
- **Non-conda installs are secondary.** The supported route is the conda environment. Outside it, set `POUTINE_JAR` and make sure Java 21 and treetime are available.
