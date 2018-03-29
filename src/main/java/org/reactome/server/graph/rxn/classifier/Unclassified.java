package org.reactome.server.graph.rxn.classifier;

import org.reactome.server.graph.rxn.common.RxnPostClassifier;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reactions in the classification target that have not been classified
 *
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@SuppressWarnings("unused")
@RxnPostClassifier
public class Unclassified extends AbstractClassifier {

    @Override
    List<String> getHeader() {
        return Arrays.asList("Identifier", "Reaction", "Created", "Modified");
    }

    @Override
    public String getDescription() {
        return "RXN-Classifier could not classify these reactions";
    }

    @Override
    protected Map getQueryParameters() {
        Map<String, Object> rtn = new HashMap<>();
        rtn.put("classified", AbstractClassifier.classified.keySet());
        return rtn;
    }

    @Override
    String getQuery() {
        return " MATCH (rle:ReactionLikeEvent) " +
                "WHERE NOT(rle:BlackBoxEvent) AND NOT (rle)-[:catalystActivity]->() AND NOT rle.stId IN {classified} " +
                "OPTIONAL MATCH (a)-[:created]->(rle) " +
                "OPTIONAL MATCH (m)-[:modified]->(rle) " +
                "RETURN DISTINCT rle.stId AS Identifier, rle.displayName AS Reaction, a.displayName AS Created, m.displayName AS Modified " +
                "ORDER BY Created, Modified, Identifier";
    }
}
