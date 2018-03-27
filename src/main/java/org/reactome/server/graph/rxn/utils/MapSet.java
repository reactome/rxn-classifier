package org.reactome.server.graph.rxn.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
public class MapSet<S, T> {

    private Map<S, Set<T>> mapset = new HashMap<>();

    public void add(S s, T t){
        mapset.computeIfAbsent(s, k -> new HashSet<>()).add(t);
    }

    public Set<T> get(S s){
        return mapset.get(s);
    }

    public Set<S> keySet(){
        return mapset.keySet();
    }
}
