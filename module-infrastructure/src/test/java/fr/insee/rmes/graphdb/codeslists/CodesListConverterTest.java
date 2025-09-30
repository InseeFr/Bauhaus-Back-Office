package fr.insee.rmes.graphdb.codeslists;

import fr.insee.rmes.domain.codeslist.model.CodesListDomain;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CodesListConverterTest {

    @Test
    void shouldConvertInfrastructureToDomain() {
        CodesList codesList = new CodesList(
            "TEST001",
            "http://example.com/codelist/TEST001",
            "Test Label FR",
            "Test Label EN",
            "http://example.com/range"
        );
        
        CodesListDomain result = CodesListConverter.toDomain(codesList);
        
        assertNotNull(result);
        assertEquals("TEST001", result.getId());
        assertEquals("http://example.com/codelist/TEST001", result.getUri());
        assertEquals("Test Label FR", result.getLabelLg1());
        assertEquals("Test Label EN", result.getLabelLg2());
        assertEquals("http://example.com/range", result.getRange());
    }

    @Test
    void shouldConvertDomainToInfrastructure() {
        CodesListDomain codesListDomain = new CodesListDomain(
            "TEST001",
            "http://example.com/codelist/TEST001",
            "Test Label FR",
            "Test Label EN",
            "http://example.com/range"
        );
        
        CodesList result = CodesListConverter.toInfrastructure(codesListDomain);
        
        assertNotNull(result);
        assertEquals("TEST001", result.id());
        assertEquals("http://example.com/codelist/TEST001", result.uri());
        assertEquals("Test Label FR", result.labelLg1());
        assertEquals("Test Label EN", result.labelLg2());
        assertEquals("http://example.com/range", result.range());
    }

    @Test
    void shouldHandleNullInfrastructureModelToDomain() {
        CodesListDomain result = CodesListConverter.toDomain(null);
        
        assertNull(result);
    }

    @Test
    void shouldHandleNullDomainModelToInfrastructure() {
        CodesList result = CodesListConverter.toInfrastructure(null);
        
        assertNull(result);
    }

    @Test
    void shouldHandleNullFieldsInInfrastructureToDomain() {
        CodesList codesList = new CodesList(null, null, null, null, null);
        
        CodesListDomain result = CodesListConverter.toDomain(codesList);
        
        assertNotNull(result);
        assertNull(result.getId());
        assertNull(result.getUri());
        assertNull(result.getLabelLg1());
        assertNull(result.getLabelLg2());
        assertNull(result.getRange());
    }

    @Test
    void shouldHandleNullFieldsInDomainToInfrastructure() {
        CodesListDomain codesListDomain = new CodesListDomain(null, null, null, null, null);
        
        CodesList result = CodesListConverter.toInfrastructure(codesListDomain);
        
        assertNotNull(result);
        assertNull(result.id());
        assertNull(result.uri());
        assertNull(result.labelLg1());
        assertNull(result.labelLg2());
        assertNull(result.range());
    }

    @Test
    void shouldMaintainDataIntegrityInRoundTripConversion() {
        CodesList originalCodesList = new CodesList(
            "TEST001",
            "http://example.com/codelist/TEST001",
            "Test Label FR",
            "Test Label EN",
            "http://example.com/range"
        );
        
        CodesListDomain domain = CodesListConverter.toDomain(originalCodesList);
        CodesList convertedBack = CodesListConverter.toInfrastructure(domain);
        
        assertEquals(originalCodesList.id(), convertedBack.id());
        assertEquals(originalCodesList.uri(), convertedBack.uri());
        assertEquals(originalCodesList.labelLg1(), convertedBack.labelLg1());
        assertEquals(originalCodesList.labelLg2(), convertedBack.labelLg2());
        assertEquals(originalCodesList.range(), convertedBack.range());
    }

    @Test
    void shouldMaintainDataIntegrityInReversedRoundTripConversion() {
        CodesListDomain originalDomain = new CodesListDomain(
            "TEST001",
            "http://example.com/codelist/TEST001",
            "Test Label FR",
            "Test Label EN",
            "http://example.com/range"
        );
        
        CodesList infrastructure = CodesListConverter.toInfrastructure(originalDomain);
        CodesListDomain convertedBack = CodesListConverter.toDomain(infrastructure);
        
        assertEquals(originalDomain.getId(), convertedBack.getId());
        assertEquals(originalDomain.getUri(), convertedBack.getUri());
        assertEquals(originalDomain.getLabelLg1(), convertedBack.getLabelLg1());
        assertEquals(originalDomain.getLabelLg2(), convertedBack.getLabelLg2());
        assertEquals(originalDomain.getRange(), convertedBack.getRange());
    }

    @Test
    void shouldHandleEmptyStrings() {
        CodesList codesList = new CodesList("", "", "", "", "");
        
        CodesListDomain result = CodesListConverter.toDomain(codesList);
        
        assertNotNull(result);
        assertEquals("", result.getId());
        assertEquals("", result.getUri());
        assertEquals("", result.getLabelLg1());
        assertEquals("", result.getLabelLg2());
        assertEquals("", result.getRange());
    }

    @Test
    void shouldHandlePartiallyNullFields() {
        CodesList codesList = new CodesList(
            "TEST001",
            "http://example.com/codelist/TEST001",
            "Test Label FR",
            null,
            null
        );
        
        CodesListDomain result = CodesListConverter.toDomain(codesList);
        
        assertNotNull(result);
        assertEquals("TEST001", result.getId());
        assertEquals("http://example.com/codelist/TEST001", result.getUri());
        assertEquals("Test Label FR", result.getLabelLg1());
        assertNull(result.getLabelLg2());
        assertNull(result.getRange());
    }
}