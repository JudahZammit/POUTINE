import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;

/** Genotype input file (vcf or fasta, mutually exclusive). Static state, read via the getters. */
class Genotypes {
  @Option(
      names = {"-v", "--vcf"},
      description = "Multi-sample vcf file (this option is currently not in use/available)")
  private static String vcf_filename;

  static String get_vcf_filename() {
    return vcf_filename;
  }

  @Option(
      names = {"-f", "--fasta"},
      description =
          "Multi-fasta file (ancestral fasta file if -u in use). Variable sites only. Sites"
              + " must be in the same order as in the physical position file.")
  private static String msa_fasta_filename;

  static String get_msa_fasta_filename() {
    return msa_fasta_filename;
  }

  public static void verify(CommandLine cmdline) {
    try {
      verify_vcf();
      verify_fasta();
    } catch (IllegalArgumentException e) {
      throw new ParameterException(cmdline, String.format("%n%s", e.getMessage()));
    }
  }

  // --vcf and --fasta are mutually exclusive, so at most one of them is set
  private static void verify_vcf() {
    if (vcf_filename != null) Param_Checks.require_file_exists(vcf_filename, "--vcf");
  }

  private static void verify_fasta() {
    if (msa_fasta_filename != null)
      Param_Checks.require_file_exists(msa_fasta_filename, "--fasta");
  }
}
