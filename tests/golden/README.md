# Golden output

The expected results for [test_golden.py](../test_golden.py). The test runs poutine on the toy and MTB datasets with a fixed seed and one thread, and compares the output files with the copies here.

**Do not change these files without serious consideration.** They are what stops a code change from silently changing POUTINE's results. If the test fails, assume the code is wrong until you can show that the new results are the correct ones.

## Files

Each dataset folder (`toy/`, `mtb/`) holds the three files poutine writes:

- `poutine.out`: one row per homoplasically informative site, in site order.
- `poutine.out.sorted_by_a1_maxT` and `poutine.out.sorted_by_a2_maxT`: the rows for the sites where allele 1 (or allele 2) is used, sorted by `familywise_pvalue_a1` (or `_a2`). They have fewer rows than `poutine.out`, because they leave out the sites where that allele is unused.

## How they were made

- Datasets: `tests/data/toy` (`-r 1000`) and `tests/data/mtb-reference` (`-r 10000`).
- Settings: `-S 42 -T 1`, with a full treetime run (not `-u`). Results reproduce exactly only with one thread.
- Versions: poutine 1.0.2, OpenJDK 21.0.10, treetime 0.12.1, on 2026-10-12. A build of 1.0.3 gave identical results.
- Each dataset was run twice and the output was byte-identical.

## If you think they need to change

1. Find out why the results differ. What change to the code caused this?
2. Change them only when the results are meant to change, for example a deliberate change to the statistics, the random number order, the seed, or the JDK or treetime version.
3. Change only the columns the change affects. Leave every other column as it is, so the diff shows exactly what moved and the rest stays pinned. If a change alters which rows a file has or their order (anything that changes the family-wise p-values reorders the sorted files), that whole file has to be replaced.
4. Say why, in the commit message, and have it reviewed. Changing golden files in the same commit as the code that caused the change is expected, but the justification must be written down.
5. Double check the results on a real dataset and ensure your results are still scientifically reasonable.
