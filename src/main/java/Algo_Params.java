import java.util.Random;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;

/** Algorithm parameters. Static state: picocli sets the values, others read the getters. */
class Algo_Params {
  @Option(
      names = {"-r", "--replicates"},
      paramLabel = "<# replicates>",
      description = "# Replicates used for resampling (default: 100000)",
      required = false,
      order = 1)
  private static int m = 100000; // default value

  static int get_m() {
    return m;
  }

  @Option(
      names = {"-c", "--min_hcount"},
      paramLabel = "<count>",
      description = "Minimum # homoplasic mutations required at each allele (default: 1)",
      required = false,
      order = 2)
  // default value. 1 := use all homoplasic seg sites, 0 := use all seg sites including
  // those without any homoplasic mutations on either allele
  private static int min_hcount = 1;

  static int get_min_hcount() {
    return min_hcount;
  }

  @Option(
      names = {"-u", "--use-precomputed-anc-recon"},
      description =
          "Use precomputed ancestral reconstruction as input. Specify --fasta for ancestral fasta"
              + " file and --tree for ancestral newick file.",
      required = false,
      order = 3)
  private static boolean use_precomputed_anc_recon = false;

  static boolean get_use_precomputed_anc_recon() {
    return use_precomputed_anc_recon;
  }

  @Option(
      names = {"-S", "--rng-seed"},
      paramLabel = "<seed>",
      description =
          "Seed for the random number generator used in resampling and passed to treetime"
              + " (default: random, recorded in the log). Results reproduce exactly only with"
              + " --threads 1",
      required = false,
      order = 4)
  // default value := random seed (always logged so that any session can be repeated)
  private static int rng_seed = new Random().nextInt(Integer.MAX_VALUE);

  static int get_rng_seed() {
    return rng_seed;
  }

  public static void verify(CommandLine cmdline) {
    try {
      verify_replicates();
      verify_min_hcount();
      verify_rng_seed();
    } catch (IllegalArgumentException e) {
      throw new ParameterException(cmdline, String.format("%n%s", e.getMessage()));
    }
  }

  private static void verify_replicates() {
    Param_Checks.require_valid_value(
        m > 0, "--replicates", m, "# replicates used for resampling must be > 0");
  }

  private static void verify_min_hcount() {
    Param_Checks.require_valid_value(
        min_hcount >= 0,
        "--min_hcount",
        min_hcount,
        "# homoplasic mutations at a segregating site must be >= 0");
  }

  private static void verify_rng_seed() {
    Param_Checks.require_valid_value(rng_seed >= 0, "--rng-seed", rng_seed, "seed must be >= 0");
  }
}
