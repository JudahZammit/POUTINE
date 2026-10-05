# The `poutine` launcher

`bin/poutine` is the way to run POUTINE. It replaces the old `poutine.sh`, which ran class files committed in `compiled/` (now removed) through a relative classpath and so only worked from the repo root. It is deliberately small (12 lines of code); this page explains what each part is for.

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

The jar is found relative to the real location of the script (`readlink -f`), so the link can sit anywhere, and rebuilding the jar needs no re-install.

## What it does

1. **Finds the jar** in `<script dir>/../target/poutine-*.jar` (the output of `mvn package`). With several matches the last, highest one wins. If none exists it says to build it.
2. **Checks Java:** `java` on `PATH` must be version 21 or newer, otherwise it stops with a one-line message instead of Java's `UnsupportedClassVersionError`. (Activating the conda environment puts the right `java` first on `PATH`.)
3. **Runs** `java $POUTINE_JAVA_OPTS -jar <jar> "$@"` with `exec`, so POUTINE's exit code is the launcher's exit code, and arguments, including paths with spaces, pass through untouched.

`POUTINE_JAVA_OPTS` holds extra JVM options such as `-Xmx16g` (split on spaces). Launcher errors exit with code 1 and print `poutine: <message>` to standard error. POUTINE's own usage errors keep picocli's exit code 2.

## What the old script got wrong

The old `poutine.sh` was `java -cp "compiled:..." Homoplasy_Counter $@`. Reproduced failures:

- A path with a space was split in two by the unquoted `$@` (`/data/my samples/phenos.txt` became `/data/my` and `samples/phenos.txt`).
- The relative classpath only worked from the repo root.
- Java older than 21 ended in an `UnsupportedClassVersionError` stack trace.
- A missing treetime ends in a generic "error during ancestral reconstruction" message that does not say treetime is not installed. The launcher does not check for it: treetime is a documented requirement (it comes with the conda environment), and failing without it is acceptable.

## Version

`--version` and the first line of every session log print the Maven project version. The `pom.xml` writes it into `poutine.properties` inside the jar by resource filtering, and `Poutine_Version` reads it, so the jar, `--version`, the logs and the package version cannot drift apart. A build outside Maven prints `unknown (not built with Maven)`. The text of `--version` is byte-identical to the old hard-coded output.

## `poutine.sh`

Kept for backwards compatibility (decision 2026-10-05), but now only a shim: it prints a deprecation notice to standard error and forwards everything to `bin/poutine`. It no longer runs the old committed classes, so a build is needed first.

## Running the legacy build from history

The old `compiled/` directory (class files plus the coevolution, commons-math3 and picocli jars) was removed from the repository. The last commit that contains it is `d0d4a6c`. To run it, for example to compare a result against the legacy build, check that commit out into a separate worktree and use the old command from there:

```
git worktree add ../poutine-legacy d0d4a6c
cd ../poutine-legacy
java -cp "compiled:compiled/coevolution.jar:compiled/commons-math3-3.6.1.jar:compiled/picocli-4.5.1.jar" Homoplasy_Counter <options>
git worktree remove ../poutine-legacy        # when done
```

This was tested: from a worktree of that commit the legacy classes reproduce the legacy baseline on the toy fixture. If this commit may become unreachable (for example after squash-merging and deleting the branch), tag it first, for example `git tag legacy-compiled d0d4a6c`.

## Tests

[tests/tools/test_launcher.sh](../tests/tools/test_launcher.sh) (18 checks, shellcheck-clean; needs Java 21 and a built jar, so run it inside the conda environment):

```
micromamba run -n poutine sh tests/tools/test_launcher.sh
```

It covers jar lookup (including through a symlink and a missing jar), the Java version check, paths with spaces, `POUTINE_JAVA_OPTS`, exit-code pass-through, and running under `dash` and `bash --posix`. It does not compare scientific output.

Also checked when the launcher was written: a set of 15 CLI invocations (help, version, no arguments, invalid values, unknown and missing options, clustered flags) gives byte-identical output and exit codes before and after this change, and both fixtures run through the launcher (via a symlink in the environment, with `-u` and with a full treetime run) match the legacy build on the deterministic output columns.

## Left out on purpose

A first version was larger (49 lines of code) and also had these features; the launcher then lost its treetime check too, leaving 12. They were removed as not needed yet; add them back if a need appears.

- **`JAVA_HOME` support.** The activated conda environment puts the right `java` first on `PATH`, so it was redundant. (It also overrode `PATH`, which made testing with a restricted `PATH` confusing.)
- **`POUTINE_JAR` override and an installed `share/poutine*/` jar location.** Only useful for a packaged install, such as the bioconda recipe, which does not exist yet (see [third-party-and-licensing.md](third-party-and-licensing.md)). When it does, the recipe can install the jar next to the launcher, or the lookup can grow a `share/` path again.
- **A hand-written symlink-resolving loop,** replaced by `readlink -f` since only Linux is supported.
- **Any treetime check.** First a refusal (which needed clustered-option parsing to avoid blocking a valid `-Du` run), then a warning. Removed by decision: treetime is a stated requirement and it is fine for POUTINE to fail without it.
