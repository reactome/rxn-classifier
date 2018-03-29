package org.reactome.server.graph.rxn.classifier;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.ogm.model.Result;
import org.reactome.server.graph.rxn.common.Classifier;
import org.reactome.server.graph.rxn.common.RxnClassifier;
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

    public static MapSet<String, String> classified = new MapSet<>();
    public static Set<String> unclassified = new HashSet<>();

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
            if(getClass().getAnnotation(RxnClassifier.class)!=null) {
                classified.add(stId, getName());
            } else {
                unclassified.add(stId);
            }

            List<String> line = new ArrayList<>();
            for (String attribute : getHeader()) {
                line.add(String.format("\"%s\"", map.get(attribute)));
            }
            lines.add(StringUtils.join(line, ","));
        }
        Files.write(path, lines, Charset.forName("UTF-8"));
        return lines.size() - 1;
    }
}
