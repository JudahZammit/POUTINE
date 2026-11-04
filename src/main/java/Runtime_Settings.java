import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;

/** Runtime options. Static state: picocli sets the values, everything else reads the getters. */
class Runtime_Settings {
  // default value := max # logical processors available (# cpu cores and hyperthreading)
  private static int num_threads = Runtime.getRuntime().availableProcessors();

  static int get_num_threads() {
    return num_threads;
  }

  @Option(
      names = {"-T", "--threads"},
      paramLabel = "<# threads>",
      description = "# Threads used for parallel processing (default: max # logical processors)",
      required = false)
  private static void validate_and_set_thread_option(int user_value) {
    if (user_value > 0) num_threads = user_value;
    else
      throw new ParameterException(
          Homoplasy_Counter.spec.commandLine(),
          String.format(
              "%nInvalid value '%s' for option '--threads': " + "# threads must be > 0",
              user_value));
  }
}
