import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import org.gersteinlab.coevolution.core.data.NewickTree;
import org.gersteinlab.coevolution.core.data.NewickTreeNode;

/**
 * Ancestral sequences stored as differences from each node's parent, instead of one full sequence
 * per node. Only the root keeps absolute alleles.
 *
 * <p>Nodes are numbered in preorder, so a parent always comes before its children and the root is
 * node 0. The diffs are grouped by site: the diffs of site s are the entries from site_start[s]
 * up to (not including) site_start[s + 1] of diff_node and diff_allele.
 */
public class Sequence_Diffs {

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
  public Sequence_Diffs(HashMap<String, char[]> seg_sites, NewickTree tree) {
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

  public int num_diffs() {
    return diff_node.length;
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
