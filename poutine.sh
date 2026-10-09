#!/bin/sh
# Deprecated: use the "poutine" command (conda install). This shim only forwards to it, and will be removed.
echo "poutine.sh is deprecated; use the 'poutine' command instead." >&2
exec poutine "$@"
