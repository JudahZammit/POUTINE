package dso.burden;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The variant annotation file (--anno-file): one line per variant with the variant id, its gene and
 * its type. The id looks like REF-ID_POS:REF:ALT, for example H37Rv_371:C:T. The annotations are
 * keyed by position, which is what links a variant to a site.
 */
public class Variant_Annotations {

  /** An annotated variant. The alleles come from its id. */
  public record Annotation(char ref_allele, char alt_allele, String gene, String type) {}

  private final Map<Integer, List<Annotation>> by_position = new LinkedHashMap<>();

  public Variant_Annotations(String filename) throws IOException {
    Table_Reader.for_each_row(filename, 3, this::add);
  }

  private void add(String[] columns) {
    Variant_Id id = Variant_Id.parse(columns[0]);
    Annotation annotation =
        new Annotation(id.ref_allele(), id.alt_allele(), columns[1], columns[2]);
    by_position.computeIfAbsent(id.position(), p -> new ArrayList<>()).add(annotation);
  }

  public int size() {
    return by_position.size();
  }

  /**
   * @return the annotations at this position; empty if there are none.
   */
  public List<Annotation> annotations(int position) {
    return by_position.getOrDefault(position, List.of());
  }
}
