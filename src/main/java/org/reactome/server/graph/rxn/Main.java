
package org.reactome.server.graph.rxn;

import com.martiansoftware.jsap.*;
import org.reactome.server.graph.exception.CustomQueryException;
import org.reactome.server.graph.rxn.classifier.AbstractClassifier;
import org.reactome.server.graph.rxn.common.Classifier;
import org.reactome.server.graph.rxn.common.RxnClassifier;
import org.reactome.server.graph.rxn.config.ReactomeNeo4jConfig;
import org.reactome.server.graph.rxn.utils.FileUtils;
import org.reactome.server.graph.rxn.utils.ProgressBar;
import org.reactome.server.graph.rxn.utils.Report;
import org.reactome.server.graph.service.AdvancedDatabaseObjectService;
import org.reactome.server.graph.service.GeneralService;
import org.reactome.server.graph.utils.ReactomeGraphCore;
import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
public class Main {

    private static Boolean VERBOSE;

    private static GeneralService genericService;
    private static AdvancedDatabaseObjectService ados;
    private static List<Class<?>> sortedClassifiers;
    private static String path;

    public static void main(String[] args) throws JSAPException {

        SimpleJSAP jsap = new SimpleJSAP(Main.class.getName(), "A tool for assisting the reaction classification in Reactome",
                new Parameter[]{
                        new FlaggedOption(  "host",       JSAP.STRING_PARSER,"localhost",       JSAP.REQUIRED,     'h', "host",      "The neo4j host"          ),
                        new FlaggedOption(  "port",       JSAP.STRING_PARSER,"7474",            JSAP.NOT_REQUIRED, 'b', "port",      "The neo4j port"          ),
                        new FlaggedOption(  "user",       JSAP.STRING_PARSER,"neo4j",           JSAP.REQUIRED,     'u', "user",      "The neo4j user"          ),
                        new FlaggedOption(  "password",   JSAP.STRING_PARSER,"reactome",        JSAP.REQUIRED,     'p', "password",  "The neo4j password"      ),
                        new FlaggedOption(  "output",     JSAP.STRING_PARSER,"./reports",       JSAP.REQUIRED,     'o', "output",    "Output folder"           ),
                        new FlaggedOption(  "classifier", JSAP.STRING_PARSER,"all",             JSAP.NOT_REQUIRED, 'c', "classifier","A specific classifier"   ),
                        new QualifiedSwitch("verbose",    JSAP.BOOLEAN_PARSER,   JSAP.NO_DEFAULT,   JSAP.NOT_REQUIRED, 'v', "verbose",   "Requests verbose output" )
                }
        );
        JSAPResult config = jsap.parse(args);
        if (jsap.messagePrinted()) System.exit(1);

        initialise(config);

        final Long start = System.currentTimeMillis();
        String classifier = config.getString("classifier").toLowerCase();
        List<Report> reports = classifier.equals("all") ? runAllClassifiers() : runSingleClassifier(classifier);
        AbstractClassifier.reportClassifications(path, "Classifier_Aggregation_v" + genericService.getDBVersion());
        //Reports have to be stored and printed in the screen (when VERBOSE)
        storeReports(path, "Classifier_Summary_v" + genericService.getDBVersion(), reports);
        final Long time = System.currentTimeMillis() - start;

        printReports(reports, time);
    }

