import java.io.BufferedWriter;
import java.io.File;
import picocli.CommandLine.Option;

/** Output files and options, plus the writers opened for them. */
class Output_Options {
  @Option(
      names = {"-d", "--out-dir"},
      description = "Output directory for all output files (default: poutine_session_current_time)",
      required = false)
  File output_dir; // default is relative to the current dir

  @Option(
      names = {"-o", "--out"},
      description =
          "Output file (relative to --out-dir) containing association results (default:"
              + " out-dir/poutine_session_current_time.out)",
      required = false)
  File output_file;

  File output_file_sorted_by_a1_maxT;
  File output_file_sorted_by_a2_maxT;
  BufferedWriter out;
  BufferedWriter out_sorted_by_a1_maxT;
  BufferedWriter out_sorted_by_a2_maxT;

  @Option(
      names = {"-l", "--log"},
      description =
          "Log file (relative to --out-dir) containing this session's log ouput and any runtime"
              + " errors that may occur (default: out-dir/poutine_session_current_time.log)",
      required = false)
  File log_file;

  BufferedWriter log;

  @Option(
      names = {"-a", "--anc-recon-out-dir"},
      description =
          "Output directory (relative to --out-dir) for all files related to ancestral"
              + " reconstruction (default: out-dir/ancestral_reconstruction_current_time)",
      required = false)
  File anc_recon_dir; // default is a subdirectory of output_dirname

  @Option(
      names = {"-s", "--timestamp"},
      description =
          "Add current session's start timestamp to user-specified dir and filenames (default: no"
              + " timestamp)",
      required = false)
  boolean timestamp = false;

  @Option(
      names = {"-X", "--force-overwrite"},
      description =
          "Allow overwriting of existing files having the same user-specified filenames (default:"
              + " program safely exits if existing files detected)",
      required = false)
  boolean force_overwrite = false;

  @Option(
      names = {"-D", "--debug"},
      description =
          "Turns debug mode on: output all debugging information to debug file"
              + " out-dir/poutine_session_current_time.debug (this option is likely temporary for"
              + " testing purposes)",
      required = false)
  boolean DEBUG_MODE = false;

  BufferedWriter debug;
}
