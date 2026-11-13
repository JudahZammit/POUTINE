import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
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

/**
 * Ancestral sequences stored as differences from each node's parent, instead of one full sequence
 * per node. Only the root keeps absolute alleles.
 *
 * <p>Nodes are numbered in preorder, so a parent always comes before its children and the root is
 * node 0. The diffs are grouped by site: the diffs of site s are the entries from site_start[s] up
 * to (not including) site_start[s + 1] of diff_node and diff_allele.
 */
class Sequence_Diffs_old {

  private final HashMap<String, Integer> node_index = new HashMap<>(); // name -> preorder index
  private final String[] node_names;
  private final int[] parent_index; // -1 for the root
  private final boolean[] is_leaf;

  private final char[] root_alleles; // one per site
  private final int[] site_start; // length = # sites + 1
  private final int[] diff_node; // preorder index of a node whose allele differs from its parent
  private final char[] diff_allele; // that node's allele

  /**
   * Converts absolute sequences (key := node name) into diffs against each node's parent. The
   * sequences are only read, so the caller can drop them afterwards.
   */
  public Sequence_Diffs_old(HashMap<String, char[]> seg_sites, NewickTree tree) {
    List<NewickTreeNode> nodes = preorder(tree.getRoot());
    index_nodes(nodes);
    node_names = nodes.stream().map(NewickTreeNode::getId).toArray(String[]::new);
    parent_index = find_parents(nodes);
    is_leaf = find_leaves(nodes);

    char[][] seqs = sequences_in_order(seg_sites);
    root_alleles = seqs[0].clone();
    site_start = find_site_starts(seqs);
    diff_node = new int[site_start[root_alleles.length]];
    diff_allele = new char[diff_node.length];
    for (int s = 0; s < root_alleles.length; s++) fill_diffs_at(seqs, s);
  }

  /** The nodes of the tree, parents before children (iterative: trees can be deep). */
  private static List<NewickTreeNode> preorder(NewickTreeNode root) {
    List<NewickTreeNode> order = new ArrayList<>();
    Deque<NewickTreeNode> stack = new ArrayDeque<>();
    stack.push(root);
    while (!stack.isEmpty()) {
      NewickTreeNode node = stack.pop();
      order.add(node);
      for (Object child : node.getChildren()) stack.push((NewickTreeNode) child);
    }
    return order;
  }

  private void index_nodes(List<NewickTreeNode> nodes) {
    for (int i = 0; i < nodes.size(); i++) node_index.put(nodes.get(i).getId(), i);
  }

  private int[] find_parents(List<NewickTreeNode> nodes) {
    int[] parents = new int[nodes.size()];
    parents[0] = -1;
    for (int i = 1; i < parents.length; i++) {
      parents[i] = node_index.get(nodes.get(i).getParent().getId());
    }
    return parents;
  }

  private static boolean[] find_leaves(List<NewickTreeNode> nodes) {
    boolean[] leaves = new boolean[nodes.size()];
    for (int i = 0; i < leaves.length; i++) leaves[i] = nodes.get(i).getChildrenCount() == 0;
    return leaves;
  }

  /** Each node's absolute sequence, in preorder. Every tree node must have one. */
  private char[][] sequences_in_order(HashMap<String, char[]> seg_sites) {
    char[][] seqs = new char[node_names.length][];
    for (int i = 0; i < seqs.length; i++) seqs[i] = sequence_of(seg_sites, node_names[i]);
    for (char[] seq : seqs) check_same_length(seq, seqs[0]);
    return seqs;
  }

  private static char[] sequence_of(HashMap<String, char[]> seg_sites, String node_name) {
    char[] seq = seg_sites.get(node_name);
    if (seq == null) throw new IllegalArgumentException("no sequence for tree node " + node_name);
    return seq;
  }

  private static void check_same_length(char[] seq, char[] root_seq) {
    if (seq.length != root_seq.length) {
      throw new IllegalArgumentException("ancestral sequences differ in length");
    }
  }

  private boolean differs(char[][] seqs, int site, int node) {
    return seqs[node][site] != seqs[parent_index[node]][site];
  }

  private int count_diffs_at(char[][] seqs, int site) {
    int count = 0;
    for (int n = 1; n < seqs.length; n++) if (differs(seqs, site, n)) count++;
    return count;
  }

  private int[] find_site_starts(char[][] seqs) {
    int[] starts = new int[seqs[0].length + 1];
    for (int s = 0; s < seqs[0].length; s++) starts[s + 1] = starts[s] + count_diffs_at(seqs, s);
    return starts;
  }

  private void fill_diffs_at(char[][] seqs, int site) {
    int next = site_start[site];
    for (int n = 1; n < seqs.length; n++) {
      if (!differs(seqs, site, n)) continue;
      diff_node[next] = n;
      diff_allele[next++] = seqs[n][site];
    }
  }

  public int num_sites() {
    return root_alleles.length;
  }

  /** # leaves with each allele at this site. Missing data is counted like any other allele. */
  public HashMap<Character, Integer> leaf_allele_counts(int site) {
    char[] alleles = alleles_at(site);
    HashMap<Character, Integer> counts = new HashMap<>();
    for (int n = 0; n < alleles.length; n++) {
      if (is_leaf[n]) counts.merge(alleles[n], 1, Integer::sum);
    }
    return counts;
  }

  /** Every node's allele at this site: a node has its diff's allele, else its parent's. */
  private char[] alleles_at(int site) {
    char[] alleles = new char[node_names.length];
    alleles[0] = root_alleles[site];
    int next = site_start[site]; // the diffs of a site are in preorder, like the nodes
    for (int n = 1; n < alleles.length; n++) {
      boolean differs = next < site_start[site + 1] && diff_node[next] == n;
      alleles[n] = differs ? diff_allele[next++] : alleles[parent_index[n]];
    }
    return alleles;
  }

  /** A node whose allele differs from its parent's. */
  public record Diff(String node_name, char allele, boolean on_root_child_edge) {}

  public String root_name() {
    return node_names[0];
  }

  public char root_allele(int site) {
    return root_alleles[site];
  }

  public List<Diff> diffs_at(int site) {
    List<Diff> diffs = new ArrayList<>();
    for (int i = site_start[site]; i < site_start[site + 1]; i++) diffs.add(diff(i));
    return diffs;
  }

  private Diff diff(int i) {
    int node = diff_node[i];
    return new Diff(node_names[node], diff_allele[i], parent_index[node] == 0);
  }
}
