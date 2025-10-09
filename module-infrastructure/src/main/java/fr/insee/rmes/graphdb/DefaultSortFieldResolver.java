package fr.insee.rmes.graphdb;


import fr.insee.rmes.sparql.annotations.DefaultSortField;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.function.Function;

public enum DefaultSortFieldResolver {
    ;

    @SuppressWarnings("unchecked")
    public static <T> Function<T, String> resolveSortFunction(final Class<T> recordClass) {
        if (!recordClass.isRecord()) {
            throw new IllegalArgumentException("Only record classes are supported");
        }

        for (final RecordComponent component : recordClass.getRecordComponents()) {
            if (component.isAnnotationPresent(DefaultSortField.class)) {
                final String fieldName = component.getName();
                try {
                    final Method accessor = recordClass.getMethod(fieldName);
                    return (final T record) -> {
                        try {
                            final Object value = accessor.invoke(record);
                            return null != value ? value.toString() : "";
                        } catch (Exception _) {
                            return "";
                        }
                    };
                } catch (final NoSuchMethodException e) {
                    throw new RuntimeException("Accessor method not found for field: " + fieldName, e);
                }
            }
        }

        throw new RuntimeException("No @DefaultSortField annotation found in " + recordClass.getSimpleName());
    }
}