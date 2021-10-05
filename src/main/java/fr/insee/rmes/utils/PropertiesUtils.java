
package fr.insee.rmes.utils;

import javax.validation.constraints.NotNull;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

public class PropertiesUtils {
	
	  private PropertiesUtils() {
		    throw new IllegalStateException("Utility class");
	}


    private static final Properties propsForReadPropertyFromFile=new Properties();

    public static Optional<String> readPropertyFromPath(@NotNull String propertyName, @NotNull Path filePath){
        propsForReadPropertyFromFile.remove(propertyName);
        // Use OS charset : for example windows-1252 for windows, UTF-8 for linux, ...
        // switch to java 11 to specify charset
        try(Reader readerFromFile=new FileReader(filePath.toFile())){
            propsForReadPropertyFromFile.load(readerFromFile);
        }catch (IOException e){
            //propsForReadPropertyFromFile remain empty if file cannot be read
        }
        return Optional.ofNullable(propsForReadPropertyFromFile.getProperty(propertyName));
    }

}
