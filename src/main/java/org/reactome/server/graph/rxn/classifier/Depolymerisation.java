package org.reactome.server.graph.rxn.classifier;

import org.reactome.server.graph.rxn.common.RxnClassifier;

import java.util.Arrays;
import java.util.List;

/**
 * Polymerization can be seen as a specialized form of a binding event. Polymerization events have their own reaction
 * class in the Reactome data-model and are therefore easy to identify.
 *
 *          x DEPOLYMERIZES TO a (,b and c)
 *
 * Source: https://doi.org/10.1093/database/bau060
 *
 * Note: It was suggested to also exclude reactions with catalyst activity in this classifier
 *
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@RxnClassifier
public class Depolymerisation extends AbstractClassifier {

    @Override
    List<String> getHeader() {
        return Arrays.asList("Identifier", "Reaction", "Created", "Modified");
    }

    @Override
    public String getDescription() {
        return "x DEPOLYMERIZES TO a (,b and c)";
    }

    @Override
    String getQuery() {
        return " MATCH (rle:Depolymerisation) " +
                "OPTIONAL MATCH (a)-[:created]->(rle) " +
                "OPTIONAL MATCH (m)-[:modified]->(rle) " +
                "RETURN DISTINCT rle.stId AS Identifier, rle.displayName AS Reaction, a.displayName AS Created, m.displayName AS Modified " +
                "ORDER BY Created, Modified, Identifier";
    }
}
