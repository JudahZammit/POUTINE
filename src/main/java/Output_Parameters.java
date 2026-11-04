import java.io.File;
import picocli.CommandLine.Option;

/** Output parameters. Static state: picocli sets the values, others read the getters. */
class Output_Parameters {
  @Option(
      names = {"-d", "--out-dir"},
      description = "Output directory for all output files (default: poutine_session_current_time)",
      required = false)
  private static File output_dir; // default is relative to the current dir

  static File get_output_dir() {
    return output_dir;
  }

  @Option(
      names = {"-o", "--out"},
      description =
          "Output file (relative to --out-dir) containing association results (default:"
              + " out-dir/poutine_session_current_time.out)",
      required = false)
  private static File output_file;

  static File get_output_file() {
    return output_file;
  }

  @Option(
      names = {"-l", "--log"},
      description =
          "Log file (relative to --out-dir) containing this session's log ouput and any runtime"
              + " errors that may occur (default: out-dir/poutine_session_current_time.log)",
      required = false)
  private static File log_file;

  static File get_log_file() {
    return log_file;
  }

  @Option(
      names = {"-a", "--anc-recon-out-dir"},
      description =
          "Output directory (relative to --out-dir) for all files related to ancestral"
              + " reconstruction (default: out-dir/ancestral_reconstruction_current_time)",
      required = false)
  private static File anc_recon_dir; // default is a subdirectory of output_dirname

  static File get_anc_recon_dir() {
    return anc_recon_dir;
  }

  @Option(
      names = {"-s", "--timestamp"},
      description =
          "Add current session's start timestamp to user-specified dir and filenames (default: no"
              + " timestamp)",
      required = false)
  private static boolean timestamp = false;

  static boolean get_timestamp() {
    return timestamp;
  }

  @Option(
      names = {"-X", "--force-overwrite"},
      description =
          "Allow overwriting of existing files having the same user-specified filenames (default:"
              + " program safely exits if existing files detected)",
      required = false)
  private static boolean force_overwrite = false;

  static boolean get_force_overwrite() {
    return force_overwrite;
  }

  @Option(
      names = {"-D", "--debug"},
      description =
          "Turns debug mode on: output all debugging information to debug file"
              + " out-dir/poutine_session_current_time.debug (this option is likely temporary for"
              + " testing purposes)",
      required = false)
  private static boolean debug_mode = false;

  static boolean get_debug_mode() {
    return debug_mode;
  }
}
