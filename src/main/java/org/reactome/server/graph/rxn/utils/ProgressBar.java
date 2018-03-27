package org.reactome.server.graph.rxn.utils;

/**
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
public class ProgressBar {

    private static final int width = 70;

    /**
     * Simple method that prints a progress bar to command line
     *
     * @param done Number of entries added to the graph
     */
    public static void updateProgressBar(String current, int done, int total) {
        current = (total == done) ? "" : " [current:" + current + "]";

        String format = "\r\t%3d%% %s %c%s";
        char[] rotators = {'|', '/', '—', '\\'};
        double percent = (double) done / total;
        StringBuilder progress = new StringBuilder(width);
        progress.append('|');
        int i = 0;
        for (; i < (int) (percent * width); i++) progress.append("=");
        for (; i < width; i++) progress.append(" ");
        progress.append('|');
        System.out.printf(format, (int) (percent * 100), progress, rotators[done % rotators.length], current);
    }

    public static void done(int total) {
        updateProgressBar("", total, total);
    }

}
