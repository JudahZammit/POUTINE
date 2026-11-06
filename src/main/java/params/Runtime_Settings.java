package params;

import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;

/** Runtime options. Static state: picocli sets the values, everything else reads the getters. */
public class Runtime_Settings {
  @Option(
      names = {"-T", "--threads"},
      paramLabel = "<# threads>",
      description = "# Threads used for parallel processing (default: max # logical processors)",
      required = false)
  // default value := max # logical processors available (# cpu cores and hyperthreading)
  private static int num_threads = Runtime.getRuntime().availableProcessors();

  public static int get_num_threads() {
    return num_threads;
  }

  public static void verify(CommandLine cmdline) {
    try {
      verify_threads();
    } catch (IllegalArgumentException e) {
      throw new ParameterException(cmdline, String.format("%n%s", e.getMessage()));
    }
  }

  private static void verify_threads() {
    Param_Checks.require_valid_value(
        num_threads > 0, "--threads", num_threads, "# threads must be > 0");
  }
}
