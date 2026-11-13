import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

public class Gene_Set_Aggregator {

  public static Sequence_Diffs aggregate(
      Sequence_Diffs seq_diffs,
      String fasta_map_filename,
      String anno_filename,
      String setlist_filename,
      String mask_filename,
      boolean no_reference) {

    return seq_diffs;
  }

  private static HashMap<Integer, String> get_fasta_map(String fasta_map_filename)
      throws Exception {
    HashMap<Integer, String> fasta_map = new HashMap<>();
    List<String> lines = Files.readAllLines(Path.of(fasta_map_filename));
    final int SEQUENCE_ID_IDX = 1;
    for (int i = 0; i < lines.size(); i++) {
      fasta_map.put(i, lines.get(i).split("\t")[SEQUENCE_ID_IDX]);
    }
    return fasta_map;
  }
}
