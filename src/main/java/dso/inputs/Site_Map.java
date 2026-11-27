package dso.inputs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The map file: one line per segregating site, in the order of the fasta columns. Tab-delimited
 * with no header. The last column is the physical position; the other columns are not used. A site
 * has two representations: its contiguous index (line number, 0-based) and its absolute (physical)
 * position.
 */
public class Site_Map {
  private static final int NO_SITE = -1;

  private final int[] positions;
  private final Map<Integer, Integer> contiguous_by_absolute = new HashMap<>();

  public Site_Map(String filename) throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
      positions = reader.lines().mapToInt(Site_Map::parse_position).toArray();
    }
    index_positions();
  }

  private void index_positions() {
    for (int site = 0; site < positions.length; site++) {
      if (contiguous_by_absolute.put(positions[site], site) != null) {
        throw new IllegalArgumentException("duplicate position " + positions[site]);
      }
    }
  }

  /** @return the absolute position of the site with this contiguous index. */
  public int to_absolute(int contiguous) {
    return positions[contiguous];
  }

  /** @return the contiguous index of the site at this absolute position, or -1 if there is none. */
  public int to_contiguous(int absolute) {
    return contiguous_by_absolute.getOrDefault(absolute, NO_SITE);
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
