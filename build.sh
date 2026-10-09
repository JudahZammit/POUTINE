#!/bin/bash
# Used by conda-build (meta.yaml), and can also be run by hand inside an activated conda environment that has
# openjdk 21, maven and python:  bash build.sh   (installs into the active environment, $CONDA_PREFIX).
set -euo pipefail

PREFIX="${PREFIX:-$CONDA_PREFIX}"
SP_DIR="${SP_DIR:-$("$PREFIX/bin/python" -c 'import sysconfig; print(sysconfig.get_paths()["purelib"])')}"

# Shaded jar, with all Java dependencies inside it (needs network access to Maven Central).
mvn -B clean package

# poutine_launcher.py looks for the jar at <env>/share/poutine/poutine.jar.
mkdir -p "$PREFIX/share/poutine"
cp target/poutine-*.jar "$PREFIX/share/poutine/poutine.jar"
cp poutine_launcher.py "$SP_DIR/"

# conda-build generates the "poutine" command from the entry point in meta.yaml; a manual run has to create it.
if [ -z "${CONDA_BUILD:-}" ]; then
    mkdir -p "$PREFIX/bin"
    printf '#!/bin/sh\nexec "%s/bin/python" -c "import poutine_launcher; poutine_launcher.main()" "$@"\n' "$PREFIX" > "$PREFIX/bin/poutine"
    chmod +x "$PREFIX/bin/poutine"
fi
