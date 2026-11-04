import java.io.BufferedWriter;
import java.io.File;

/** Output paths and open writers resolved for this run (user values: Output_Parameters). */
class Output_Options {
  File output_dir; // default is relative to the current dir
  File output_file;
  File output_file_sorted_by_a1_maxT;
  File output_file_sorted_by_a2_maxT;
  BufferedWriter out;
  BufferedWriter out_sorted_by_a1_maxT;
  BufferedWriter out_sorted_by_a2_maxT;
  File log_file;
  BufferedWriter log;
  File anc_recon_dir; // default is a subdirectory of output_dir
  BufferedWriter debug;
}
