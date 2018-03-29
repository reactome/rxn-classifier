package org.reactome.server.graph.rxn.classifier;

import org.reactome.server.graph.rxn.common.RxnPostClassifier;

import java.util.Arrays;
import java.util.List;

/**
 * Reactions out of the classification target
 *
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@SuppressWarnings("unused")
@RxnPostClassifier
public class Omitted extends AbstractClassifier {

    @Override
    List<String> getHeader() {
        return Arrays.asList("Identifier", "Reaction", "Created", "Modified");
    }

    @Override
    public String getDescription() {
        return "RXN-Classifier does not take them into account (BBE without catalyst activity)";
    }

    @Override
    String getQuery() {
        return " MATCH (rle:BlackBoxEvent) " +
                "WHERE NOT (rle)-[:catalystActivity]->() " +
                "OPTIONAL MATCH (a)-[:created]->(rle) " +
                "OPTIONAL MATCH (m)-[:modified]->(rle) " +
                "RETURN DISTINCT rle.stId AS Identifier, rle.displayName AS Reaction, a.displayName AS Created, m.displayName AS Modified " +
                "ORDER BY Created, Modified, Identifier";
    }
}
