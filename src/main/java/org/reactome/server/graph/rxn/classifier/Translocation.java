package org.reactome.server.graph.rxn.classifier;

import org.reactome.server.graph.rxn.common.RxnClassifier;

import java.util.Arrays;
import java.util.List;

/**
 * Within Reactome pathways, the unassisted movement of an entity between cellular compartments is nonetheless
 * regarded as an event. Translocation can be identified by comparing the compartment of molecular entities as
 * inputs with their compartment as an output. If the entity is unchanged but associated with a different
 * molecular compartment, this is a translocation event.
 *
 *          a TRANSLOCATES FROM [compartment x] TO [compartment y]
 *
 * A similar situation occurs when entities move between cells. An example is cytosolic retinol moving from
 * liver parenchymal cells to the cytosol of hepatic stellate cells for storage. In these circumstances, Reactome
 * can use the ‘cellType’ attribute in the event name. This leads to a CV name in the following style:
 *
 *          a TRANSLOCATES FROM [compartment x of cell type 1] TO [compartment x of cell type 2]
 *
 * Source: https://doi.org/10.1093/database/bau060
 *
 *
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@RxnClassifier
public class Translocation extends AbstractClassifier {

    @Override
    List<String> getHeader() {
        return Arrays.asList("Identifier", "Reaction", "Created", "Modified");
    }

    @Override
    public String getDescription() {
        return "a TRANSLOCATES FROM [compartment x] TO [compartment y]";
    }

    @Override
    String getQuery() {
        return " MATCH (ci:Compartment)<-[:compartment]-(i:PhysicalEntity)<-[ri:input]-(rle:ReactionLikeEvent)-[ro:output]->(o:PhysicalEntity)-[:compartment]->(co:Compartment) " +
                "WHERE NOT (rle)-[:catalystActivity]->() " +
                "WITH DISTINCT rle, COLLECT(i) AS is, COLLECT(DISTINCT ri) AS ris, COLLECT(o) AS os, COLLECT(DISTINCT ro) AS ros, " +
                "     COLLECT(i.schemaClass) AS isc, COLLECT(o.schemaClass) AS osc, COLLECT(ci) AS cis, COLLECT(co) AS cos " +
                "WITH rle, isc, osc, cis, cos, REDUCE(n=0, e IN ris | n + e.stoichiometry) AS inputs, REDUCE(n=0, e IN ros | n + e.stoichiometry) AS outputs " +
                "WHERE inputs=1 AND outputs=1 AND NONE(c IN cis WHERE c IN cos) AND ALL(sc IN isc WHERE sc IN osc) " +
                "OPTIONAL MATCH (a)-[:created]->(rle) " +
                "OPTIONAL MATCH (m)-[:modified]->(rle) " +
                "RETURN DISTINCT rle.stId AS Identifier, rle.displayName AS Reaction, a.displayName AS Created, m.displayName AS Modified " +
                "ORDER BY Created, Modified, Identifier";
    }
}
