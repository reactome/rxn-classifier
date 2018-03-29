package org.reactome.server.graph.rxn.classifier;

import org.reactome.server.graph.rxn.common.RxnClassifier;

import java.util.Arrays;
import java.util.List;

/**
 * If more output molecules are present than inputs, the event is a dissociation event. This can be tested by
 * determining that at least one of the output entities is a component of an input entity or that an output
 * complex reduces in size (to distinguish this class from certain transformation reactions). The naming
 * structure for dissociation events is shown below:
 *
 *          c DISSOCIATES TO a AND b
 *
 * Source: https://doi.org/10.1093/database/bau060
 *
 *
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@RxnClassifier
public class Dissociation extends AbstractClassifier {

    @Override
    List<String> getHeader() {
        return Arrays.asList("Identifier", "Reaction", "Created", "Modified");
    }

    @Override
    public String getDescription() {
        return "c DISSOCIATES TO a AND b";
    }

    @Override
    String getQuery() {
        return " MATCH (i:PhysicalEntity)<-[ri:input]-(rle:ReactionLikeEvent)-[ro:output]->(o:PhysicalEntity) " +
                "WHERE NOT(rle:BlackBoxEvent) AND NOT (rle)-[:catalystActivity]->() " +
                "WITH DISTINCT rle, COLLECT(i) AS is, COLLECT(DISTINCT ri) AS ris, COLLECT(o) AS os, COLLECT(DISTINCT ro) AS ros  " +
                "WITH rle, REDUCE(n=0, e IN ris | n + e.stoichiometry) AS inputs, REDUCE(n=0, e IN ros | n + e.stoichiometry) AS outputs  " +
                "WHERE ANY(pe IN os WHERE (rle)-[:input]-(:Complex)-[:hasComponent]->(pe)) " +
                "OPTIONAL MATCH (a)-[:created]->(rle) " +
                "OPTIONAL MATCH (m)-[:modified]->(rle) " +
                "RETURN DISTINCT rle.stId AS Identifier, rle.displayName AS Reaction, a.displayName AS Created, m.displayName AS Modified " +
                "ORDER BY Created, Modified, Identifier";
    }
}
