
        /*// replace this section with code from my bioinformatics library to process fasta files: =======================================
        try (BufferedReader br = new BufferedReader(new FileReader(ancestral_reconstruction_filename))) {
            String currline;
            while ((currline = br.readLine()) != null) {
                if (currline.startsWith(">")) {
                    String node_name = currline.substring(1).trim();
                    node_names.add(node_name);
                } else {
                    char[] snps = currline.toCharArray();  // snps across all segsites for one sample/internal node
                    snp_sets.add(snps);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        for (int i = 0; i < node_names.size(); i++) {
            seg_sites.put(node_names.get(i), snp_sets.get(i));
        }  // ===========================================================================================================================*/

//            outputOptions.log.write(String.format("%n# samples detected from input genotype file: " + seg_sites.size() + "%n"));
//                outputOptions.log.write(String.format("%ngenotype file: # samples = " + seg_sites.size() + ", # segregating sites in each sample = " + num_segsites_detected + "%n"));

    /**
     * This method works with cfml version.
     *
     * Build a tree data structure from newick grammar
     */
    /*private NewickTree build_tree() {
        NewickTree tree = null;

        try (BufferedReader br = new BufferedReader(new FileReader(newick_filename))) {
            // read in a newick tree
            NewickTreeTokenizer tokenizer = new NewickTreeTokenizer(br);
            NewickTreeReader tree_reader = new NewickTreeReader(tokenizer);
            tree = tree_reader.readTree();
        } catch (IOException | DataFormatException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        // TODO:  refactor into log code
        System.out.println("phylogeny:  ");
        System.out.println("# of leaves = " + tree.getLeafNodes().size());
//        int num_children_of_rootnode = tree.getRoot().getChildrenCount();
//        System.out.println("num_children_of_rootnode = " + num_children_of_rootnode);
        System.out.println();

        return tree;
    }*/

/*    //    private void test_piping_fs_version() {
    private void test_p_to_p_version() {
//        ProcessBuilder pb = new ProcessBuilder("echo", "What would Snowden do? . . .");
        ProcessBuilder pb = new ProcessBuilder("Rscript", "--vanilla", "--verbose", "piping_test.R");

        try {
            Process p = pb.start();

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
            bw.write("10,1,1,10");
            bw.newLine();
            bw.write("60,0,10,60");
            bw.newLine();
            bw.write("50,8,5,49");
            bw.newLine();
            bw.close();

            int err_code = p.waitFor();
            System.out.println("err_code = " + err_code);

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String subprocess_output;
            while ((subprocess_output = br.readLine()) != null) {
                System.out.println("subprocess_output = " + subprocess_output);
            }

            System.out.println("\n\nEnd Test.\n\n");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }*/



/*
    private void test_jsc() {
//        int[][] test_site_homoplasy_counts = new int[2][2];
        int[][] test_site_homoplasy_counts = {{0,10}, {10,0}};
        ContingencyTable2x2 test_site_contingency_table = new ContingencyTable2x2(test_site_homoplasy_counts);
        FishersExactTest test_statistic = new FishersExactTest(test_site_contingency_table);

        System.out.println("test_site_contingency_table = " + test_site_contingency_table);
        System.out.println("test_statistic.getTestStatistic() = " + test_statistic.getTestStatistic());
        System.out.println("test_statistic.getOneTailedSP() = " + test_statistic.getOneTailedSP());
        System.out.println("test_statistic.getSP() = " + test_statistic.getSP());
        System.out.println("test_statistic.getOppositeTailProb() = " + test_statistic.getOppositeTailProb());

    }
*/


    // NOTE:  no longer need an explicit constructor since moving to picocli library
//    private Homoplasy_Counter(String[] args) {
////        if (args.length != 6) {
//        if (args.length != 5) {
//
//            System.out.println("\nUsage:  java Homoplasy_Counter . . . ");
//            System.exit(-1);
//        }
//
//        // TODO:  code in command-line switches
////        newick_filename = args[0];
//
////        nexus_filename = args[0];
////        ancestral_reconstruction_filename = args[1];
//
//        msa_fasta_filename = args[0];
//        user_input_newick_filename = args[1];
//
////        pos_cross_ref_filename = args[2];
//        pheno_filename = args[2];
//        map_filename = args[3];
////        recomb_tracts_filename = args[5];
//        qvalues_option = Boolean.parseBoolean(args[4]);
//    }

