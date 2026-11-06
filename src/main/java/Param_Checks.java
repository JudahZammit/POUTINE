import java.io.File;

/** Checks shared by the parameter classes' verify methods. They throw IllegalArgumentException. */
class Param_Checks {
  static void require_file_exists(String filename, String option) {
    if (!new File(filename).exists())
      throw new IllegalArgumentException(
          String.format(
              "File '%s' for option '%s' cannot be found.  Please make sure the correct path and"
                  + " file name are specified.",
              filename, option));
  }

  static void require_valid_value(boolean valid, String option, Object value, String rule) {
    if (!valid)
      throw new IllegalArgumentException(
          String.format("Invalid value '%s' for option '%s': %s", value, option, rule));
  }
}
