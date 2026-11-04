import picocli.CommandLine.Option;

/** Phenotype, tree and map input files. Static state: picocli sets them, others use the getters. */
class Other_Input_Files {
  @Option(
      names = {"-p", "--phenos"},
      description =
          "Phenotype file (case = 1, control = 0. Format is two columns tab-delimited with no"
              + " header: column one is sample id, column two is phenotype)",
      required = true)
  private static String pheno_filename;

  static String get_pheno_filename() {
    return pheno_filename;
  }

  @Option(
      names = {"-t", "--tree"},
      description = "Newick file (ancestral newick file if -u in use)",
      required = true)
  private static String newick_filename;

  static String get_newick_filename() {
    return newick_filename;
  }

  @Option(
      names = {"-m", "--map"},
      description =
          "Physical positions file (PLINK map format or tab-delimited with no header: last"
              + " column represents physical positions, all other columns not used)",
      required = true)
  private static String map_filename;

  static String get_map_filename() {
    return map_filename;
  }
}
