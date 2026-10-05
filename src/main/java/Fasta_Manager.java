/*
 * Reconstructed from the old committed bytecode (compiled/Fasta_Manager.class, removed from the repository; last in git history
 * at commit d0d4a6c); the original source was never committed.
 * Written to be behavior-equivalent, including its quirks (noted below). Presumed to be part of POUTINE (GPL-3),
 * authorship to be confirmed with the previous maintainer: see docs/third-party-and-licensing.md.
 */

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

/**
 * Sequential reader for multi-FASTA files.  Each call to next() returns the next record, or null at end of file.
 *
 * Quirks preserved from the original:
 * - Errors are reported by printing and calling System.exit(-1), not by throwing.
 * - A record ends at the next line beginning with '>'.  To avoid consuming that line the reader is mark()ed and then
 *   reset(), with a read-ahead limit of 1000 characters, so a header line longer than that makes reset() fail.
 * - An empty line between records is appended to the sequence like any other line (zero length, so no effect).
 */
public class Fasta_Manager {

    static final int readAheadLimit = 1000;

    private String fastaFileName;
    private LineNumberReader lnr_fasta;
    private boolean EOF;


    public Fasta_Manager(String inputFileName) {
        EOF = false;
        fastaFileName = inputFileName;
        try {
            lnr_fasta = new LineNumberReader(new FileReader(fastaFileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }


    /**
     * @return the next record with its sequence lines concatenated as read (no trimming), or null at end of file
     */
    public Fasta_Record next() {
        Fasta_Record record = null;

        try {
            if (EOF) {
                return null;
            }

            String header = lnr_fasta.readLine();
            if (header == null) {
                return null;
            }

            if (header.equals("")) {
                System.out.println("\nThere is a bug in the code . . . the header line should never be an empty string!: line #" + lnr_fasta.getLineNumber() + "\n");
                System.exit(-1);
            } else if (header.charAt(0) != '>') {
                System.out.println("\nFirst line of Fasta file muust be a header line.");
                System.exit(-1);
            }

            lnr_fasta.mark(readAheadLimit);
            boolean EOR = false;  // end of record
            String sequence = "";
            String currLine = null;
            while (!EOR) {
                currLine = lnr_fasta.readLine();
                if (currLine == null) {  // end of file
                    EOF = true;
                    EOR = true;
                    record = new Fasta_Record(header, sequence);
                } else if (currLine.length() != 0 && currLine.charAt(0) == '>') {  // start of next record
                    EOR = true;
                    lnr_fasta.reset();
                    record = new Fasta_Record(header, sequence);
                } else {
                    sequence = sequence + currLine;
                    lnr_fasta.mark(readAheadLimit);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return record;
    }


    /**
     * Same as next(), except each sequence line is trimmed and followed by lineEnding before being appended.
     *
     * @param lineEnding appended after every (trimmed) sequence line
     */
    public Fasta_Record next(String lineEnding) {
        Fasta_Record record = null;

        try {
            if (EOF) {
                return null;
            }

            String header = lnr_fasta.readLine();
            if (header == null) {
                return null;
            }

            if (header.equals("")) {
                System.out.println("\nThere is a bug in the code . . . the header line should never be an empty string!: line #" + lnr_fasta.getLineNumber() + "\n");
                System.exit(-1);
            } else if (header.charAt(0) != '>') {
                System.out.println("\nFirst line of Fasta file muust be a header line.");
                System.exit(-1);
            }

            lnr_fasta.mark(readAheadLimit);
            boolean EOR = false;  // end of record
            String sequence = "";
            String currLine = null;
            while (!EOR) {
                currLine = lnr_fasta.readLine();
                if (currLine == null) {  // end of file
                    EOF = true;
                    EOR = true;
                    record = new Fasta_Record(header, sequence);
                } else if (currLine.length() != 0 && currLine.charAt(0) == '>') {  // start of next record
                    EOR = true;
                    lnr_fasta.reset();
                    record = new Fasta_Record(header, sequence);
                } else {
                    sequence = sequence + currLine.trim() + lineEnding;
                    lnr_fasta.mark(readAheadLimit);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return record;
    }


    /**
     * Writes the record as ">header" then the sequence on the next line.
     */
    public void writeRecord(BufferedWriter bw, Fasta_Record currRecord) {
        try {
            bw.write(">" + currRecord.getHeader());
            bw.newLine();
            bw.write(currRecord.getSequence());
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }


    public void cleanUp() {
        try {
            lnr_fasta.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
