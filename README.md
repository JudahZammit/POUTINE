![Image of new cli](https://github.com/Peter-Two-Point-O/POUTINE/blob/a8a990851e418a1fcea19e55955803692d7f6516/images/POUTINE%20logo%202.png)

# POUTINE

POUTINE is a homoplasy counting-based method for genome-wide association studies (GWAS) for microbial genomes.  It is particularly well suited to identify causal variants in strongly clonal populations which exhibit strong and long-range linkage disequilibrium (LD).  In addition to clonal pops, we recommend using homoplasy counting also for populations higher up the recombination scale (including even highly recombining pops) for three major reasons:
1) Candidate causal variants identified by POUTINE are likely sculpted by convergent evolution and provide orthogonal evidence in conjunction with traditional allele counting methods that the site is causal.   
2) Based upon dense recombination maps, it is likely that many microbial populations will harbor regions of the genome that show insufficient recombination for traditional allele counting methods to resolve the causal signal from linked sites.  The number and size of these strongly clonal regions may be substantial and would benefit from homoplasy counting.
3) In most scenarios, POUTINE provides increased statistical power over commonly used regression-based allele counting approaches.  See our manuscript for an example comparing POUTINE and PLINK hits in an M. tuberculosis pop of ~1300 strains.

For a deeper understanding of POUTINE and its use for microbial GWAS, please refer to our [manuscript](https://github.com/Peter-Two-Point-O/POUTINE#citation).  

Stay tuned as POUTINE will continue to evolve to include [new major features](https://github.com/Peter-Two-Point-O/POUTINE#upcoming-major-featurescode-changes) and enhancements.  POUTINE is designed to be user friendly, and we welcome suggestions for improvements in our [Discussion section](https://github.com/Peter-Two-Point-O/POUTINE/discussions).  Collaborations are welcome!

## Table Of Contents

*   [Updates](https://github.com/Peter-Two-Point-O/POUTINE#updates)
*   [Installation](https://github.com/Peter-Two-Point-O/POUTINE#installation)
*   [Executing The Program](https://github.com/Peter-Two-Point-O/POUTINE#executing-the-program)
*   [Understanding The Output Files](https://github.com/Peter-Two-Point-O/POUTINE#understanding-the-output-files)
*   [Citation](https://github.com/Peter-Two-Point-O/POUTINE#citation)
*   [License](https://github.com/Peter-Two-Point-O/POUTINE#license)
*   [Upcoming Major Features/Code Changes](https://github.com/Peter-Two-Point-O/POUTINE#upcoming-major-featurescode-changes)
*   [Notes From Alpha Development Of POUTINE](https://github.com/Peter-Two-Point-O/POUTINE#notes-from-alpha-development-of-poutine)

## Updates

### 2026 October 5
Installation has been reworked so that everything POUTINE needs comes from one pinned conda environment (see [Installation](#installation)).  The main changes:
*   POUTINE is now built with Maven and run with the new `bin/poutine` launcher, which replaces `poutine.sh` (still present, but only as a deprecated shim that forwards to the launcher).
*   Java 21 is now required.  The conda environment provides it.
*   POUTINE is supported on **Linux only**.
*   The treetime guidance in the 2022 entries below (use 0.8.6, curly braces) is superseded: the environment pins treetime 0.12.1, the latest release, and the curly-brace problem did not occur with it.  On our two test datasets treetime 0.7.6, 0.8.6 and 0.12.1 give identical ancestral sequences and identical POUTINE results (details in [docs/treetime-version-comparison.md](docs/treetime-version-comparison.md)).

### 2022 November 30
Updated the README to include our recommendation to use homoplasy counting for intermediate and highly recombining pops (and not just clonal pops).  We got this question a lot, so we hope this guidance helps.    

### 2022 August 3  
Two announcements for today.
First, the Neher lab has informed us that treetime's curly brace bug will likely be fixed in their next release.  Until then, please continue to use either of the following two workarounds:
1.  Manually remove the curly braces from treetime's two output files (annotated_tree{}.nexus and ancestral_sequences{}.fasta), and run POUTINE again with the --use-precomputed-anc-recon option using the newly renamed files.
2.  Install a recent version of treetime that does not have this curly brace bug (version 0.8.6).  One can do this using pip with the following line:
`pip install phylo-treetime==0.8.6`

*(Superseded: see the 2026 October 5 update.  The two workarounds above are no longer needed.)*

Second, the Neher lab has also informed us that treetime does not currently work on Windows.  A future release of treetime may support Windows, so stay tuned.  Until such time, POUTINE will not run on Windows.
  
### 2022 June 14
There is a known issue with the latest version of Treetime (0.9.0 and above) where it adds curly braces to its output filenames for ancestral reconstruction.  A patch will be coming soon!  In the meantime, one can either manually remove the curly braces from the Treetime output files and then run POUTINE using the "--use-precomputed-anc-recon" option using the newly renamed files, or simply run the slightly older Treetime version (up to and including 0.8.6) before the latest change that added the curly braces to its hard-coded output filenames.

## Installation

POUTINE runs on **Linux**.  Windows is not supported because treetime does not run there, and macOS is not currently a supported platform.

The recommended route is conda (or micromamba), which provides the exact Java, Maven and treetime versions POUTINE is tested with in one pinned environment:

```
git clone https://github.com/Peter-Two-Point-O/POUTINE.git
cd POUTINE
micromamba create -n poutine -f conda-lock.yml     # or, with conda: conda-lock install -n poutine conda-lock.yml
micromamba activate poutine                        # or: conda activate poutine
mvn package                                        # builds target/poutine-1.0.0.jar
bin/poutine --help
```

To get a plain `poutine` command, link the launcher into the environment (the link can be recreated at any time and survives rebuilding the jar):

```
ln -s "$PWD/bin/poutine" "$CONDA_PREFIX/bin/poutine"
```

The environment contains:

*   Java 21 (a JVM of at least this version is required; newer JVMs bring substantial speed and memory improvements over old ones such as Java 8)
*   Maven (only needed to build POUTINE)
*   treetime 0.12.1 and its Python dependencies (only needed when POUTINE has to run the ancestral reconstruction; not needed with `-u`)

[conda-lock.yml](conda-lock.yml) fixes every package version and checksum, so everyone gets the same environment.  See [docs/environment.md](docs/environment.md) for how it is built and updated.  A bioconda package is planned.

Without conda you need JDK 21 or newer, Maven 3.9 or newer, and `treetime` on your `PATH`; this route is not tested.

## Executing The Program

Execute: `bin/poutine --help` to see all command-line options.

`bin/poutine` works from any directory.  It stops with a clear message if Java 21 or newer is not available.  If you linked it into your environment as shown above, simply run `poutine --help`.  More about the launcher (including `POUTINE_JAVA_OPTS` for JVM options such as `-Xmx16g`) is in [docs/launcher.md](docs/launcher.md).

Small example datasets to try are in [tests/data](tests/data); for example, from the repository root:

```
bin/poutine -f tests/data/mtb-reference/mtb_maf_05.fasta -t tests/data/mtb-reference/mtb_maf05.newick \
    -p tests/data/mtb-reference/mtb.phenos -m tests/data/mtb-reference/mtb_maf_05.map -r 10000 -d results
```

Since a picture is worth a thousand words:  

![Image of new cli](https://github.com/Peter-Two-Point-O/Easy-Is-Better-Than-Better/blob/master/images/poutine_cli_screenshot_4.png)

This is what a clean run should look like in the terminal with default settings:  

![Image of new console updates](https://github.com/Peter-Two-Point-O/Easy-Is-Better-Than-Better/blob/master/images/poutine_console_screenshot_2.png)

## Understanding The Output Files

POUTINE is now encouraging the following directory structure:

```
out_dir/
|___ ancestral_reconstruction_dir/
     |___ ancestral_sequences.fasta
     |___ ancestral_tree.newick
     |___ annotated_tree.nexus
     |___ sequence_evolution_model.txt     
|___ poutine_session_current_time.log (any runtime errors will be found in this file)
|___ poutine_session_current_time.debug (temporary facility, feel free to use it as it gives a bunch of internal stats)
|___ poutine_session_current_time.out (main results file)
|___ poutine_session_current_time.out.sorted_by_a1_maxT (major allele associations across all sites sorted by maxT)
|___ poutine_session_current_time.out.sorted_by_a2_maxT (minor allele associations across all sites sorted by maxT)
```

For convenience, you can view the results output files in pretty-format using the following Unix command:  
`column -ts "Ctrl-v <tab>" output_filename.out.sorted_by_a2_maxT | less -S`

To set TAB as the delimiter, macOS and Linux systems usually want `Ctrl-v <tab>` which is a Ctrl-v followed immediately by the tab key.

The results files that most users will be interested in are \*.out.sorted\_by\_a1\_maxT and \*.out.sorted\_by\_a2\_maxT.  Both output files contain the same column format, and the columns are:

1.  segsite\_ID:  Internal integer ID that POUTINE assigns to each segregating site.  This value is sequential in the same order as the input physical positions file.
2.  physical\_pos:  Physical position of the site as specified in the input physical positions file.
3.  allele1:  Major allele.
4.  allele2:  Minor allele.
5.  a1\_count:  Total homoplasy count at major allele.
6.  a2\_count:  Total homoplasy count at minor allele.
7.  a1\_count\_extant\_only:  Total homoplasy count at major allele in only extant input samples (i.e. homoplasies identified on the internal branches from ancestral reconstruction are not counted).
8.  a2\_count\_extant\_only:  Total homoplasy count at minor allele in only extant input samples (i.e. homoplasies identified on the internal branches from ancestral reconstruction are not counted).
9.  obs\_homoplasy\_counts:  Total homoplasy counts for both alleles broken down by phenotype.  The format is \[a1 cases, a1 controls, a2 cases, a2 controls\].
10.  r\_a1:  # replicates from the empirical null distribution that is equal to or more extreme than the observed test statistic (major allele only).
11.  r\_a2:  # replicates from the empirical null distribution that is equal to or more extreme than the observed test statistic (minor allele only).
12.  pointwise\_pvalue\_a1:  Pointwise resampling-derived estimate for the major allele.
13.  pointwise\_pvalue\_a2:  Pointwise resampling-derived estimate for the minor allele.
14.  obs\_binom\_pvalue\_a1:  Binomial test p-value of the observed homoplasy counts at the major allele.
15.  obs\_binom\_pvalue\_a2:  Binomial test p-value of the observed homoplasy counts at the minor allele.
16.  r\_maxT\_a1:  # max(T) test statistics from resampling that is equal to or more extreme than the observed test statistic (major allele only).
17.  r\_maxT\_a2:  # max(T) test statistics from resampling that is equal to or more extreme than the observed test statistic (minor allele only).
18.  familywise\_pvalue\_a1:  max(T) FWER for the major allele.
19.  familywise\_pvalue\_a2:  max(T) FWER for the minor allele.

## Citation

Our preprint is currently available on bioRxiv:  [**Classic genome-wide association methods are unlikely to identify causal variants in strongly clonal microbial populations.**](https://www.biorxiv.org/content/10.1101/2021.06.30.450606v1)

## License

See the [LICENSE](https://github.com/Peter-Two-Point-O/Easy-Is-Better-Than-Better/blob/5803828df31f41951f69a70b9cca61135562ff28/LICENSE) file for license rights and limitations (GNU GPL3).

## Upcoming Major Features/Code Changes

*   Set-testing to address allelic heterogeneity
*   Bootstrapped effect size calculations with confidence intervals
*   Ability to homoplasy count in the accessory genome
*   Ability to homoplasy count indels
*   Multi-thread homoplasy identification step

## Notes From Alpha Development Of POUTINE

### Recent Major Code Changes

*   Removed q-values from the significance assessment. Thus, all things R have been removed. The main reason for this feature removal is because the resampling-derived FWER (maxT variant) is sufficient for users to sort and look for top hits (remember the philosophy here is "easier is better than better"). It's also more robust than many methods in the FDR space because max(T) better address dependency structures between segregating sites.  In a future release, when we are likely to add estimation statistics like a resampling-derived effect size + confidence intervals, we can reconsider the progress of FDR-based methods for addressing dependence structures.
*   Incorporated treetime for purposes of genotypic ancestral reconstruction using the default optimized joint probabilities method.
*   (Historical note, 2020: current support is Linux only, see Installation.) The program should be fully platform-independent now. ~Waiting for Windows users to get back to us to verify. Tests on MacOS/Intel and Linux/Intel were successful. NOTE: Windows users should check out the code from the platform\_independence branch. Once we verify it works, we'll merge this branch back into master.~
*   Full command-line interface. This will be our store-front! so will try to make this elegant and easy.
*   Incorporate consume\_results.sh code into the main program along with other facilities to sort and pretty-format results.
*   Organize all program output (e.g. various results files, log file, debugging file, proper console messages, etc).
*   There is now a bounty of checks (e.g. malformed input file formats, etc) that allow the program to fail fast and meaningfully. The user is now prompted with console and log messages that provide either warnings and/or points them to further action.
*   All output (whether to console or file) is now pretty formatted and human understandable, so if there is anything you want to see changed, just let us know!
*   New command-line feature (-u or --use-precomputed-anc-recon): this new feature allows the user to utilize a precomputed ancestral reconstruction fasta and newick file. When the option is turned on, treetime is bypassed completely and the program understands that it is now looking at a fasta file with ancestral genotypes as well as a newick file with internal nodes labeled. In addition, I've updated the program to output the Newick tree that I parse out from treetime's nexus output file. This allows users to easily reuse this ancestral\_tree.newick file (along with its accompanying ancestral\_sequences.fasta file) for subsequent poutine sessions to bypass ancestral reconstruction and more quickly explore other GWAS settings and/or phenotypes. This new feature is of great utility for moderate to large datasets where the ancestral reconstruction phase is the rate-limiting step and there is a desire to run multiple poutine sessions based upon the same input genotypes and tree.
*   Program now identifies # monomorphic, bi/tri/quad-allelic sites, and reporting this metric in the log file (and debug file). This is particularly useful as a sanity check to see which sites are being assayed by poutine (only biallelic sites for now). I anticipate users not considering this point, and thus seeing the # non-biallelic sites can potentially alert the user to either problems in their dataset and/or # sites that are not being considered by poutine. This need came from analyzing the discovery set where a substantial number of sites turned out to be monomorphic (due likely to subclonal heterogeneity being picked up by the variant caller). A future version may include the capability of considering multiallelic sites perhaps using a multinomial test.
*   Program now works with missing phenotype data.

> Philosophy behind how the algorithm treats missing phenotype data:
> 
> Two ideas:
> 
> ```
> 1. Missing data is not the absence of data but instead the presence of information that encapsulates whether the missingness is random or non-random.   
> 2. The observed and null distributions must be comparable:  we can't have one dist as a f(case, control) and the other dist as a f(case, control, missing)
> ```

> Two ways missing phenotype data can be used with the program: Option 1 requires the user modify their input geno and pheno files. Option 2 requires nothing to be changed by the user.  
> \* Option 1: preprocess and delete all samples missing pheno data from both input genotype fasta file and input pheno file. Must be in both files!  
> \* Option 2: leave all input files untouched. The program designates missing pheno data with a non-0|1 value. This allows the program to treat missing/improperly coded pheno data as the presence of information and protect against potentially non-random bias in missingness. Samples with missing pheno data are kept in both input genotype file and input phenotype file. Doing this allows test statistics to be calculated with observed data that also includes missing phenos (eg at a specific segsite, a homoplasic mutation may be missing pheno data from the sample this genotype comes from, the observed test statistic will reflect this by not counting this homoplasic mutation towards either the tot # trials or tot # cases). During resampling, missing phenos are also permuted and test statistics also incorporate missing pheno data at homoplasic mutations in the same way as test statistics for observed data. This approach allows the observed distribution to be compareable to the resampled null distribution (they are both a function of 3 states: case, control, missing). What I do not recommend is the user keeping samples with missing pheno data in their input genotype file while removing these same samples from the input phenotype file, and vice versa. If this is done the two distributions, observed and null, are not as comparable because there could be non-random bias in missingness. Consider that if you use samples with missing pheno data in the input geno file, then sites containing observed homoplasic mutations with missing pheno data will be calculated with the missing data incorporated into the test statistic, while resampled test statistics will never see missingness because all samples with missing phenos were removed from the input pheno file! As such, the observed pvalues are a function of cases, controls, and non-random missingness in phenotypes and the resampled pvalues are only a function of cases and controls.

> Both options account for potential non-random missingness; any bias is reflected in both observed and null distributions. Option 1 does this by seeing missingness implicitly (ie all missingness is removed from all input files as a preprocessing step). Option 2 does this by explicitly modeling missingness (ie missing pheno data is incorporated in permutations and also in observed test statistics).

> Differences between the 2 options: I think option 2 is less error prone because I anticipate users removing samples with missing pheno data by simply deleting those rows from the pheno file only (and not also in the input geno file as required). I could check for this with code and prompt the user to modify input files, but then modifying fasta files by an average user could also introduce errors. With option 2, the program now protects a user if they are not even aware there are phenos with missing data/improperly coded.

*   Program now understands missing genotypes.

> The philosophy here is essentially the same as missing phenotypes (though the code is not!):

> I take a middle route where I preserve as much data as I can (i.e. segsites with some missing genotypes) while not adding to the data in any way (e.g. no imputation). In a similar fashion to how the program deals with missing phenos, any non-random structure in genotype missingness is reflected in both obs and null dists, as such they can be safely compared.

> In a future version for more explicit missing genotype control, one could build in preprocessing steps to both summarize genotype missingness by both sample and site, and also to test genotype missingness conditional on phenos at each site (i.e. are missing genotypes seen in more cases than controls?) say using a fisher's exact test. This allows the user to preprocess and remove any sites where the genotype missingness looks highly non-random. This preprocessing approach is how plink handles missing geno data.

Suite of "niceties" to make the program easier to use:

1.  Program now checks for mismatched sample names across input files. This is a common error and thus worth checking or else results may be incorrect. Specifically, the program now checks for mismatched sample names across input genotype, phenotype, and tree files. Note that this program itself does not place any restrictions on the makeup of the string that comprises the sample name. The only requirement is that sample names across files match. Program fails-fast if the sample names do not match, and the log file now reports the sample names in the phenotype file that do not match with either genotype and/or tree file.
2.  Improvements to log file, command-line help message, and terminal progress indicator.

### Current Major Bugs

**No major bugs!**

**FIXED:**  Concurrency bug during resampling: This bug only occurs sometimes. If your test run doesn't run to completion (`CLEAN EXIT` should be the last line in the output file) and you see the program hang, simply kill the process and restart it for now.

Sometimes the program would lock up and freeze in perpetuity. Based upon my output showing aspects of the thread pool, it looked like a few of the threads sometimes did not execute till completion, and thus the countdown latch is never decremented. I thought this was weird because no stack trace was ever seen in the output. Best guess was that we have a race condition at the heart of the bug.

I explicitly attempted to catch all runtime errors inside each thread, and voila: we get the classic NullPointerException during a run where the program locks up. Turns out the bug is during read access of the hash table cache I used to optimize the running time (worked fine in single-threaded mode). The cache is not thread-safe; specifically, the put() operation is not atomic in the sense that a key could be fully updated while the value is not fully updated, thus not satisfying the happens-before relation necessary for memory consistency between threads. I'm guessing this memory inconsistency can happen either because the value actually hasn't been updated OR it has been updated but not flushed from the cpu memory (i.e. register or L1/L2 caches) into main memory for other threads to see, either way the value is null to other threads executing a get() on an identical key.

The fix is to make only part of the put() operation atomic so that all reads from the cache have a proper happens-before relation with the writes to the cache on an identical key. No need to synchronize and lock larger sections of code, thus keeping threads more active.
