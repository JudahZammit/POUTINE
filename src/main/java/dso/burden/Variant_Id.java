package dso.burden;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A variant id in the burden test files: REF-ID_POS:REF:ALT, for example H37Rv_371:C:T. */
record Variant_Id(int position, char ref_allele, char alt_allele) {

  private static final Pattern FORMAT = Pattern.compile(".+_(\\d+):([^:\\s]):([^:\\s])");

  static Variant_Id parse(String id) {
    Matcher match = FORMAT.matcher(id);
    if (!match.matches()) {
      throw new IllegalArgumentException(
          "variant id should look like REF-ID_POS:REF:ALT, not " + id);
    }
    return new Variant_Id(
        Integer.parseInt(match.group(1)), match.group(2).charAt(0), match.group(3).charAt(0));
  }
}
