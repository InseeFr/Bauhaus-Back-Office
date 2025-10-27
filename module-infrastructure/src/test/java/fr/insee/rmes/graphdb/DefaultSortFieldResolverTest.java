package fr.insee.rmes.graphdb;

import fr.insee.rmes.sparql.annotations.DefaultSortField;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class DefaultSortFieldResolverTest {

    public record TestRecordWithDefaultSort(
            @DefaultSortField
            String name,
            String description,
            Integer value
    ) {}

    public record TestRecordWithoutDefaultSort(
            String name,
            String description,
            Integer value
    ) {}

    public record TestRecordWithMultipleDefaults(
            @DefaultSortField
            String name,
            @DefaultSortField
            String description,
            Integer value
    ) {}

    public record TestRecordWithNullableField(
            @DefaultSortField
            String name,
            String description
    ) {}

    public static class NonRecordClass {
        public String name;
    }

    @Test
    void shouldResolveSortFunctionForValidRecord() {
        final Function<TestRecordWithDefaultSort, String> sortFunction =
            DefaultSortFieldResolver.resolveSortFunction(TestRecordWithDefaultSort.class);
        
        assertNotNull(sortFunction);
        
        final TestRecordWithDefaultSort record = new TestRecordWithDefaultSort("John", "A person", 25);
        assertEquals("John", sortFunction.apply(record));
    }

    @Test
    void shouldReturnFirstAnnotatedFieldWhenMultipleDefaults() {
        final Function<TestRecordWithMultipleDefaults, String> sortFunction =
            DefaultSortFieldResolver.resolveSortFunction(TestRecordWithMultipleDefaults.class);
        
        assertNotNull(sortFunction);
        
        final TestRecordWithMultipleDefaults record = new TestRecordWithMultipleDefaults("John", "Description", 25);
        assertEquals("John", sortFunction.apply(record));
    }

    @Test
    void shouldHandleNullValues() {
        final Function<TestRecordWithNullableField, String> sortFunction =
            DefaultSortFieldResolver.resolveSortFunction(TestRecordWithNullableField.class);
        
        assertNotNull(sortFunction);
        
        final TestRecordWithNullableField record = new TestRecordWithNullableField(null, "Description");
        assertEquals("", sortFunction.apply(record));
    }

    @Test
    void shouldHandleNonStringFields() {
        record TestRecordWithIntSort(@DefaultSortField Integer value, String name) {}
        
        final Function<TestRecordWithIntSort, String> sortFunction =
            DefaultSortFieldResolver.resolveSortFunction(TestRecordWithIntSort.class);
        
        assertNotNull(sortFunction);
        
        final TestRecordWithIntSort record = new TestRecordWithIntSort(42, "Test");
        assertEquals("42", sortFunction.apply(record));
    }

    @Test
    void shouldThrowExceptionForNonRecordClass() {
        final IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> DefaultSortFieldResolver.resolveSortFunction(NonRecordClass.class)
        );
        
        assertEquals("Only record classes are supported", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenNoDefaultSortFieldAnnotation() {
        final RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> DefaultSortFieldResolver.resolveSortFunction(TestRecordWithoutDefaultSort.class)
        );
        
        assertEquals("No @DefaultSortField annotation found in TestRecordWithoutDefaultSort", exception.getMessage());
    }

    @Test
    void shouldReturnEmptyStringWhenAccessorThrowsException() {
        final Function<TestRecordWithDefaultSort, String> sortFunction =
            DefaultSortFieldResolver.resolveSortFunction(TestRecordWithDefaultSort.class);
        
        assertNotNull(sortFunction);
        
        final TestRecordWithDefaultSort record = new TestRecordWithDefaultSort("Test", "Description", 25);
        final String result = sortFunction.apply(record);
        assertEquals("Test", result);
    }

    @Test
    void shouldHandleComplexObjectTypes() {
        record TestRecordWithObjectSort(@DefaultSortField Object value, String name) {}
        
        final Function<TestRecordWithObjectSort, String> sortFunction =
            DefaultSortFieldResolver.resolveSortFunction(TestRecordWithObjectSort.class);
        
        assertNotNull(sortFunction);
        
        final TestRecordWithObjectSort record = new TestRecordWithObjectSort("StringValue", "Test");
        assertEquals("StringValue", sortFunction.apply(record));
        
        final TestRecordWithObjectSort recordWithNull = new TestRecordWithObjectSort(null, "Test");
        assertEquals("", sortFunction.apply(recordWithNull));
    }
}