//    private String read_newick() {
//
//        String newick = null;
//
//        try (BufferedReader br = new BufferedReader(new FileReader(inputFiles.otherInputFiles.newick_filename))) {
//
//        } catch (IOException e) {
//            System.out.printf(Ansi.AUTO.string("@|red,bold %nAn error has occurred while reading the ancestral newick tree file. Please see the stack trace in the log file for more information on this error.%n|@"));
//            e.printStackTrace();
//            System.exit(-1);
//        }
//
//        return newick;
//    }



//  NOTE:  moved everything in main() to call()
//    public static void main(String[] args) {
//
//        Homoplasy_Counter homoplaser = new Homoplasy_Counter(args);
//
//        // PLINK .map file that contains physical positions in the order of the ped file used to create input files for CFML
//        int[] physical_poss = homoplaser.get_physical_positions();
//
//        /*  cfml version
//        // phylogeny reflecting ALL seg sites
//        NewickTree tree = homoplaser.build_tree();
//        */
//
//        /*
//         * ancestral recon pseudocode:
//         * - kick off process to start treetime <--- ancestral_reconstruction(). this should be it's own method without any other major tasks in it
//         *
//         * These methods should remain in main() as it is currently:
//         * - read in output file nexus          <--- nexus_to_newick()
//         * - read in output file msa fasta      <--- build_seg_sites()
//         * - build_tree(newick)
//         */
//        homoplaser.ancestral_reconstruction();
//
//        // parse treetime's annotated nexus file (with internal node's labelled) -> newick
//        String newick = homoplaser.nexus_to_newick();
//
//        // create tree data structure
//        NewickTree tree = homoplaser.build_tree(newick);
//
///*      // process cfml version of genotype ancestral reconstruction:
//        HashMap<String , char[]> seg_sites = homoplaser.build_seg_sites();
//        int[] segsite_indexes = homoplaser.get_segsite_indexes();
//        // sanity check for physical positions (i'm moving between map/ped <-> CFML position_cross_reference/ML_sequence.fasta worlds)
//        System.out.println("physical_poss.length = " + physical_poss.length);
//        System.out.println("segsite_indexes.length = " + segsite_indexes.length);  // this should = above length
////        System.out.println("seg_sites.size() = " + seg_sites.size());  // hmmm this returns # nodes (extant + ancestral) not # of alleles at each site
//*/
//
//        // process treetime's ancestral reconstruction fasta output file:
//        // read in ancestral genotypes
//        HashMap<String , char[]> seg_sites = homoplaser.build_seg_sites();
//        // output # segregating sites (map file and anc recon fasta file)
//        System.out.println("physical_poss.length = " + physical_poss.length);
////        seg_sites.values().iterator().next().length  // # segsites from treetime's ancestral fasta (in first HashMap entry only, should be same as rest of the entries)
//        // TODO:  rename build_seg_sites() -> read_ancestral_seqs()?  rename seg_sites -> ancestral_seqs?
//
//
//        // TODO:  refactor all ArrayLists into arrays[] since I know the sizes ahead of time?
//
//        HashMap<String, String> phenos = homoplaser.read_phenos();  // only extant phenotypes
//
//        homoplaser.log_session_info();
//
//        // homoplasy counts
////        ArrayList<Homoplasy_Events> all_events = homoplaser.count_all_homoplasy_events(tree, seg_sites, segsite_indexes, physical_poss);  // cfml version
//        ArrayList<Homoplasy_Events> all_events = homoplaser.count_all_homoplasy_events(tree, seg_sites, physical_poss);  // treetime version
//
//        // EXAMINE itol tree to think about effect sizes
////        homoplaser.output_segsite_MRCAs_and_clades_as_branch_colors(all_events, 1044);
////        homoplaser.output_segsite_events_as_branch_colors(all_events, 1044);
////        System.exit(-1);
//
//  /*    // assess magnitude of recombination
//        // DEBUG:  is all_events sorted by phys pos?
////        all_events.forEach(s -> System.out.println(s.segsite_ID + "\t" + s.physical_pos));
//        homoplaser.identify_recombinant_segsites(all_events, phenos);
//        System.exit(-1); // stop here for debugging
//        homoplaser.calc_magnitude_recomb(all_events);
//
//        // REMOVE RECOMBINANT REGIONS from all_events
//        homoplaser.remove_all_recombinant_regions(all_events);
//*/
//
//        homoplaser.assoc(all_events, phenos);
//
////        homoplaser.assess_significance(all_events, phenos);  // DEPRECATED
//
//        // Summary:  calculate some stats and explore both the homoplasy events and ancestral reconstructions
////        homoplaser.characterize(all_events, tree);
//
//        // DEBUG
////        homoplaser.debug(all_events, seg_sites, segsite_indexes);
//
//        // TEST: jsc library
////        homoplaser.test_jsc();
//
//        // (deprecated) test "filesystem" version of "piping" code between Java <-> R
////        homoplaser.test_piping_fs_version();
//
//        // TEST "process-to-process" version (java process <-> Rscript process)
////        homoplaser. test_p_to_p_version();
//
//        homoplaser.end_session();
//    }


    /**
     * This method uses cfml's recombinant tract file to identify all segsites that fall within any of these regions, regardless of which sample the recomb tract is
     * detected in.  Here, I'm focusing on segregating sites, not mutations within a specific sample.
     *
     * @param all_events
     * @param phenos
     * @return
     */
    /*private void identify_recombinant_segsites(ArrayList<Homoplasy_Events> all_events, HashMap<String, String> phenos) {
        // boolean[] recombinant_segsites = new boolean[all_events.size()];

        *//*
        for (recombinant region, 1 row from cfml recombinant tract file) {
            - mark all elements from all_events that are within this region
        }
        *//*

        // mark wich segsites are covered by at least 1 recomb tract in at least 1 sample from cfml recomb tract file
        try (BufferedReader br = new BufferedReader(new FileReader(recomb_tracts_filename))) {
            String currline;
            br.readLine();  // skip header
            while ((currline = br.readLine()) != null) {  // 3 cols (tab-delimited):  sample_ID, start recomb tract, end
                String[] cols = currline.split("\t");
                String sample_ID = cols[0];
                int tract_start = Integer.parseInt(cols[1]);
                int tract_end = Integer.parseInt(cols[2]);

                // only use recomb tracts from extant nodes
                if (!phenos.containsKey(sample_ID)) {
                    continue;
                }

//                all_events.stream().filter(s -> s.physical_pos >= tract_start && s.physical_pos <= tract_end).forEach(s -> s.isRecombinant = true);
                all_events.stream().filter(s -> s.segsite_ID >= tract_start && s.segsite_ID <= tract_end).forEach(s -> s.isRecombinant = true);


                // DEBUG
//                System.out.println("Node_ID\ttract_start\ttract_end");
                if (tract_start < 526 || tract_end < 526) {
                    System.out.println(sample_ID + "\t" + tract_start + "\t" + tract_end);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        // DEBUG:  did the above work?
        List<Homoplasy_Events> recombinant_sites = all_events.stream().filter(s -> s.isRecombinant == true).collect(Collectors.toList());
        System.out.println("\n\n\ntot # recombinant segregating sites = " + recombinant_sites.size());
        recombinant_sites.forEach(r -> System.out.println(r.physical_pos));



        // return list of recombinant sites as a subset of all_events?
    }*/

