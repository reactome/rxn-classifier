package org.reactome.server.graph.rxn.classifier;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.ogm.model.Result;
import org.reactome.server.graph.rxn.common.RxnClassifier;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * Names for events with a catalyst specify the catalyst name (x). If the catalyst has an associated GO molecular
 * function (GOMF), this may allow the use of a derived functional term, as explained below; otherwise, the name
 * is structured as shown in this example:
 *
 *          x CATALYZES a (,b and c) TO d (,e and f)
 *
 * In Reactome, all catalyst entities have an associated GOMF term. The GOMF term can be used to derive or abstract
 * a verb that can be used as part of the event name.
 *
 * The paper explains that the current GOMF to verb table is available online
 * https://docs.google.com/a/ebi.ac.uk/spreadsheet/ccc?key = 0AnYqRvZI4xkedDJoR1JPMXhqX 1RYRnVYemMy Zm M4cWc&usp=sharing#gid=0
 * and new GOMF to verb mappings will be added to this table as required. When that happens, the file in this project
 * 'GOMF_lookuptable.tsv' needs to be updated.
 *
 * Source: https://doi.org/10.1093/database/bau060
 *
 *
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@SuppressWarnings("unused")
@RxnClassifier
public class MolecularCatalyst extends AbstractClassifier {

    private Map<String, String> gomfTranslation = new HashMap<>();

    @Override
    List<String> getHeader() {
        return Arrays.asList("Identifier", "Reaction", "Type", "Created", "Modified");
    }

    @Override
    public String getDescription() {
        return "x CATALYZES a (,b and c) TO d (,e and f)";
    }

    public MolecularCatalyst() {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(Objects.requireNonNull(classLoader.getResource("GOMF_lookuptable.tsv")).getFile());
            Stream<String> stream = Files.lines(file.toPath());
            stream.forEach(line -> {
                String[] cols = line.split("[\t ]");
                gomfTranslation.put(cols[0], cols[1]);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    int report(Result result, Path path) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("\"" + StringUtils.join(getHeader(), "\",\"") + "\"");
        for (Map<String, Object> map : result) {
            String stId = String.valueOf(map.get(getHeader().get(0)));
            List<String> line = new ArrayList<>();
            for (String attribute : getHeader()) {
                String aux = String.valueOf(map.get(attribute));
                if(attribute.equals("Type")){
                    String translate = gomfTranslation.get(aux);
                    aux = (translate == null || translate.isEmpty()) ? "GO:" + aux : StringUtils.capitalize(translate);
                    classified.add(stId, aux);
                }
                line.add(String.format("\"%s\"", aux));
            }
            lines.add(StringUtils.join(line, ","));
        }
        Files.write(path, lines, Charset.forName("UTF-8"));
        return lines.size() - 1;
    }

    @Override
    String getQuery() {
        return " MATCH (rle:ReactionLikeEvent)-[:catalystActivity]->(:CatalystActivity)-[:activity]->(go:GO_MolecularFunction) " +
                "OPTIONAL MATCH (a)-[:created]->(rle) " +
                "OPTIONAL MATCH (m)-[:modified]->(rle) " +
                "RETURN rle.stId AS Identifier, rle.displayName AS Reaction, go.accession AS Type, a.displayName AS Created, m.displayName AS Modified " +
                "ORDER BY Created, Modified, Identifier";
    }

}
