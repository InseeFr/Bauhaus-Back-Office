package fr.insee.rmes.modules.commons.domain.model;

import java.net.URI;
import java.nio.file.Path;

public record Document(String path, String name) {
    public String getFullPath(){
        return path + "/" + name;
    }

    public static Document fromUri(URI uri){
        var path = Path.of(uri);
        return new Document(path.getParent().toString(), path.getFileName().toString());
    }
}
