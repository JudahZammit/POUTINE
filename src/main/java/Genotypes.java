import picocli.CommandLine.Option;

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
}
