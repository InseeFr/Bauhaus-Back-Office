package fr.insee.rmes.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringUtilsTest {

    @Test
    void stringToList() {
        List<String> response = StringUtils.stringToList("Bauhaus-Back");
        List<String> model = List.of("Bauhaus-Back");
        assertEquals(model,response);
    }

    @Test
    void convertHtmlStringToRaw() {
        String html= "<html><head> <title> Bauhaus HTML Page </title> <meta charset=\"utf-8\" /> </head> <body> <h1> A title of <p> Bauhaus </p> Application </h1> <p> An example. </p> <h2>Level of title  2</h2> <p> A new link to a website  <a href=\"https://github.com/inseefr\">bauhaus-back-office.fr</a>.</p></body> </html>";
        String result = StringUtils.convertHtmlStringToRaw(html);
        String expected =   "Bauhaus HTML Page      A title of Bauhaus  Application An example.  Level of title  2 A new link to a website bauhaus-back-office.fr.";
        String resultWhithoutSpaces = result.replaceAll("\\s", "");
        String expectedWhithoutSpaces = expected.replaceAll("\\s", "");
        assertEquals(expectedWhithoutSpaces,resultWhithoutSpaces);
    }

}