/*
    private ArrayList<Fishers_Exact_Statistic> assoc_fishers(ArrayList<Homoplasy_Events> all_events, HashMap<String,String> phenos) {

        // Fisher's exact:
        // only associate homoplasically non-monomorphic alleles (i.e. 0, 1, or 2 alleles may be homoplasically segregating)
        // here we also define "derived" alleles relative to the MRCA (not some outgroup)
        for (Homoplasy_Events segsite : all_events) {
            if (segsite.get_a1_count() >= 2 && segsite.get_a2_count() >= 2) {

            }
        }

        return null;
    }
*/

    /**
     * // This method is now DEPRECATED
     *
     * Only multi-hypo corrections likely to be used are q-values and maxT, but need to test ad-hoc "sub-family" idea which should go here
     *
     *
     * Assess statistical significance of each segregating site.  Any statistical test can be used for this assessment.  Currently, using:
     * - fisher's exact test (no multi-hypo correction, I could dervie q-values from distribution of fisher's exact p-values)
     * - maxT resampling (FWER correction)
     */
/*
    private void assess_significance(ArrayList<Homoplasy_Events> all_events, HashMap<String, String> phenos) {
        ArrayList<Fishers_Exact_Statistic> test_statistics = fishers_exact(all_events, phenos);

        // chisq
        // maxT
        // other methods to assess statistical significance at each segregating site

        // pvalues
        double[] pvalues = new double[test_statistics.size()];
        for (int i = 0; i < pvalues.length; i++) {
            pvalues[i] = test_statistics.get(i).getPvalue();
        }

         // DEBUG
//        for (double p : pvalues) {
//            System.out.println(p);
//        }

        // multi-hypo correction (for right now, always calculate qvalues, later may include option to choose a different method like a FWER)
        QSet qset = qvalues_R(pvalues);

        // results:  each row := 1 seg site
        output_qvalues(all_events, test_statistics, qset);

    }
*/

