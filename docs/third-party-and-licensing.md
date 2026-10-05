# Third-party code and licensing

What POUTINE contains or depends on that it did not write, what we know about each licence, and what must be resolved **before publishing to bioconda** (or any other redistribution channel).

This is a project note, not legal advice. Confirm anything that matters with the lab or institution that owns the code.

## Why this blocks bioconda

Bioconda's [contributor guidelines](https://bioconda.github.io/contributor/guidelines.html) require that the "license allows redistribution and license is indicated in `meta.yaml`", and recipes declare it with `license` and `license_file`. A package that bundles code with no stated licence cannot satisfy that. POUTINE's own licence is GPL-3 ([LICENSE](../LICENSE)), so anything bundled into it must also have a licence compatible with GPL-3.

When code carries no licence, copyright law normally leaves all rights with its authors and grants no permission to redistribute. The absence of a notice is not permission.

## Inventory

| Component | How it is used | Licence status | Action |
|---|---|---|---|
| POUTINE itself | The program | GPL-3 | None |
| **Coevolution classes** (`org.gersteinlab.coevolution`, 5 files vendored under [src/main/java/org/gersteinlab/coevolution/](../src/main/java/org/gersteinlab/coevolution/NOTICE.md)) | Newick tree parsing and the tree/node data model, central to homoplasy counting | **No licence or copyright notice found anywhere in the distribution.** | **Blocker.** See below. |
| **`Fasta_Manager`, `Fasta_Record`** | FASTA reading | **Probably POUTINE's own code, so GPL-3, but unconfirmed.** The source was never committed; the classes first appear in the POUTINE author's first release (`73bd73a`, 2020-09-01), live in the default package like POUTINE, and their messages match POUTINE's style. No public source found. | Source **reconstructed** into `src/` (see below). Confirm authorship with the previous maintainer. |
| picocli 4.5.1 | CLI parsing | Apache-2.0 (to be confirmed from the artifact when the build is added) | Declare in the recipe |
| commons-math3 3.6.1 | `BinomialTest` | Apache-2.0 (to be confirmed from the artifact when the build is added) | Declare in the recipe |
| treetime | External program, run as a subprocess; **not redistributed** | Not bundled, so it does not affect POUTINE's redistribution. Confirm its licence before the recipe declares it as a run dependency. | Declare as a dependency, do not bundle |

Third-party jars currently committed in `compiled/` (coevolution, commons-math3, picocli) are themselves a form of redistribution. The planned Maven build replaces the last two with Maven Central dependencies.

## Coevolution classes

### What they are

The Gerstein lab's *Coevolution* system (Yip, Patel, Kim, Engelman, McDermott, Gerstein, *Bioinformatics* 24(2):290-292, 2008). The classes POUTINE uses were written by Kevin Yuk-Lap Yip (the file headers name the author and carry change histories, but no licence).

### Provenance (found 2026-10-05)

- The original site, `coevolution.gersteinlab.org`, is retired ("Legacy Gersteinlab Servers"). The Wayback Machine holds the distribution package, including Java source (67 files under `src/`) and an Ant `build.xml`:
  `http://web.archive.org/web/20230528053327id_/http://coevolution.gersteinlab.org/coevolution/dist/coevolution.jar`
  The file is named `.jar` but is the whole webapp bundle; the library jar is at `WEB-INF/lib/coevolution.jar` inside it.
- Bundle SHA-256: `5262850e3fd83c141fe22546c86675e6b368e5b500dfee135f8a627e29b53a3b`.
- All 67 classes in the bundled library jar are byte-identical to POUTINE's committed `compiled/coevolution.jar`, so this is the code POUTINE has been shipping.
- The package contains no `LICENSE`, `COPYING` or copyright notice; a case-insensitive search for "license", "copyright", "GPL" and "permission" found nothing outside generated Javadoc.

### What is vendored

Only the transitive closure POUTINE needs, unmodified, at the original package paths so imports are unchanged:

- `core/data/NewickTree.java`, `NewickTreeNode.java`, `DataFormatException.java`
- `core/io/NewickTreeReader.java`, `NewickTreeTokenizer.java`

They are byte-identical to the archived upstream files (checked by SHA-256). Do not edit them. A fresh build from them (without the jar) produced identical deterministic output on the toy fixture.

### Options

| Option | Resolves the licence? | Cost and risk |
|---|---|---|
| **1. Obtain a licence or written permission** from the rights holders (the Gerstein lab and the author; also ask the previous POUTINE maintainer how the code was obtained) | Yes, if the licence is GPL-3 compatible | Needs people; the answer may take time. **Preferred.** |
| **2. Replace the five classes** with an openly licensed Newick library or an independent implementation | Yes | Must reproduce the exact tree structure: node names, internal-node labels (treetime output), branch lengths, parsing edge cases. Needs the golden test first. An independent implementation must be written from behavior, not by porting the old code line by line. Candidate libraries need their licences checked. |
| **3. Fetch from the Wayback Machine at build time** instead of vendoring | **No.** The built package would still contain the code, and an archive URL is a fragile build source. | Not recommended |
| **4. Publish the classes as a separate package** | **No.** Same redistribution question, moved elsewhere. | Not recommended |
| **5. Keep vendored, do not publish to bioconda** | Not resolved, only deferred. The repository already contains the compiled jar. | Acceptable for development on this branch, not for release |

Recommendation: pursue 1 now (a short email to the Gerstein lab and the previous maintainer), and treat 2 as the fallback if no licence can be obtained.

### What to ask for

- A statement of the licence the *Coevolution* code is released under (or permission to release the five files under GPL-3, or a GPL-compatible licence such as MIT or BSD).
- Confirmation that POUTINE may redistribute them in its repository and in a bioconda package.
- The correct copyright holder and attribution wording.

Once it exists, record the licence text in `LICENSES/` (or alongside the files), reference it from the bioconda `license_file`, and update the table above.

## Fasta_Manager and Fasta_Record

The original source was never committed (only `.class` files, in the first release). The source in `src/main/java/Fasta_Manager.java` and `src/main/java/Fasta_Record.java` was **reconstructed from the bytecode** with `javap -c -p`. Verification:

- Compiled with `javac --release 8 -g` (the original's class version is 52), the disassembly is instruction-for-instruction identical to the committed classes, including exception tables, after normalising constant-pool indices and whitespace. Original local variable names (`EOR`, `currLine`, ...) were recovered from the `LocalVariableTable`.
- A full build from `src/` alone matches the committed build on the deterministic output columns, for the toy fixture and the 124-sample MTB reference set.

The quirks are preserved deliberately (documented in the file headers). They have no effect on well-formed input, but changing them could change behavior on edge cases.

## Checklist before a bioconda submission

- [ ] Licence for the coevolution classes resolved (option 1 or 2), recorded here.
- [x] Source for `Fasta_Manager` / `Fasta_Record` recovered (reconstructed).
- [ ] Authorship of `Fasta_Manager` / `Fasta_Record` confirmed with the previous maintainer, so they can be marked GPL-3 with confidence.
- [ ] No committed third-party binaries remain; dependencies come from Maven Central or conda.
- [ ] Licences for picocli, commons-math3 and treetime confirmed from the actual artifacts.
- [ ] `license` and `license_file` set in the recipe, covering POUTINE's GPL-3 and any bundled component.
- [ ] A tagged release exists to build from (bioconda prefers release tarballs).
