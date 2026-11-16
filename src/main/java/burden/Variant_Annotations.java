package burden;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The variant annotation file (--anno-file): one line per variant with the variant id, its gene and
 * its type. The id looks like REF-ID_POS:REF:ALT, for example H37Rv_371:C:T. The annotations are
 * keyed by position, which is what links a variant to a site.
 */
public class Variant_Annotations {

  /** An annotated variant. The alleles come from its id. */
  public record Annotation(char ref_allele, char alt_allele, String gene, String type) {}

  private final Map<Integer, Annotation> by_position = new LinkedHashMap<>();

  public Variant_Annotations(String filename) throws IOException {
    Table_Reader.for_each_row(filename, 3, this::add);
  }

  private void add(String[] columns) {
    Variant_Id id = Variant_Id.parse(columns[0]);
    Annotation annotation = new Annotation(id.ref_allele(), id.alt_allele(), columns[1], columns[2]);
    if (by_position.putIfAbsent(id.position(), annotation) != null) {
      throw new IllegalArgumentException("duplicate position " + id.position());
    }
  }

  public int size() {
    return by_position.size();
  }

  /** @return the annotation of the variant at this position, or null if there is none. */
  public Annotation annotation(int position) {
    return by_position.get(position);
  }
}
