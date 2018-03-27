package org.reactome.server.graph.rxn.utils;

import org.reactome.server.graph.rxn.common.Classifier;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
public class Report implements Comparable<Report> {

    private static NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.UK);

    private Classifier classifier;
    public final Integer count;

    public Report(Classifier classifier, Integer count) {
        this.classifier = classifier;
        this.count = count;
    }

    public static void printColoured(Report p) {
        String entries = (p.count == 1) ? "entry" : "entries";
        String line = String.format("\t%s: %s %s", p.classifier.getName(), numberFormat.format(p.count), entries);
        System.out.println(line);
    }

    public static String getCSVHeader(){
        return "Name,Entries,Description";
    }

    public String getCSV() {
        return String.format("\"%s\",%d,\"%s\"",
                classifier.getName(),
                count,
                classifier.getDescription()
        );
    }

    @Override
    public int compareTo(Report o) {
        return (classifier.getName().compareTo(o.classifier.getName()));
    }
}
