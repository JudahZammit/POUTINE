#!/bin/bash
set -euo pipefail

# Shaded jar, with all Java dependencies inside it (needs network access to Maven Central).
mvn -B clean package

# bin/poutine looks for the jar in ../share/poutine*/ relative to itself.
mkdir -p "$PREFIX/bin" "$PREFIX/share/poutine"
cp target/poutine-*.jar "$PREFIX/share/poutine/"
cp bin/poutine "$PREFIX/bin/poutine"
