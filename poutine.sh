#!/bin/sh
# Deprecated: use bin/poutine. This shim only forwards to it, and will be removed.
# Kept for backwards compatibility. It used to run class files committed in compiled/ (removed from the repository;
# build the jar with 'mvn package' instead).
echo "poutine.sh is deprecated; use bin/poutine instead (see docs/environment.md)." >&2
exec "$(cd "$(dirname "$0")" && pwd)/bin/poutine" "$@"
