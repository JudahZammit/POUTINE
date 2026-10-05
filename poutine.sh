#!/bin/sh
# Deprecated: use bin/poutine. This shim only forwards to it, and will be removed.
# (It used to run the class files committed in compiled/, which are going away; build the jar with 'mvn package'.)
echo "poutine.sh is deprecated; use bin/poutine instead (see docs/environment.md)." >&2
exec "$(cd "$(dirname "$0")" && pwd)/bin/poutine" "$@"
