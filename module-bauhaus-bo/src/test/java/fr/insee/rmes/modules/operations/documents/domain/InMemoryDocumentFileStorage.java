package fr.insee.rmes.modules.operations.documents.domain;

import fr.insee.rmes.modules.operations.documents.domain.port.serverside.DocumentFileStorage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

/** Faux stockage en mémoire : un fichier par nom, désigné par l'URL {@code file:///gestion/<nom>}. */
public final class InMemoryDocumentFileStorage implements DocumentFileStorage {

    public static final String URL_PREFIX = "file:///gestion/";

    private final Map<String, byte[]> files = new HashMap<>();

    public void clear() {
        files.clear();
    }

    public void put(String fileName, String content) {
        files.put(fileName, content.getBytes());
    }

    public String content(String fileName) {
        return new String(files.get(fileName));
    }

    @Override
    public boolean exists(String fileName) {
        return files.containsKey(fileName);
    }

    @Override
    public String write(String fileName, InputStream content) {
        try {
            files.put(fileName, content.readAllBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return URL_PREFIX + fileName;
    }

    @Override
    public InputStream read(String fileName) {
        return new ByteArrayInputStream(files.get(fileName));
    }

    @Override
    public void delete(String fileName) {
        files.remove(fileName);
    }

    @Override
    public String fileNameOf(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }
}
