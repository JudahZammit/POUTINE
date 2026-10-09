#!/bin/bash
set -euo pipefail

# Shaded jar, with all Java dependencies inside it (needs network access to Maven Central).
mvn -B clean package

# poutine_launcher.py looks for the jar at <env>/share/poutine/poutine.jar.
mkdir -p "$PREFIX/share/poutine"
cp target/poutine-*.jar "$PREFIX/share/poutine/poutine.jar"
cp poutine_launcher.py "$SP_DIR/"
