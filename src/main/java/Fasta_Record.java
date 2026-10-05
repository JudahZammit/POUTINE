/*
 * Reconstructed from the committed bytecode (compiled/Fasta_Record.class); the original source was never committed.
 * Written to be behavior-equivalent, including its quirks (see getHeader()). Presumed to be part of POUTINE (GPL-3),
 * authorship to be confirmed with the previous maintainer: see docs/third-party-and-licensing.md.
 */

/**
 * One record of a FASTA file: the header line and the (single-string) sequence.
 */
public class Fasta_Record {

    private String header;  // as read from the file, including the leading '>'
    private String sequence;
    private int seqLen;


    public Fasta_Record(String header, String sequence) {
        this.header = header;
        this.sequence = sequence;
        if (sequence == null) {
            this.seqLen = 0;
        } else {
            this.seqLen = sequence.length();
        }
    }


    /**
     * @return the header without its leading character (the '>') and with surrounding whitespace trimmed.
     *         Note that the first character is dropped unconditionally.
     */
    public String getHeader() {
        return header.substring(1).trim();
    }


    public String getSequence() {
        return sequence;
    }


    public int getSeqLen() {
        return seqLen;
    }


    public void setSeqLen(int seqLen) {
        this.seqLen = seqLen;
    }


    /**
     * Prints the record (raw header, including the '>') and sequence to standard output.
     */
    public void write_record() {
        System.out.println(header);
        System.out.println(sequence);
    }
}
