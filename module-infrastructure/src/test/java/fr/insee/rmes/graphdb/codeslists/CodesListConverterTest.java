package fr.insee.rmes.graphdb.codeslists;

import fr.insee.rmes.domain.codeslist.model.CodesListDomain;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class CodesListConverterTest {

    @Test
    void shouldConvertInfrastructureToDomain() {
        CodesList codesList = new CodesList(
            "http://example.com/codelist/TEST001",
            "TEST001",
            "Test Label FR",
            "Test Label EN",
            "Description FR",
            "Description EN",
            "http://example.com/range",
            "LAST001",
            "2023-01-01",
            "creator1",
            "VALIDATED",
            "PUBLIC",
            "2023-01-02",
            "http://example.com/parent",
            Collections.emptyList()
        );
        
        CodesListDomain result = CodesListConverter.toDomain(codesList);
        
        assertNotNull(result);
        assertEquals("TEST001", result.getId());
        assertEquals("http://example.com/codelist/TEST001", result.getUri());
        assertEquals("Test Label FR", result.getLabelLg1());
        assertEquals("Test Label EN", result.getLabelLg2());
        assertEquals("Description FR", result.getDescriptionLg1());
        assertEquals("Description EN", result.getDescriptionLg2());
        assertEquals("http://example.com/range", result.getRange());
        assertEquals("LAST001", result.getLastCodeUriSegment());
        assertEquals("2023-01-01", result.getCreated());
        assertEquals("creator1", result.getCreator());
        assertEquals("VALIDATED", result.getValidationState());
        assertEquals("PUBLIC", result.getDisseminationStatus());
        assertEquals("2023-01-02", result.getModified());
        assertEquals("http://example.com/parent", result.getIriParent());
    }

    @Test
    void shouldConvertDomainToInfrastructure() {
        CodesListDomain codesListDomain = new CodesListDomain(
            "http://example.com/codelist/TEST001",
            "TEST001",
            "Test Label FR",
            "Test Label EN",
            "Description FR",
            "Description EN",
            "http://example.com/range",
            "LAST001",
            "2023-01-01",
            "creator1",
            "VALIDATED",
            "PUBLIC",
            "2023-01-02",
            "http://example.com/parent",
            Collections.emptyList()
        );
        
        CodesList result = CodesListConverter.toInfrastructure(codesListDomain);
        
        assertNotNull(result);
        assertEquals("http://example.com/codelist/TEST001", result.uri());
        assertEquals("TEST001", result.id());
        assertEquals("Test Label FR", result.labelLg1());
        assertEquals("Test Label EN", result.labelLg2());
        assertEquals("Description FR", result.descriptionLg1());
        assertEquals("Description EN", result.descriptionLg2());
        assertEquals("http://example.com/range", result.range());
        assertEquals("LAST001", result.lastCodeUriSegment());
        assertEquals("2023-01-01", result.created());
        assertEquals("creator1", result.creator());
        assertEquals("VALIDATED", result.validationState());
        assertEquals("PUBLIC", result.disseminationStatus());
        assertEquals("2023-01-02", result.modified());
        assertEquals("http://example.com/parent", result.iriParent());
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
        CodesList codesList = new CodesList(null, null, null, null, null, null, null, null, null, null, null, null, null, null, Collections.emptyList());
        
        CodesListDomain result = CodesListConverter.toDomain(codesList);
        
        assertNotNull(result);
        assertNull(result.getUri());
        assertNull(result.getId());
        assertNull(result.getLabelLg1());
        assertNull(result.getLabelLg2());
        assertNull(result.getDescriptionLg1());
        assertNull(result.getDescriptionLg2());
        assertNull(result.getRange());
        assertNull(result.getLastCodeUriSegment());
        assertNull(result.getCreated());
        assertNull(result.getCreator());
        assertNull(result.getValidationState());
        assertNull(result.getDisseminationStatus());
        assertNull(result.getModified());
        assertNull(result.getIriParent());
    }

    @Test
    void shouldHandleNullFieldsInDomainToInfrastructure() {
        CodesListDomain codesListDomain = new CodesListDomain(null, null, null, null, null, null, null, null, null, null, null, null, null, null, Collections.emptyList());
        
        CodesList result = CodesListConverter.toInfrastructure(codesListDomain);
        
        assertNotNull(result);
        assertNull(result.uri());
        assertNull(result.id());
        assertNull(result.labelLg1());
        assertNull(result.labelLg2());
        assertNull(result.descriptionLg1());
        assertNull(result.descriptionLg2());
        assertNull(result.range());
        assertNull(result.lastCodeUriSegment());
        assertNull(result.created());
        assertNull(result.creator());
        assertNull(result.validationState());
        assertNull(result.disseminationStatus());
        assertNull(result.modified());
        assertNull(result.iriParent());
    }

    @Test
    void shouldMaintainDataIntegrityInRoundTripConversion() {
        CodesList originalCodesList = new CodesList(
            "http://example.com/codelist/TEST001",
            "TEST001",
            "Test Label FR",
            "Test Label EN",
            "Description FR",
            "Description EN",
            "http://example.com/range",
            "LAST001",
            "2023-01-01",
            "creator1",
            "VALIDATED",
            "PUBLIC",
            "2023-01-02",
            "http://example.com/parent",
            Collections.emptyList()
        );
        
        CodesListDomain domain = CodesListConverter.toDomain(originalCodesList);
        CodesList convertedBack = CodesListConverter.toInfrastructure(domain);
        
        assertEquals(originalCodesList.uri(), convertedBack.uri());
        assertEquals(originalCodesList.id(), convertedBack.id());
        assertEquals(originalCodesList.labelLg1(), convertedBack.labelLg1());
        assertEquals(originalCodesList.labelLg2(), convertedBack.labelLg2());
        assertEquals(originalCodesList.descriptionLg1(), convertedBack.descriptionLg1());
        assertEquals(originalCodesList.descriptionLg2(), convertedBack.descriptionLg2());
        assertEquals(originalCodesList.range(), convertedBack.range());
        assertEquals(originalCodesList.lastCodeUriSegment(), convertedBack.lastCodeUriSegment());
        assertEquals(originalCodesList.created(), convertedBack.created());
        assertEquals(originalCodesList.creator(), convertedBack.creator());
        assertEquals(originalCodesList.validationState(), convertedBack.validationState());
        assertEquals(originalCodesList.disseminationStatus(), convertedBack.disseminationStatus());
        assertEquals(originalCodesList.modified(), convertedBack.modified());
        assertEquals(originalCodesList.iriParent(), convertedBack.iriParent());
    }

    @Test
    void shouldMaintainDataIntegrityInReversedRoundTripConversion() {
        CodesListDomain originalDomain = new CodesListDomain(
            "http://example.com/codelist/TEST001",
            "TEST001",
            "Test Label FR",
            "Test Label EN",
            "Description FR",
            "Description EN",
            "http://example.com/range",
            "LAST001",
            "2023-01-01",
            "creator1",
            "VALIDATED",
            "PUBLIC",
            "2023-01-02",
            "http://example.com/parent",
            Collections.emptyList()
        );
        
        CodesList infrastructure = CodesListConverter.toInfrastructure(originalDomain);
        CodesListDomain convertedBack = CodesListConverter.toDomain(infrastructure);
        
        assertEquals(originalDomain.getUri(), convertedBack.getUri());
        assertEquals(originalDomain.getId(), convertedBack.getId());
        assertEquals(originalDomain.getLabelLg1(), convertedBack.getLabelLg1());
        assertEquals(originalDomain.getLabelLg2(), convertedBack.getLabelLg2());
        assertEquals(originalDomain.getDescriptionLg1(), convertedBack.getDescriptionLg1());
        assertEquals(originalDomain.getDescriptionLg2(), convertedBack.getDescriptionLg2());
        assertEquals(originalDomain.getRange(), convertedBack.getRange());
        assertEquals(originalDomain.getLastCodeUriSegment(), convertedBack.getLastCodeUriSegment());
        assertEquals(originalDomain.getCreated(), convertedBack.getCreated());
        assertEquals(originalDomain.getCreator(), convertedBack.getCreator());
        assertEquals(originalDomain.getValidationState(), convertedBack.getValidationState());
        assertEquals(originalDomain.getDisseminationStatus(), convertedBack.getDisseminationStatus());
        assertEquals(originalDomain.getModified(), convertedBack.getModified());
        assertEquals(originalDomain.getIriParent(), convertedBack.getIriParent());
    }

    @Test
    void shouldHandleEmptyStrings() {
        CodesList codesList = new CodesList("", "", "", "", "", "", "", "", "", "", "", "", "", "", Collections.emptyList());
        
        CodesListDomain result = CodesListConverter.toDomain(codesList);
        
        assertNotNull(result);
        assertEquals("", result.getUri());
        assertEquals("", result.getId());
        assertEquals("", result.getLabelLg1());
        assertEquals("", result.getLabelLg2());
        assertEquals("", result.getDescriptionLg1());
        assertEquals("", result.getDescriptionLg2());
        assertEquals("", result.getRange());
        assertEquals("", result.getLastCodeUriSegment());
        assertEquals("", result.getCreated());
        assertEquals("", result.getCreator());
        assertEquals("", result.getValidationState());
        assertEquals("", result.getDisseminationStatus());
        assertEquals("", result.getModified());
        assertEquals("", result.getIriParent());
    }

    @Test
    void shouldHandlePartiallyNullFields() {
        CodesList codesList = new CodesList(
            "http://example.com/codelist/TEST001",
            "TEST001",
            "Test Label FR",
            null,
            "Description FR",
            null,
            null,
            "LAST001",
            null,
            "creator1",
            null,
            "PUBLIC",
            null,
            null,
            Collections.emptyList()
        );
        
        CodesListDomain result = CodesListConverter.toDomain(codesList);
        
        assertNotNull(result);
        assertEquals("http://example.com/codelist/TEST001", result.getUri());
        assertEquals("TEST001", result.getId());
        assertEquals("Test Label FR", result.getLabelLg1());
        assertNull(result.getLabelLg2());
        assertEquals("Description FR", result.getDescriptionLg1());
        assertNull(result.getDescriptionLg2());
        assertNull(result.getRange());
        assertEquals("LAST001", result.getLastCodeUriSegment());
        assertNull(result.getCreated());
        assertEquals("creator1", result.getCreator());
        assertNull(result.getValidationState());
        assertEquals("PUBLIC", result.getDisseminationStatus());
        assertNull(result.getModified());
        assertNull(result.getIriParent());
    }

    @Test
    void shouldConvertNewFieldsCorrectly() {
        CodesList codesList = new CodesList(
            "http://example.com/codelist/TEST001",
            "TEST001",
            "Test Label FR",
            "Test Label EN", 
            "Description in French",
            "Description in English",
            "http://example.com/range",
            "LAST_SEGMENT_001",
            "2023-01-01T10:00:00Z",
            "john.doe",
            "Validated",
            "PublicGeneric",
            "2023-01-15T15:30:00Z",
            "http://example.com/parent/scheme",
            Collections.emptyList()
        );
        
        CodesListDomain result = CodesListConverter.toDomain(codesList);
        
        assertNotNull(result);
        assertEquals("Description in French", result.getDescriptionLg1());
        assertEquals("Description in English", result.getDescriptionLg2());
        assertEquals("LAST_SEGMENT_001", result.getLastCodeUriSegment());
        assertEquals("2023-01-01T10:00:00Z", result.getCreated());
        assertEquals("john.doe", result.getCreator());
        assertEquals("Validated", result.getValidationState());
        assertEquals("PublicGeneric", result.getDisseminationStatus());
        assertEquals("2023-01-15T15:30:00Z", result.getModified());
        assertEquals("http://example.com/parent/scheme", result.getIriParent());
    }

    @Test
    void shouldConvertDomainToInfrastructureWithNewFields() {
        CodesListDomain domain = new CodesListDomain(
            "http://example.com/codelist/TEST001",
            "TEST001",
            "Test Label FR",
            "Test Label EN",
            "Description in French", 
            "Description in English",
            "http://example.com/range",
            "LAST_SEGMENT_001",
            "2023-01-01T10:00:00Z",
            "john.doe",
            "Validated",
            "PublicGeneric",
            "2023-01-15T15:30:00Z",
            "http://example.com/parent/scheme",
            Collections.emptyList()
        );
        
        CodesList result = CodesListConverter.toInfrastructure(domain);
        
        assertNotNull(result);
        assertEquals("Description in French", result.descriptionLg1());
        assertEquals("Description in English", result.descriptionLg2());
        assertEquals("LAST_SEGMENT_001", result.lastCodeUriSegment());
        assertEquals("2023-01-01T10:00:00Z", result.created());
        assertEquals("john.doe", result.creator());
        assertEquals("Validated", result.validationState());
        assertEquals("PublicGeneric", result.disseminationStatus());
        assertEquals("2023-01-15T15:30:00Z", result.modified());
        assertEquals("http://example.com/parent/scheme", result.iriParent());
    }
}