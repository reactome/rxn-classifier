package org.reactome.server.graph.rxn.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
public class FileUtils {

    public static Path getFilePath(String path, String fileName) throws IOException {
        Path p = Paths.get(path + fileName + ".csv");
        Files.deleteIfExists(p);
        if (!Files.isSymbolicLink(p.getParent())) Files.createDirectories(p.getParent());
        Files.createFile(p);
        return p;
    }
}
