package burden;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * The gene set list (--set-list): one line per gene with its name, chromosome, position and
 * variants. The chromosome is not kept. The variants are written as ids in the file and kept as
 * positions.
 */
public class Gene_Sets {

  public record Gene_Set(String name, int position, List<Integer> variant_positions) {}

  private final Map<String, Gene_Set> by_name = new LinkedHashMap<>();

  public Gene_Sets(String filename) throws IOException {
    Table_Reader.for_each_row(filename, 4, this::add);
  }

  private void add(String[] columns) {
    Gene_Set gene_set =
        new Gene_Set(columns[0], Integer.parseInt(columns[2]), variant_positions(columns[3]));
    if (by_name.putIfAbsent(gene_set.name(), gene_set) != null) {
      throw new IllegalArgumentException("duplicate gene " + gene_set.name());
    }
  }

  private static List<Integer> variant_positions(String variant_ids) {
    return Stream.of(variant_ids.split(",")).map(id -> Variant_Id.parse(id).position()).toList();
  }

  public int num_genes() {
    return by_name.size();
  }

  public Collection<Gene_Set> all() {
    return by_name.values();
  }

  public Gene_Set get(String name) {
    return by_name.get(name);
  }

  /** The gene positions, in the same order as {@link #all()} (the gene index order). */
  public int[] positions() {
    return all().stream().mapToInt(Gene_Set::position).toArray();
  }
}
