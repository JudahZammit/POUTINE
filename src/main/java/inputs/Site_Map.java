package inputs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * The map file: one line per segregating site, in the order of the fasta columns. Tab-delimited
 * with no header. The last column is the physical position; the other columns are not used. A site
 * is identified by its line number (0-based).
 */
public class Site_Map {
  private final int[] positions;

  public Site_Map(String filename) throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
      positions = reader.lines().mapToInt(Site_Map::parse_position).toArray();
    }
  }

  private static int parse_position(String line) {
    String[] columns = line.split("\t");
    return Integer.parseInt(columns[columns.length - 1]);
  }

  public int num_sites() {
    return positions.length;
  }

  public int[] positions() {
    return positions.clone();
  }
}
