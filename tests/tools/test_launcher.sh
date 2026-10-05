#!/bin/sh
# Tests for bin/poutine. Run from anywhere, inside the conda environment, after "mvn package":
#
#   micromamba run -n poutine sh tests/tools/test_launcher.sh
#
# Needs java on PATH. Exits non-zero if any check fails.
# This checks the launcher (jar lookup, quoting, Java check, option passing, exit codes) and that a fixture run
# completes; it does NOT compare scientific output (that is the job of the golden-output test).

# ok/bad always succeed, so "A && ok || bad" is a safe if-then-else here.
# shellcheck disable=SC2015

repo=$(cd "$(dirname "$0")/../.." && pwd)
launcher=$repo/bin/poutine
data=$repo/tests/data/toy
pass=0
fail=0

ok() { pass=$((pass + 1)); printf '  PASS  %s\n' "$1"; }
bad() { fail=$((fail + 1)); printf '  FAIL  %s\n' "$1"; }
expect_exit() { # name expected actual
    if [ "$2" = "$3" ]; then ok "$1 (exit $3)"; else bad "$1 (expected exit $2, got $3)"; fi
}

command -v java > /dev/null || { echo "java must be on PATH to run these tests"; exit 2; }
ls "$repo"/target/poutine-*.jar > /dev/null 2>&1 || { echo "build the jar first: mvn package"; exit 2; }

tmp=$(mktemp -d) || exit 2
trap 'rm -rf "$tmp"' EXIT
base_path=/usr/bin:/bin

# stand-ins: a PATH with a fake Java 17, a PATH with no java at all, a directory to hold a symlink to the launcher
mkdir "$tmp/oldjava" "$tmp/nojava" "$tmp/bin"
printf '#!/bin/sh\necho "openjdk version \\"17.0.1\\" 2021-10-19" >&2\n' > "$tmp/oldjava/java"
chmod +x "$tmp/oldjava/java"
for c in dirname readlink sed head; do ln -s "$(command -v $c)" "$tmp/nojava/$c"; done
ln -s "$launcher" "$tmp/bin/poutine"

# fixture arguments (with precomputed ancestral files, so used together with -u and no treetime run)
fixture="-f $data/ancestral/ancestral_sequences.fasta -t $data/ancestral/ancestral_tree.newick -p $data/phenos.txt -m $data/sites.map -r 500 -T 2"

echo "launcher lookup"
out=$(cd / && "$tmp/bin/poutine" --version)
[ "$out" = "
POUTINE 1.0.0" ] && ok "--version through a symlink, run from /" || bad "--version through a symlink: [$out]"
(cd / && "$tmp/bin/poutine" --help | grep -q "Usage: poutine") && ok "--help" || bad "--help"
# installed layout, as in a conda package: bin/poutine next to share/poutine-<version>/poutine.jar
mkdir -p "$tmp/inst/bin" "$tmp/inst/share/poutine-1.0.0-0"
cp "$launcher" "$tmp/inst/bin/poutine"; cp "$repo"/target/poutine-*.jar "$tmp/inst/share/poutine-1.0.0-0/poutine.jar"
"$tmp/inst/bin/poutine" --version > /dev/null 2>&1; expect_exit "installed layout (share/poutine*/poutine.jar)" 0 $?
mkdir "$tmp/nojar" "$tmp/nojar/bin"; cp "$launcher" "$tmp/nojar/bin/poutine"
"$tmp/nojar/bin/poutine" --version > "$tmp/o.txt" 2>&1; expect_exit "no jar found refused" 1 $?
grep -q "mvn package" "$tmp/o.txt" && ok "  message says to run mvn package" || bad "  message: $(cat "$tmp/o.txt")"

echo "running"
# shellcheck disable=SC2086
"$launcher" -u $fixture -d "$tmp/out" -o o.out -l o.log > "$tmp/o.txt" 2>&1; expect_exit "-u fixture run" 0 $?
grep -q "CLEAN EXIT" "$tmp/o.txt" && ok "  ends with CLEAN EXIT" || bad "  no CLEAN EXIT"
[ "$(wc -l < "$tmp/out/o.out")" -eq 299 ] && ok "  299 result lines (298 sites plus header)" || bad "  unexpected result line count"
mkdir -p "$tmp/my data/with spaces"; cp "$data/phenos.txt" "$data/sites.map" "$data"/ancestral/* "$tmp/my data/with spaces/"
s="$tmp/my data/with spaces"
"$launcher" -u -f "$s/ancestral_sequences.fasta" -t "$s/ancestral_tree.newick" -p "$s/phenos.txt" -m "$s/sites.map" -r 500 -T 2 -d "$s/out dir" -o o.out -l o.log > /dev/null 2>&1
expect_exit "every path contains spaces" 0 $?
[ -f "$s/out dir/o.out" ] && ok "  output written under the spaced directory" || bad "  no output file"
POUTINE_JAVA_OPTS="-XshowSettings:vm -Xmx512m" "$launcher" --version 2>&1 | grep -q "Max. Heap Size: 512" && ok "POUTINE_JAVA_OPTS reaches the JVM" || bad "POUTINE_JAVA_OPTS"

echo "Java check"
PATH="$tmp/oldjava:$base_path" "$launcher" --version > "$tmp/o.txt" 2>&1; expect_exit "Java 17 refused" 1 $?
grep -q "Java 21 or newer is required (found 17)" "$tmp/o.txt" && ok "  message names the found version" || bad "  message: $(cat "$tmp/o.txt")"
PATH="$tmp/nojava" "$launcher" --version > "$tmp/o.txt" 2>&1; expect_exit "no java refused" 1 $?
grep -q "java not found" "$tmp/o.txt" && ok "  message says java not found" || bad "  message: $(cat "$tmp/o.txt")"

echo "exit codes pass through"
"$launcher" -f "$data/sites.fa" -t "$data/tree.nwk" -p "$data/phenos.txt" -m "$data/sites.map" -r 0 -d "$tmp/o5" > /dev/null 2>&1; expect_exit "invalid -r" 2 $?
"$launcher" --bogus > /dev/null 2>&1; expect_exit "unknown option" 2 $?

echo "other shells"
for shell in dash "bash --posix"; do
    # shellcheck disable=SC2086
    $shell "$launcher" --version > /dev/null 2>&1; expect_exit "runs under $shell" 0 $?
done

echo
echo "$pass passed, $fail failed"
[ "$fail" -eq 0 ]
