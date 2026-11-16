package burden;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The mask definition file (--mask-def): one line per mask with its name and the variant types it
 * keeps. Only the first mask is used for now; the names of the others are kept so the caller can
 * warn about them.
 */
public class Mask_Definitions {

  private String name;
  private Set<String> types;
  private final List<String> unused_names = new ArrayList<>();

  public Mask_Definitions(String filename) throws IOException {
    Table_Reader.for_each_row(filename, 2, this::add);
    if (name == null) throw new IllegalArgumentException(filename + ": no mask defined");
  }

  private void add(String[] columns) {
    if (name != null) {
      unused_names.add(columns[0]);
      return;
    }
    name = columns[0];
    types = new LinkedHashSet<>(List.of(columns[1].split(",")));
  }

  public String name() {
    return name;
  }

  /** @return the variant types the mask keeps. */
  public Set<String> types() {
    return types;
  }

  /** @return the names of the masks after the first, which are ignored. */
  public List<String> unused_names() {
    return unused_names;
  }
}
