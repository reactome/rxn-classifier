package org.reactome.server.graph.rxn.classifier;

import org.reactome.server.graph.rxn.common.RxnClassifier;

import java.util.Arrays;
import java.util.List;

/**
 * Transport that requires a transporter uses a different phrasing. The extra requirement is that there is a protein
 * entity named as catalyst. The style for naming this class of events is shown below:
 *
 *          Protein x TRANSPORTS a ( FROM compartment x TO compartment y )
 *
 * Source: https://doi.org/10.1093/database/bau060
 *
 *
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@RxnClassifier
//@Deprecated //This is now taken into account in MolecularCatalyst
public class TranslocationMediated extends AbstractClassifier {

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
        return " MATCH (ci:Compartment)<-[:compartment]-(i:PhysicalEntity)<-[ri:input]-(rle:ReactionLikeEvent), " +
                "      (rle)-[ro:output]->(o:PhysicalEntity)-[:compartment]->(co:Compartment) " +
                "WHERE NOT(rle:BlackBoxEvent) AND (rle)-[:catalystActivity]->() " +
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