//    /**
//     * Counts geno-pheno state pairs, and interfaces w R process to calculate fisher's exact (two-sided) for all sites.
//     */
//    private ArrayList<Fishers_Exact_Statistic> fishers_exact(ArrayList<Homoplasy_Events> homoplasically_segregating_sites, HashMap<String, String> phenos) {
//
//        // TODO:  refactor count_phenos() into 2 methods:
//        /*
//        for (homoplasically segregating sites) {
//            count_homoplasies()
//            build_2x2()
//        }
//         */
//
//        ArrayList<int[][]> contingency_tables = count_phenos(homoplasically_segregating_sites, phenos);
//        ArrayList<Fishers_Exact_Statistic> test_statistics = fishers_exact_R(contingency_tables);
//
//        return test_statistics;
//    }

    /**
     * Process-to-process communication (java <-> Rscript)
     */
    /* 
    private ArrayList<Fishers_Exact_Statistic> fishers_exact_R(ArrayList<int[][]> contingency_tables) {
        ArrayList<Fishers_Exact_Statistic> test_statistics = new ArrayList<>();  // TODO:  pull out to fishers_exact()?

//        ProcessBuilder pb = new ProcessBuilder("Rscript", "--vanilla", "--verbose", "piping_test.R");
        ProcessBuilder pb = new ProcessBuilder("Rscript", "--vanilla", "--verbose", R_dir + "fishers_exact.R");

        // DEBUG
//        pb.redirectErrorStream(true);  // combine stderr + stdout

        try {
            Process R_process = pb.start();

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(R_process.getOutputStream()));

            // DEBUG
//            BufferedWriter bw = new BufferedWriter(new FileWriter("contingency_tables_as_rows.txt"));


            // 3 fake contingency tables for testing:
            bw.write("10,10,10,10");
            bw.newLine();
            bw.write("60,0,10,60");
            bw.newLine();
            bw.write("50,8,5,49");
            bw.newLine();

// COMMENTED OUT to allow a few fake contingency tables to be tested.  Code below is to process all seg sites:

            // pass to R process all contingency tables:  1 row representing 1 contingency table representing 1 segregating site
//            int cap = 1000000;  // DEBUG
//            int sites = 0;  // DEBUG
            for (int[][] curr_table : contingency_tables) {
                // TODO:  replace with StringBuilder?
                // row := a1p1, a1p2, a2p1, a2p2
                String table_as_row = String.valueOf(curr_table[0][0]) + "," +  // a1p1 := a1 case
                        String.valueOf(curr_table[0][1]) + "," +  // a1p2 := a1 control
                        String.valueOf(curr_table[1][0]) + "," +  // a2p1 := a2 case
                        String.valueOf(curr_table[1][1]);         // a2p2 := a2 control

                bw.write(table_as_row);
                bw.newLine();

                // DEBUG
//                sites++;
//                if (sites > cap) {
//                    break;
//                }
            }

            bw.close();

            // DEBUG
//            System.out.println("cap = " + cap);
            System.out.println("contingency_tables.size() = " + contingency_tables.size());

            // is waitFor() necessary?  Or go straight to blocking BufferedReader that waits on R process?  Ok, it's not necessary.
//            int err_code = R_process.waitFor();
//            boolean err_code = R_process.waitFor(1, TimeUnit.SECONDS);
//            System.out.println("err_code = " + err_code + "\n\n");


            // extracting results from R process:
            System.out.println("extracting results from R process");
            BufferedReader br = new BufferedReader(new InputStreamReader(R_process.getInputStream()));
            // DEBUG
            System.out.println("successfully opened input stream to R process");
            System.out.println("br.read() = " + br.ready());
            String R_process_output;
            String currline;
            StringBuilder sb = new StringBuilder(100000);  // set initial capacity to 100K chars to avoid fewer array expansions (expands at 2*current capacity)
            while ((currline = br.readLine()) != null) {  // read in ALL R output
//                System.out.println("R_process_output = " + R_process_output + "end_output");
//                R_process_output += currline + " ";
                sb.append(currline).append(" ");

                // DEBUG
//                System.out.println("currline = " + currline);
            }
            R_process_output = sb.toString();

            // parse R output
            String[] R_output = R_process_output.split(" ");

            // DEBUG
//            System.out.println("R_process_output = " + R_process_output);
            System.out.println("R_output.length = " + R_output.length);
//            for (String value : R_output) {
//                System.out.println("value = " + value);
//            }

            // DEBUG
//            int tot_num_sites = cap;  // for testing:  use 3 to test 3 contingency tables)
            int tot_num_sites = contingency_tables.size();  // i.e. tot # seg sites

            // inside R_output[]:
            // first s elements are pvalues (where s = tot # sites)
            // next s elements are ORs
            // next 2*s elements are the pairs of CIs for the ORs
            for (int pvalue_index = 0, or_index = tot_num_sites, ci_index = tot_num_sites * 2;
                 pvalue_index < tot_num_sites;
                 pvalue_index++, or_index++, ci_index += 2) {

                // add raw pheno counts as a row (same row passed into R).  contingency_tables and all_events are both in the same segsite order.
                int[][] curr_table = contingency_tables.get(pvalue_index);
                String counts_as_row = String.valueOf(curr_table[0][0]) + "," +
                        String.valueOf(curr_table[0][1]) + "," +
                        String.valueOf(curr_table[1][0]) + "," +
                        String.valueOf(curr_table[1][1]);

                // parse out pvalues from R output
                double p = Double.parseDouble(R_output[pvalue_index]);
                String or = R_output[or_index];
//                curr_stat.or_ci = R_output[j + tot_num_sites * 2] + "-" + R_output[j + tot_num_sites * 2 + 1];
                String or_ci = R_output[ci_index] + "-" + R_output[ci_index + 1];

                Fishers_Exact_Statistic curr_stat = new Fishers_Exact_Statistic(counts_as_row, p, or, or_ci);
                test_statistics.add(curr_stat);
            }

//            System.out.println("\n\nEnd Test.\n\n");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return test_statistics;
    }

/*    
    private QSet qvalues(ArrayList<Fishers_Exact_Statistic> test_statistics) {

        // get pvalues
        double[] pvalues = new double[test_statistics.size()];
        for (int i = 0; i < pvalues.length; i++) {
            pvalues[i] = test_statistics.get(i).getPvalue();
        }

        QSet qset = qvalues_R(pvalues);

//        output_qvalues(all_events, test_statistics, qset);

        return qset;
    } 
*/

    /**
     * Simply shows all the alleles at this ith site (1-based).
     *
     * I'm using this method to compare the alleles I pull out here by using CFML files against the alleles
     * I pull out from the map/ped world.  This check is necessary so that I make sure I am using the proper
     * physical positions from the input map file (remember, for some asinine reason CFML uses an additional
     * layer in the form of the position_cross_reference file to index into the ML_sequence.fasta file).
     */
    /*private void output_segsite_alleles(ArrayList<Homoplasy_Events> all_events, HashMap<String, char[]> seg_sites, int[] segsite_indexes, int i) {
        int segsite_index = segsite_indexes[i - 1] - 1;  // segsite_index is 0-based (while values in segsite_indexes[] from position_cross_references.txt are 1-based)
        HashMap<String, Character> mapped_snps = map_seg_site_to_tree(seg_sites, segsite_index);
        mapped_snps.forEach((k, v) -> System.out.println(k + "\t" + v));
    }*/

    // --Commented out by Inspection START (9/17/16, 8:20 PM):
