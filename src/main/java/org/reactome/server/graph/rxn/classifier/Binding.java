package org.reactome.server.graph.rxn.classifier;

import org.reactome.server.graph.rxn.common.RxnClassifier;

import java.util.Arrays;
import java.util.List;

/**
 * If there are more input entities than output entities, the event is classed as a binding event.
 * The test applied to determine binding can also be useful for verifying stoichiometric balance;
 * at least one input entity should become a component of (one of) the output entities, or one of
 * the input entities should include more entities as an output than it did as an input.
 * The naming structure for binding events is shown below:
 *
 *          a BINDS b forming c
 *
 * Source: https://doi.org/10.1093/database/bau060
 *
 *
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@RxnClassifier
public class Binding extends AbstractClassifier {

    @Override
    List<String> getHeader() {
        return Arrays.asList("Identifier", "Reaction", "Inputs", "Outputs", "Created", "Modified");
    }

    @Override
    public String getDescription() {
        return "a BINDS b forming c";
    }

    @Override
    String getQuery() {
        return " MATCH (i:PhysicalEntity)<-[ri:input]-(rle:ReactionLikeEvent)-[ro:output]->(o:PhysicalEntity) " +
                "WHERE NOT(rle:BlackBoxEvent) " +
                "WITH DISTINCT rle, COLLECT(i) AS is, COLLECT(DISTINCT ri) AS ris, COLLECT(o) AS os, COLLECT(DISTINCT ro) AS ros " +
                "WITH rle, REDUCE(n=0, e IN ris | n + e.stoichiometry) AS inputs, REDUCE(n=0, e IN ros | n + e.stoichiometry) AS outputs " +
                "WHERE ANY(pe IN is WHERE (rle)-[:output]-(:Complex)-[:hasComponent]->(pe)) " +
                "OPTIONAL MATCH (a)-[:created]->(rle) " +
                "OPTIONAL MATCH (m)-[:modified]->(rle) " +
                "RETURN DISTINCT rle.stId AS Identifier, rle.displayName AS Reaction, inputs AS Inputs, outputs AS Outputs, a.displayName AS Created, m.displayName AS Modified " +
                "ORDER BY Created, Modified, Identifier";
    }
}
