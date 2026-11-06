package params;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;

/** Input files: genotypes plus the phenotype, tree and map files. Static state, read by getters. */
public class Input_Files {
  @ArgGroup(
      exclusive = true,
      multiplicity = "1",
      validate = true,
      heading = "@|bg(213) %nInput genotype file: vcf or fasta format%n|@")
  private static Genotypes genotypes;

  public static Genotypes get_genotypes() {
    return genotypes;
  }

  @ArgGroup(
      exclusive = false,
      multiplicity = "1",
      validate = true,
      heading = "@|bg(123) %nOther input files%n|@")
  private static Other_Input_Files otherInputFiles;

  public static Other_Input_Files get_other_input_files() {
    return otherInputFiles;
  }

  public static void verify(CommandLine cmdline) {
    Genotypes.verify(cmdline);
    Other_Input_Files.verify(cmdline);
  }
}
