# Help output: known display issues

`poutine --help` (and the usage shown for a missing or invalid option) can look garbled in a normal terminal. Two causes are in POUTINE's source and a third is the terminal. **Nothing here is fixed**: on 2026-10-05 we decided to leave it for now and document it. All of it is cosmetic and predates the packaging work (it comes from the original author's code, commit `d0c9170`); the launcher, the jar and the conda environment did not cause it.

Seen on 2026-10-05 in the VS Code integrated terminal on WSL2 (Windows), running `poutine` from a conda environment.

## 1. Fixed help width of 210 columns

- **Where:** `usageHelpWidth = 210` in the `@Command` annotation on `Homoplasy_Counter`, and `cmdline.setUsageHelpLongOptionsMaxWidth(40)` in `main()` (lines 51 and 210 as of 2026-10-05).
- **Effect:** the longest help line is 209 columns. In any terminal narrower than that, the terminal wraps each line mid-text, so descriptions spill under the option names and the table no longer lines up.
- **Applies when piped too.** The width is layout, so wrapping at 210 happens whether or not colour is on.
- **Candidate fix (untested):** `usageHelpAutoWidth = true`, or a fixed width of about 120. A width change alters the help text itself, so the "15 CLI invocations give byte-identical output" check described in [launcher.md](launcher.md) needs a new baseline. Record the output before and after.

## 2. Section headings set only a background colour

- **Where:** the five `@ArgGroup` headings, written as `@|bg(N) %n...%n|@`, with N = 213 (input genotype file), 123 (other input files), 222 (algorithm parameters), 85 (runtime options) and 197 (output files and options). Lines 62, 72, 86, 111 and 124 as of 2026-10-05.
- **Effect:** the text keeps the terminal's default foreground colour. On a dark theme that is light grey on a pale pink, cyan, orange or green background, so the headings show as blank coloured blocks. It would look fine on a light theme, which is probably what the original author used.
- **Only the headings do this.** The console messages elsewhere use `fg(N)` or `red,bold`.
- **Candidate fix (untested):** add a text colour, for example `@|bg(213),black ...|@`. Colour is stripped when output is not a terminal, so piped output would stay byte-identical.

## 3. The terminal may not render coloured or italic text

picocli's default styles are yellow option names (`ESC[33m`) and italic parameter labels (`ESC[3m`). In the VS Code terminal on the affected machine, **any text with a colour or italic style disappeared, or showed only in fragments**. This reproduces without POUTINE:

```
printf '\e[33mYELLOW\e[39m \e[3mitalic\e[23m \e[48;5;123mblock\e[0m plain\n'
```

Only `plain` appeared (the `block` showed as an empty cyan box). `poutine --help | cat` was complete and correct, so POUTINE's text is fine and the problem is the terminal's rendering. The cause was not identified. Untested suggestions: set `terminal.integrated.gpuAcceleration` to `off` in VS Code and open a new terminal, or run the same line in Windows Terminal or plain `wsl.exe`.

**Workaround for users:** pipe the help, which turns colour off: `poutine --help | cat` (or `| less`).

## How to inspect

picocli turns colour off when output is not a terminal, so captured output (tests, CI, scripts) never shows problems 2 and 3.

```
poutine --help | cat                                                 # plain text
POUTINE_JAVA_OPTS="-Dpicocli.ansi=true" poutine --help | cat -v      # force colour, show the escape codes
poutine --help | awk '{ if (length($0)>m) m=length($0) } END { print m }'   # longest line (209)
```
