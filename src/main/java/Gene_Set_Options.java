import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;

/** Gene set testing options (regenie format). Homoplasy_Counter holds null when not in use. */
class Gene_Set_Options {
  @Option(
      names = {"--anno-file"},
      description =
          "Variant annotation file (space-delimited with no header: variant id, gene, variant"
              + " effect)",
      required = true)
  String anno_filename;

  @Option(
      names = {"--set-list"},
      description =
          "Gene set list file (space-delimited with no header: gene, chromosome, position,"
              + " comma-separated variant ids)",
      required = true)
  String setlist_filename;

  @Option(
      names = {"--mask-def"},
      description =
          "Mask definition file (space-delimited with no header: mask name, comma-separated"
              + " variant effects)",
      required = true)
  String mask_filename;

  @Option(
      names = {"--burden"},
      description = "Run gene set burden testing (required with the gene set files; not yet used)",
      required = true)
  boolean burden;

  @Option(
      names = {"--no-reference"},
      description =
          "Do not include 'reference' as a variant effect (default: false). Note that this will not"
              + " consider variants that are potentially associated with the phenotype if that"
              + " variant matches the reference.")
  boolean no_reference = false;

  void check_files_exist(CommandLine cmdline) {
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

  static void write_log(Gene_Set_Options options, BufferedWriter log) throws IOException {
    log.write(options == null ? "gene set testing: not in use" : options.describe());
    log.newLine();
  }

  private String describe() {
    return String.format(
        "gene set annotation file: %s%ngene set list file: %s%ngene set mask definition file:"
            + " %s%nIs --no-reference in use?: %b",
        anno_filename, setlist_filename, mask_filename, no_reference);
  }
}
