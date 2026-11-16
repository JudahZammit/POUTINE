package diffs;

/** Sequence_Diffs after gene-set aggregation. Stub: it only copies the base for now. */
public class Aggregated_Sequence_Diffs extends Sequence_Diffs {

  /** Shares the base's arrays; do not use the base afterwards. */
  public Aggregated_Sequence_Diffs(
      Sequence_Diffs base,
      String fasta_map_filename,
      String anno_filename,
      String setlist_filename,
      String mask_filename,
      boolean no_reference) {
    this.root_name = base.root_name;
    this.root_seq = base.root_seq;
    this.root_child_names = base.root_child_names;
    this.leaf_names = base.leaf_names;
    this.leaf_allele_counts = base.leaf_allele_counts;
    this.diff_nodes = base.diff_nodes;
    this.curr_alleles = base.curr_alleles;
    this.prev_alleles = base.prev_alleles;
    this.node_index = base.node_index;
    this.node_names = base.node_names;
  }
}
