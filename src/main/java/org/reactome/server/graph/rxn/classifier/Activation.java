package org.reactome.server.graph.rxn.classifier;

import org.reactome.server.graph.rxn.common.RxnClassifier;

import java.util.Arrays;
import java.util.List;

/**
 * In Reactome, activation is used only to specify a conformational event that does not otherwise involve covalent
 * modification or transport. Activation reactions can be identified as events where the input and output entities
 * are the same with no change of compartment.
 *
 *          a is(are) activated
 *
 * Source: https://doi.org/10.1093/database/bau060
 *
 *
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@RxnClassifier
public class Activation extends AbstractClassifier {

    @Override
    List<String> getHeader() {
        return Arrays.asList("Identifier", "Reaction", "Created", "Modified");
    }

    @Override
    public String getDescription() {
        return "a is(are) activated";
    }

    @Override
    String getQuery() {
        return " MATCH (i:PhysicalEntity)<-[ri:input]-(rle:ReactionLikeEvent)-[ro:output]->(o) " +
                "WHERE NOT (rle:BlackBoxEvent) AND NOT (rle)-[:catalystActivity]->() " +
                "WITH DISTINCT rle, COLLECT(DISTINCT ri) AS ris, COLLECT(o) AS os, COLLECT(DISTINCT ro) AS ros " +
                "WITH DISTINCT rle, REDUCE(n=0, e IN ris | n + e.stoichiometry) AS inputs, REDUCE(n=0, e IN ros | n + e.stoichiometry) AS outputs " +
                "WHERE inputs=1 AND outputs=1 " +
                "MATCH (i)<-[:input]-(rle)-[:output]->(o), " +
                "      (i)-[:compartment]->(c:Compartment)<-[:compartment]-(o), " +
                "      (i)-[:referenceEntity]->(re:ReferenceGeneProduct)<-[:referenceEntity]-(o) " +
                "OPTIONAL MATCH (i)-[:hasModifiedResidue]->(imr:AbstractModifiedResidue) " +
                "OPTIONAL MATCH (o)-[:hasModifiedResidue]->(omr:AbstractModifiedResidue) " +
                "WITH rle, i, COLLECT(imr) AS imrs, o, COLLECT(omr) AS omrs       " +
                "WHERE ALL(mr IN imrs WHERE mr IN omrs) AND ALL(mr IN omrs WHERE mr IN imrs) " +
                "      AND (i.startCoordinate IS NULL OR i.startCoordinate = o.startCoordinate) " +
                "      AND (o.endCoordinate IS NULL OR i.endCoordinate = o.endCoordinate) " +
                "OPTIONAL MATCH (a)-[:created]->(rle) " +
                "OPTIONAL MATCH (m)-[:modified]->(rle) " +
                "RETURN DISTINCT rle.stId AS Identifier, rle.displayName AS Reaction, a.displayName AS Created, m.displayName AS Modified " +
                "ORDER BY Created, Modified, Identifier";
    }
}