//    /**
//     * Stats for 1 seg site.
//     */
//    private void gather_stats(HashMap<String, HashSet<String>> allele1_mrcAs, HashMap<String, HashSet<String>> allele2_mrcAs) {
//        int allele1_num_homoplasy_events = 0;
//        int allele2_num_homoplasy_events = 0;
//
//        if (allele1_mrcAs.size() > 1) {
//            allele1_num_homoplasy_events = allele1_mrcAs.size();
//        }
//
//        if (allele2_mrcAs.size() > 1) {
//            allele2_num_homoplasy_events = allele2_mrcAs.size();
//        }
//
////        System.out.print("# homoplasy events (allele1) = " + allele1_num_homoplasy_events);
////        System.out.println("\t# homoplasy events (allele2) = " + allele2_num_homoplasy_events);
//        System.out.println(allele1_num_homoplasy_events + "\t" + allele2_num_homoplasy_events);
//    }
// --Commented out by Inspection STOP (9/17/16, 8:20 PM)

// --Commented out by Inspection START (9/13/16, 6:22 PM):
//    /**
//     * Testing the coevolution API only.
//     */
//    private void test_newick_parser(String[] args) {
//
//        try (BufferedReader br = new BufferedReader(new FileReader(args[0]))) {
//            // read in a newick tree
//            NewickTreeTokenizer tokenizer = new NewickTreeTokenizer(br);
//            NewickTreeReader tree_reader = new NewickTreeReader(tokenizer);
//            NewickTree tree = tree_reader.readTree();
//
//            // spit the tree back out in newick txt format
//            String newick_as_string = tree.toString();
//            System.out.println(newick_as_string);
//        } catch (IOException | DataFormatException e) {
//            e.printStackTrace();
//        }
//    }
// --Commented out by Inspection STOP (9/13/16, 6:22 PM)


