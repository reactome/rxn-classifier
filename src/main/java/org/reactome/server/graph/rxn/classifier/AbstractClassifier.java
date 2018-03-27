package org.reactome.server.graph.rxn.classifier;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.ogm.model.Result;
import org.reactome.server.graph.rxn.common.Classifier;
import org.reactome.server.graph.rxn.utils.FileUtils;
import org.reactome.server.graph.rxn.utils.MapSet;
import org.reactome.server.graph.service.GeneralService;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
public abstract class AbstractClassifier implements Classifier {

    public static MapSet<String, String> classifications = new MapSet<>();

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @SuppressWarnings("WeakerAccess")
    protected Map getQueryParameters() {
        return Collections.EMPTY_MAP;
    }

    abstract List<String> getHeader();

    abstract String getQuery();

    @SuppressWarnings("unchecked")
    @Override
    public int run(GeneralService genericService, String path) {
        Result result = genericService.query(getQuery(), getQueryParameters());
        if (result == null || !result.iterator().hasNext()) return 0;
        try {
            return report(result, FileUtils.getFilePath(path, getName()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    int report(Result result, Path path) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("\"" + StringUtils.join(getHeader(), "\",\"") + "\"");
        for (Map<String, Object> map : result) {
            String stId = String.valueOf(map.get(getHeader().get(0)));
            classifications.add(stId, getName());

            List<String> line = new ArrayList<>();
            for (String attribute : getHeader()) {
                // Some results might be list of elements. In some cases we use REDUCE and the output looks like
                // ["a", "b", "c", ] and we want it to look like ["a", "b", "c"].
                //               ^ we remove this comma and the space after it
                // That's why we replace ", ]" by "]"
                Object aux = map.get(attribute);
                if(aux instanceof Object[]){
                    StringBuilder rtn = new StringBuilder("[");
                    for (Object item : (Object[]) aux) {
                        rtn.append(item).append(", ");
                    }
                    aux = rtn.append("]").toString();
                }
                line.add("\"" + (aux == null ? null : ("" + aux).replaceAll(", ]$", "]")) + "\"");
            }
            lines.add(StringUtils.join(line, ","));
        }
        Files.write(path, lines, Charset.forName("UTF-8"));
        return lines.size() - 1;
    }

    public static void reportClassifications(String path, String name){
        List<String> lines = new ArrayList<>();
        for (String stId : classifications.keySet()) {
            Set<String> classification = classifications.get(stId);
            lines.add(String.format("%s,%d,\"%s\"", stId, classification.size(),StringUtils.join(classification, "\",\"")));
        }

        try {
            Files.write(FileUtils.getFilePath(path, name), lines, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
