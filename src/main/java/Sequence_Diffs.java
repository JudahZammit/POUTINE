import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.gersteinlab.coevolution.core.data.NewickTree;
import org.gersteinlab.coevolution.core.data.NewickTreeNode;

public class Sequence_Diffs {

  private String root_name;
  private char[] root_seq;
  private HashSet<String> root_child_names;
  private HashSet<String> leaf_names;
  private int[][] leaf_allele_counts;
  private static final Map<Character, Integer> ALLELE_INDEX =
      Map.of('A', 0, 'T', 1, 'G', 2, 'C', 3);

  // The list of sites that are different than the parent of a node a particular id
  // For each site, keep a list of nodes that differ from their parent at that site, and the allele
  // of that node
  private int[][] diff_nodes;
  private char[][] curr_alleles;
  private char[][] prev_alleles;
  private Map<String, Integer> node_index;
  private String[] node_names;

  public Sequence_Diffs(HashMap<String, char[]> seg_sites, NewickTree tree) {
    parse_root(tree.getRoot(), seg_sites);
    parse_leaves(tree);
    count_leaf_alleles(seg_sites);
    create_node_index(seg_sites.keySet());
    parse_diffs(tree, seg_sites);
  }

  private void create_node_index(Collection<String> node_names) {
    this.node_index = new HashMap<>();
    this.node_names = node_names.toArray(String[]::new);
    int index = 0;
    for (String name : node_names) {
      this.node_index.put(name, index++);
    }
  }

  private void parse_diffs(NewickTree tree, HashMap<String, char[]> seg_sites) {

    int[][] node_diff_sites = new int[node_index.size()][];
    char[][] node_diff_alleles = new char[node_index.size()][];
    char[][] node_prev_alleles = new char[node_index.size()][];

    for (String node_id : node_index.keySet()) {
      NewickTreeNode parent = tree.getNode(node_id).getParent();

      String parent_id;
      if (parent == null) {
        parent_id = node_id;
      } else {
        parent_id = parent.getId();
      }

      char[] node_seq = seg_sites.get(node_id);
      char[] parent_seq = seg_sites.get(parent_id);

      List<Integer> diff_sites_list = compare_sequences(node_seq, parent_seq);
      List<Character> diff_alleles_list = new ArrayList<>();
      List<Character> prev_alleles_list = new ArrayList<>();
      for (int site : diff_sites_list) {
        diff_alleles_list.add(node_seq[site]);
        prev_alleles_list.add(parent_seq[site]);
      }

      int node_index_value = node_index.get(node_id);
      node_diff_sites[node_index_value] =
          diff_sites_list.stream().mapToInt(Integer::intValue).toArray();
      node_diff_alleles[node_index_value] = new char[diff_alleles_list.size()];
      node_prev_alleles[node_index_value] = new char[prev_alleles_list.size()];
      for (int i = 0; i < diff_alleles_list.size(); i++) {
        node_diff_alleles[node_index_value][i] = diff_alleles_list.get(i);
        node_prev_alleles[node_index_value][i] = prev_alleles_list.get(i);
      }
    }
    convert_to_site_format(node_diff_sites, node_diff_alleles, node_prev_alleles);
  }

  private List<Integer> compare_sequences(char[] node_seq, char[] parent_seq) {
    List<Integer> diff_sites_list = new ArrayList<>();
    for (int i = 0; i < node_seq.length; i++) {
      if (node_seq[i] != parent_seq[i]) {
        diff_sites_list.add(i);
      }
    }
    return diff_sites_list;
  }

  /** Transposes the per-node diffs into per-site diffs; a site's nodes end up in index order. */
  private void convert_to_site_format(
      int[][] node_diff_sites, char[][] node_diff_alleles, char[][] node_prev_alleles) {
    int[] diff_counts = count_diffs_per_site(node_diff_sites);
    allocate_site_arrays(diff_counts);
    int[] filled = new int[diff_counts.length];
    for (int node = 0; node < node_diff_sites.length; node++) {
      for (int i = 0; i < node_diff_sites[node].length; i++) {
        int site = node_diff_sites[node][i];
        int slot = filled[site]++;
        diff_nodes[site][slot] = node;
        curr_alleles[site][slot] = node_diff_alleles[node][i];
        prev_alleles[site][slot] = node_prev_alleles[node][i];
      }
    }
  }

  private int[] count_diffs_per_site(int[][] node_diff_sites) {
    int[] counts = new int[num_sites()];
    for (int[] sites : node_diff_sites) {
      for (int site : sites) counts[site]++;
    }
    return counts;
  }

  private void allocate_site_arrays(int[] diff_counts) {
    diff_nodes = new int[num_sites()][];
    curr_alleles = new char[num_sites()][];
    prev_alleles = new char[num_sites()][];
    for (int site = 0; site < num_sites(); site++) {
      diff_nodes[site] = new int[diff_counts[site]];
      curr_alleles[site] = new char[diff_counts[site]];
      prev_alleles[site] = new char[diff_counts[site]];
    }
  }

  private void parse_leaves(NewickTree tree) {
    this.leaf_names = new HashSet<>();
    List<NewickTreeNode> leaf_nodes = tree.getLeafNodes();
    for (NewickTreeNode node : leaf_nodes) {
      this.leaf_names.add(node.getId());
    }
  }

  private void parse_root(NewickTreeNode root, HashMap<String, char[]> seg_sites) {
    this.root_name = root.getId();
    this.root_seq = seg_sites.get(root_name);
    this.root_child_names = new HashSet<>();
    for (Object child : root.getChildren()) {
      this.root_child_names.add(((NewickTreeNode) child).getId());
    }
  }

  private void count_leaf_alleles(HashMap<String, char[]> seg_sites) {
    leaf_allele_counts = new int[num_sites()][ALLELE_INDEX.size()];
    for (String name : leaf_names) {
      count_alleles_of(seg_sites.get(name));
    }
  }

  private void count_alleles_of(char[] seq) {
    for (int site = 0; site < seq.length; site++) {
      Integer allele = ALLELE_INDEX.get(seq[site]);
      if (allele != null) leaf_allele_counts[site][allele]++;
    }
  }

  public int num_sites() {
    return root_seq.length;
  }

  public HashMap<Character, Integer> leaf_allele_counts(int site) {
    HashMap<Character, Integer> counts = new HashMap<>();
    for (Map.Entry<Character, Integer> entry : ALLELE_INDEX.entrySet()) {
      Integer count = leaf_allele_counts[site][entry.getValue()];
      if (count > 0) {
        counts.put(entry.getKey(), count);
      }
    }
    return counts;
  }

  public String root_name() {
    return root_name;
  }

  public char root_allele(int site) {
    return root_seq[site];
  }

  public record Diff(String node_name, char curr_allele, char prev_allele, boolean root_child) {}

  public List<Diff> diffs_at(int site) {
    List<Diff> diffs = new ArrayList<>();
    int[] site_node_diffs = diff_nodes[site];
    char[] site_curr_alleles = curr_alleles[site];
    char[] site_prev_alleles = prev_alleles[site];
    for (int i = 0; i < site_node_diffs.length; i++) {
      int node = site_node_diffs[i];
      String node_name = node_names[node];
      char curr_allele = site_curr_alleles[i];
      char prev_allele = site_prev_alleles[i];
      boolean root_child = root_child_names.contains(node_name);
      diffs.add(new Diff(node_name, curr_allele, prev_allele, root_child));
    }
    return diffs;
  }
}
