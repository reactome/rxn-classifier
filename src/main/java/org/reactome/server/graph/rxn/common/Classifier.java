package org.reactome.server.graph.rxn.common;

import org.reactome.server.graph.service.GeneralService;

public interface Classifier {

    String getName();

    String getDescription();

    int run(GeneralService genericService, String path);

}
