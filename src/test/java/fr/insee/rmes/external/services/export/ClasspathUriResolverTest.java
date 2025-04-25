package fr.insee.rmes.external.services.export;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import javax.xml.transform.TransformerException;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClasspathUriResolverTest {

    @ParameterizedTest
    @ValueSource(strings = { "..this is an example of href---this an example of base","../..application---Bauhaus","Bauhaus---online","Bauhaus back!!=565**/*/- office---online","!!!!!!!!!!!!!!!!!!!---***********","123458569---45454897987"})
    void resolve(String description) throws TransformerException {

        ClasspathUriResolver classpathUriResolver = new ClasspathUriResolver();
        String [] details = description.split("---");
        String source = classpathUriResolver.resolve(details[0],details[1]).toString();

        int suffixLength = source.split("@")[1].length();
        boolean prefixContent = source.startsWith("javax.xml.transform.stream.StreamSource");

        assertTrue(prefixContent && suffixLength<=8);
    }
}