import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;

/** Burden test options (regenie format). Static state: get_burden() says if they are in use. */
class Burden_Test_Options {
  @Option(
      names = {"--anno-file"},
      description =
          "Variant annotation file (space-delimited with no header: variant id, gene, variant"
              + " effect)",
      required = true)
  private static String anno_filename;

  static String get_anno_filename() {
    return anno_filename;
  }

  @Option(
      names = {"--set-list"},
      description =
          "Gene set list file (space-delimited with no header: gene, chromosome, position,"
              + " comma-separated variant ids)",
      required = true)
  private static String setlist_filename;

  static String get_setlist_filename() {
    return setlist_filename;
  }

  @Option(
      names = {"--mask-def"},
      description =
          "Mask definition file (space-delimited with no header: mask name, comma-separated"
              + " variant effects)",
      required = true)
  private static String mask_filename;

  static String get_mask_filename() {
    return mask_filename;
  }

  @Option(
      names = {"--burden"},
      description = "Run burden testing (required with the burden test  files; not yet used)",
      required = true)
  private static boolean burden;

  static boolean get_burden() {
    return burden;
  }

  @Option(
      names = {"--no-reference"},
      description =
          "Do not include 'reference' as a variant effect (default: false). Note that this will not"
              + " consider variants that are potentially associated with the phenotype if that"
              + " variant matches the reference.")
  private static boolean no_reference = false;

  static boolean get_no_reference() {
    return no_reference;
  }

  static void check_files_exist(CommandLine cmdline) {
    require_file_exists(cmdline, anno_filename, "--anno-file");
    require_file_exists(cmdline, setlist_filename, "--set-list");
    require_file_exists(cmdline, mask_filename, "--mask-def");
  }

  private static void require_file_exists(CommandLine cmdline, String filename, String option) {
    if (!new File(filename).exists())
      throw new ParameterException(
          cmdline,
          String.format(
              "%nFile '%s' for option '%s' cannot be found.  Please make sure the correct path"
                  + " and file name are specified.",
              filename, option));
  }

  static void write_log(BufferedWriter log) throws IOException {
    log.write(burden ? describe() : "burden testing: not in use");
    log.newLine();
  }

  private static String describe() {
    return String.format(
        "gene set annotation file: %s%nset list file: %s%nburden test mask definition file:"
            + " %s%nIs --no-reference in use?: %b",
        anno_filename, setlist_filename, mask_filename, no_reference);
  }
}