// --Commented out by Inspection START (9/13/16, 6:23 PM):
//    /**
//     * Testing the coevolution API only.
//     */
//    private void test_tree_methods(String[] args) {
//        NewickTree tree = null;
//
//        try (BufferedReader br = new BufferedReader(new FileReader(args[0]))) {
//            // read in a newick tree
//            NewickTreeTokenizer tokenizer = new NewickTreeTokenizer(br);
//            NewickTreeReader tree_reader = new NewickTreeReader(tokenizer);
//            tree = tree_reader.readTree();
//        } catch (IOException | DataFormatException e) {
//            e.printStackTrace();
//            System.exit(-1);
//        }
//
//        // test some tree methods:
//        NewickTreeNode root = tree.getRoot();
//        System.out.println("\nroot: " + root.getId());
//
//        // verify # of children of root node:
//        System.out.println("\nverifying # children of root node:");
//        System.out.println("root.getChildrenCount() = " + root.getChildrenCount());
//        ArrayList children = (ArrayList) root.getChildren();
//        System.out.println("children.size() = " + children.size());
//
//        // verify children node names:
//        System.out.println("\nverifying node names of children of root directly with ArrayList:");
//        for (int i = 0; i < children.size(); i++) {
//            NewickTreeNode curr_child_node =  (NewickTreeNode) children.get(i);
//            System.out.println("curr_child_node.getId() = " + curr_child_node.getId());
//        }
//
//        // verify children node names using API's iterator method:
//        System.out.println("\nverifying node names of children of root node using API's iterator:");
//        Iterator it = root.getChildrenIterator();
//        while (it.hasNext()) {
//            NewickTreeNode curr_child_node = (NewickTreeNode) it.next();
//            System.out.println("curr_child_node.getId() = " + curr_child_node.getId());
//        }
//
//        // verify leaf method
//        System.out.println("\nverifying leaf nodes:");
//        ArrayList leaves = (ArrayList) tree.getLeafNodes();
//        System.out.println("leaves.size() = " + leaves.size());
//        System.out.println("leaves = " + leaves);
//
//
//    }
// --Commented out by Inspection STOP (9/13/16, 6:23 PM)
