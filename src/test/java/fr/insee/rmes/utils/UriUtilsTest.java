package fr.insee.rmes.utils;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static fr.insee.rmes.utils.UriUtils.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UriUtilsTest {

    @Test
    void shouldValidateUrl() {
        List<String> urlTested = List.of("https://github.com","http://github.com","https://github.com//fr","http://[::FFFF:129.144.52.38]:80/index.html","http://http://","https://\\\\");
        List<Boolean> urlType = new ArrayList<>();
        urlTested.forEach(element -> urlType.add(isValiURL(element)));
        List<Boolean> urlResponse = urlType.stream().distinct().toList();
        assertTrue(urlResponse.getFirst() && urlResponse.size()==1);
    }

    @Test
     void shouldnotValidateUrl(){
        List<String> urlTested = List.of("https:/github.com","test://github.com","https://","http://]","http://|","http://[","http:// 1","http://test:-2/index.html","mytest","http://]a]","http://]a[","http://urnhjh]a[","http://urnhjh[test45[");
        List<Boolean> urlType = new ArrayList<>();
        urlTested.forEach(element -> urlType.add(isValiURL(element)));
        List<Boolean> urlResponse = urlType.stream().distinct().toList();
        assertTrue(!urlResponse.getFirst() && urlResponse.size()==1);
    }


    @Test
    void shouldValidateUrn(){
        List<String> urnTested = List.of("urn:test:test","urn:test:");
        List<Boolean> urnType = new ArrayList<>();
        urnTested.forEach(element -> urnType.add(isValiURN(element)));
        List<Boolean> urnResponse = urnType.stream().distinct().toList();
        assertTrue(urnResponse.getFirst() && urnResponse.size()==1);
    }

    @Test
    void shouldnotValidateUrn(){
        List<String> urnTested = List.of("test:test:test","urn:test","urn : :","urn","urn: 1:2","urn: : ");
        List<Boolean> urnType = new ArrayList<>();
        urnTested.forEach(element -> urnType.add(isValiURN(element)));
        List<Boolean> urnResponse = urnType.stream().distinct().toList();
        assertTrue(!urnResponse.getFirst() && urnResponse.size()==1);
    }

    @Test
    void shouldValidateUri(){
        List<String> uriTested = List.of("urn:test:test","test:test:test");
        List<Boolean> uriType = new ArrayList<>();
        uriTested.forEach(element -> uriType.add(isValiURI(element)));
        List<Boolean> uriResponse = uriType.stream().distinct().toList();
        assertTrue(uriResponse.getFirst() && uriResponse.size()==1);
    }

    @Test
    void shouldnotValidateUri(){
        List<String> uriTested = List.of("I am an invalid URI string.","I am an invalid URI string !");
        List<Boolean> uriType = new ArrayList<>();
        uriTested.forEach(element -> uriType.add(isValiURI(element)));
        List<Boolean> uriResponse = uriType.stream().distinct().toList();
        assertTrue(!uriResponse.getFirst() && uriResponse.size()==1);
    }



}