    private static List<Report> runAllClassifiers() {
        List<Report> reports = new ArrayList<>();
        if (VERBOSE) System.out.println();
        int n = sortedClassifiers.size(), i = 1;
        for (Class test : sortedClassifiers) {
            try {
                Object object = test.newInstance();
                Classifier classifier = (Classifier) object;
                if (VERBOSE) ProgressBar.updateProgressBar(classifier.getName(), i++, n);
                int result = classifier.run(genericService, path);
                reports.add(new Report(classifier, result));
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return reports;
    }

    private static List<Report> runSingleClassifier(String target) {
        List<Report> reports = new ArrayList<>();
        Class clazz = sortedClassifiers.stream().filter(t -> t.getSimpleName().contains(target)).findFirst().orElse(null);
        if (clazz != null) {
            try {
                Object object = clazz.newInstance();
                Classifier classifier = (Classifier) object;
                if (VERBOSE) System.out.print("\nRunning single classifier '" + classifier.getName() + "'...");
                int result = classifier.run(genericService, path);
                reports.add(new Report(classifier, result));
                if (VERBOSE) System.out.print(" Done.");
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("\nThe classifier '" + target + "' cannot be found. Please check the name and try again.");
            System.exit(1);
        }
        return reports;
    }

    private static void printReports(List<Report> reports, long time) {
        if (!VERBOSE) return;
        //Printing the reports sorted by name
        System.out.println(String.format("\n\n· Report%s:", reports.size() > 1 ? "s" : ""));
        reports.stream().sorted().forEach(Report::printColoured);

        long total = getTotalReactions();
        long target = getTargetedReactions();
        long classified = AbstractClassifier.classifications.keySet().size();
        System.out.println(String.format(
                "\n· Summary:\n\tTotal: %,d reactions\n\tTarget: %,d reactions (BBE without catalyst activity are excluded).\n\tClassified: %,d reactions\n\tPercentages: %2.0f%% of the target | %2.0f%% of the total",
                total, target, classified, classified / (double) target * 100d, classified / (double) total * 100d)
        );

        long c = reports.stream().filter(r -> r.count > 0).count();
        System.out.println(String.format("\nReaction Classifier finished. %s classifier%s generated reports (%s)", c, c == 1 ? "" : "s", getTimeFormatted(time)));

    }

    private static void storeReports(String path, String fileName, List<Report> reports) {
        if (reports.isEmpty()) return;
        List<String> lines = new ArrayList<>();
        lines.add(Report.getCSVHeader());
        reports.stream().sorted().forEach(r -> lines.add(r.getCSV()));

        try {
            Files.write(FileUtils.getFilePath(path, fileName), lines, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int getTargetedReactions(){
        try {
            String query = "" +
                    "MATCH (rle:ReactionLikeEvent) " +
                    "WHERE NOT (rle:BlackBoxEvent) OR (rle)-[:catalystActivity]->() " +
                    "RETURN COUNT(DISTINCT rle)";
            return ados.customNumbernQueryResult(query, Collections.emptyMap()).intValue();
        } catch (CustomQueryException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int getTotalReactions(){
        try {
            String query = "MATCH (rle:ReactionLikeEvent) RETURN COUNT(DISTINCT rle)";
            return ados.customNumbernQueryResult(query, Collections.emptyMap()).intValue();
        } catch (CustomQueryException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static void initialise(JSAPResult config) {
        VERBOSE = config.getBoolean("verbose");

        //Initialising ReactomeCore Neo4j configuration
        ReactomeGraphCore.initialise(
                config.getString("host"),
                config.getString("port"),
                config.getString("user"),
                config.getString("password"),
                ReactomeNeo4jConfig.class
        );
        //ReactomeGraphCore has to be initialised before services can be instantiated
        genericService = ReactomeGraphCore.getService(GeneralService.class);
        ados = ReactomeGraphCore.getService(AdvancedDatabaseObjectService.class);

        Reflections reflections = new Reflections(AbstractClassifier.class.getPackage().getName());
        Set<Class<?>> tests = reflections.getTypesAnnotatedWith(RxnClassifier.class);
        sortedClassifiers = tests.stream().filter(c -> c.getAnnotation(Deprecated.class) == null)
                .sorted(Comparator.comparing(Class::getSimpleName)) // Sorting tests by name
                .collect(Collectors.toList());

        if (VERBOSE) {  //Report test initialisation
            System.out.println("· Reaction Classifier initialisation:");
            System.out.print("\t>Initialising classifiers to be performed...");
            long t = tests.size();
            System.out.println(String.format("\r\t> %d classifier%s found: ", t, t == 1 ? "" : "s"));
            long r = sortedClassifiers.size();
            System.out.println(String.format("\t\t-%3d classifier%s active", r, r == 1 ? "" : "s"));
            long d = tests.stream().filter(c -> c.getAnnotation(Deprecated.class) != null).count();
            System.out.println(String.format("\t\t-%3d classifier%s excluded ('@Deprecated')", d, d == 1 ? "" : "s"));
        }

        //Cleans up previous reports
        path = config.getString("output");
        if (!path.endsWith("/")) path += "/";

        try {
            org.apache.commons.io.FileUtils.cleanDirectory(new File(path));
            if (VERBOSE) System.out.println("\t> Reports folder cleanup -> (done)");
        } catch (Exception e) {
            System.out.println("\t> No reports folder found -> it will be created.");
        }
    }

    private static String getTimeFormatted(Long millis) {
        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }
}