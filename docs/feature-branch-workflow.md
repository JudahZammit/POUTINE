# Feature Branch Workflow

How we plan, track and wrap up work on a feature branch. The goal is continuity: anyone (or any Claude Code session) picking up a branch should be able to see what is being built, what is done and what is next, without relying on chat history.

## Overview

1. Create a feature branch.
2. Write a `PLAN.md` at the repo root and commit it on the branch.
3. Keep `PLAN.md` up to date as work progresses.
4. Before merging, harvest anything lasting from `PLAN.md` into the permanent docs.
5. Delete `PLAN.md` in a final commit, then merge.

## `PLAN.md`

`PLAN.md` is temporary and branch-scoped. It exists only while the feature is in progress and should never reach `master`.

### Suggested contents

| Section | What to put there |
|---|---|
| Goal | What the feature does and why. Include what is explicitly out of scope. |
| Design | The chosen approach, and the alternatives that were considered and rejected. |
| Steps | A checklist of implementation steps. Tick items off as they are completed. |
| Open questions | Anything unresolved that needs a decision. |
| Notes and gotchas | Surprises, pitfalls and non-obvious constraints discovered along the way. |

### While working

- Commit small, focused steps and update the checklist in the same commit where practical.
- At the end of each session, record what is done and what comes next, so the next session can start with "read `PLAN.md` and continue".

## Before merging: harvest lasting knowledge

`PLAN.md` will be deleted, so first read through it and move anything a future contributor would still want.

| Kind of content | Where it goes |
|---|---|
| Design rationale, algorithm notes, file formats, how-tos, longer gotchas | A topic-specific file in `docs/`, written as clean, organized Markdown |
| Short rules that apply to every working session (commands, conventions, traps) | [CLAUDE.md](../CLAUDE.md) |
| User-facing changes (new CLI options, output columns) | [README.md](../README.md) |

Rules of thumb:

- Prefer one `docs/` file per topic over one large file.
- Do not duplicate content. Write it once, then link to it from the other places.
- Keep the content that explains why a decision was made, since that is the part that is hardest to recover from the code later.

## Finishing up

1. Review the harvested changes.
2. Delete `PLAN.md` in its own commit, for example `Remove PLAN.md after harvesting notes`.
3. Merge the branch.
