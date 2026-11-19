package burden;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

/**
 * Reads the burden test files (--anno-file, --set-list, --mask-def): space-delimited, no header,
 * blank lines skipped. The map file is not read here (see inputs.Site_Map).
 */
class Table_Reader {

  /**
   * Passes each row to the handler. A row with the wrong number of columns, or one the handler
   * rejects with an IllegalArgumentException, is reported with the file name and line number.
   */
  static void for_each_row(String filename, int columns, Consumer<String[]> handler)
      throws IOException {
    List<String> lines = Files.readAllLines(Path.of(filename));
    for (int i = 0; i < lines.size(); i++) {
      if (!lines.get(i).isBlank()) read_row(filename, i + 1, lines.get(i), columns, handler);
    }
  }

  private static void read_row(
      String filename, int number, String line, int columns, Consumer<String[]> handler) {
    try {
      handler.accept(split(line, columns));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          String.format("%s line %d: %s", filename, number, e.getMessage()), e);
    }
  }

  private static String[] split(String line, int columns) {
    String[] fields = line.trim().split("\\s+");
    if (fields.length != columns) {
      throw new IllegalArgumentException(
          String.format("expected %d columns, found %d", columns, fields.length));
    }
    return fields;
  }
}
