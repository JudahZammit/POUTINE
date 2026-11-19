package diffs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import burden.Gene_Sets;
import burden.Mask_Definitions;
import burden.Variant_Annotations;
import burden.Variant_Annotations.Annotation;
import inputs.Site_Map;

/** Sequence_Diffs after gene-set aggregation. Stub: it only copies the base for now. */
public class Aggregated_Sequence_Diffs extends Sequence_Diffs {

  private final int num_genes;

  private static final char NO_CHANGE = 'A';
  private static final char MODIFIED = 'T';
  private final Variant_Annotations annotations;
  private final Gene_Sets gene_sets;
  private final Mask_Definitions masks;
  private final boolean no_reference;
  private final Site_Map site_map;
  private final String[] gene_names;

  /** Shares the base's arrays; do not use the base afterwards. */
  public Aggregated_Sequence_Diffs(
      Sequence_Diffs base,
      Site_Map site_map,
      Variant_Annotations annotations,
      Gene_Sets gene_sets,
      Mask_Definitions masks,
      boolean no_reference) {

    this.site_map = site_map;
    this.annotations = annotations;
    this.gene_sets = gene_sets;
    this.masks = masks;
    this.no_reference = no_reference;

    // Keep the same phylogenetic tree as the base
    this.root_name = base.root_name;
    this.root_child_names = base.root_child_names;
    this.leaf_names = base.leaf_names;
    this.node_index = base.node_index;
    this.node_names = base.node_names;

    this.num_genes = gene_sets.num_genes();
    this.gene_names = get_gene_names(this.num_genes);
    this.root_seq = get_trivial_root_seq(num_genes);

    aggregate_diffs(base.diff_nodes, base.curr_alleles, base.prev_alleles);

    fill_curr_and_prev_alleles(this.diff_nodes);
    fill_leaf_allele_counts();
  }

  private String[] get_gene_names(int num_genes) {
    String[] all_gene_names = new String[num_genes];
    int i = 0;
    for (Gene_Sets.Gene_Set gene_set : gene_sets.all()) {
      all_gene_names[i++] = gene_set.name();
    }
    return all_gene_names;
  }

  private static char[] get_trivial_root_seq(int num_genes) {
    char[] seq = new char[num_genes];
    for (int i = 0; i < num_genes; i++) seq[i] = NO_CHANGE;
    return seq;
  }

  private void aggregate_diffs(int[][] diff_nodes, char[][] curr_alleles, char[][] prev_alleles) {
    this.diff_nodes = new int[this.num_genes][];

    for (int gene = 0; gene < this.num_genes; gene++) {
      // Get index of all nodes where this gene has been modified
      int[] nodes = get_mod_nodes(gene, diff_nodes, curr_alleles, prev_alleles);
      this.diff_nodes[gene] = nodes;
    }
  }

  private int[] get_var_positions(int gene) {
    String gene_name = gene_names[gene];
    Gene_Sets.Gene_Set gene_set = gene_sets.get(gene_name);

    if (gene_set == null) {
      throw new NullPointerException("gene set not found for gene " + gene_name);
    }

    return gene_set.variant_positions().stream()
        .mapToInt(site_map::to_contiguous)
        .filter(site -> site >= 0)
        .toArray();
  }

  private List<Annotation> get_relevent_annotations(int positions) {
    List<Annotation> var_annotations = this.annotations.annotations(positions);
    List<Annotation> out_annotations = new ArrayList<>();

    Set<String> valid_types = this.masks.types();

    for (Annotation var_annot : var_annotations) {
      if (!this.no_reference) {
        out_annotations.add(
            new Annotation(
                var_annot.alt_allele(), var_annot.ref_allele(), var_annot.gene(), "reference"));
      }
      if (valid_types.contains(var_annot.type())) {
        out_annotations.add(var_annot);
      }
    }

    return out_annotations;
  }

  private int[] get_mod_nodes(
      int gene, int[][] diff_nodes, char[][] curr_alleles, char[][] prev_alleles) {

    int[] var_positions = get_var_positions(gene);

    HashSet<Integer> mod_nodes = new HashSet<>();

    for (int var_position : var_positions) {
      List<Annotation> var_annotation =
          get_relevent_annotations(site_map.to_absolute(var_position));

      int[] pos_diff_nodes = diff_nodes[var_position];
      char[] pos_curr_alleles = curr_alleles[var_position];
      char[] pos_prev_alleles = prev_alleles[var_position];

      for (int i = 0; i < pos_diff_nodes.length; i++) {
        if (diff_in_annotation_set(var_annotation, pos_curr_alleles[i], pos_prev_alleles[i])) {
          mod_nodes.add(pos_diff_nodes[i]);
        }
      }
    }

    return mod_nodes.stream().mapToInt(Integer::intValue).toArray();
  }

  private boolean diff_in_annotation_set(
      List<Annotation> var_annotations, char curr_allele, char prev_allele) {
    for (Annotation annot : var_annotations) {
      if (curr_allele == annot.alt_allele() && prev_allele == annot.ref_allele()) {
        return true;
      }
    }
    return false;
  }

  /** Kept simple: the leaf diffs of a gene are its modified count, the other leaves are not. */
  private void fill_leaf_allele_counts() {
    this.leaf_allele_counts = new int[this.num_genes][ALLELE_INDEX.size()];

    for (int gene = 0; gene < this.num_genes; gene++) {
      int num_diffs = count_leaf_diffs(this.diff_nodes[gene]);
      this.leaf_allele_counts[gene][ALLELE_INDEX.get(NO_CHANGE)] =
          this.leaf_names.size() - num_diffs;
      this.leaf_allele_counts[gene][ALLELE_INDEX.get(MODIFIED)] = num_diffs;
    }
  }

  private int count_leaf_diffs(int[] nodes) {
    int count = 0;
    for (int node : nodes) {
      if (this.leaf_names.contains(this.node_names[node])) count++;
    }
    return count;
  }

  private void fill_curr_and_prev_alleles(int[][] diff_nodes) {
    this.curr_alleles = new char[this.num_genes][];
    this.prev_alleles = new char[this.num_genes][];

    for (int gene = 0; gene < this.num_genes; gene++) {
      int[] nodes = diff_nodes[gene];
      this.curr_alleles[gene] = new char[nodes.length];
      this.prev_alleles[gene] = new char[nodes.length];

      for (int i = 0; i < nodes.length; i++) {
        this.curr_alleles[gene][i] = MODIFIED;
        this.prev_alleles[gene][i] = NO_CHANGE;
      }
    }
  }
}
