package fr.insee.rmes.graphdb;

import fr.insee.rmes.graphdb.annotations.DefaultSortField;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.function.Function;

public class DefaultSortFieldResolver {

    @SuppressWarnings("unchecked")
    public static <T> Function<T, String> resolveSortFunction(Class<T> recordClass) {
        if (!recordClass.isRecord()) {
            throw new IllegalArgumentException("Only record classes are supported");
        }

        for (RecordComponent component : recordClass.getRecordComponents()) {
            if (component.isAnnotationPresent(DefaultSortField.class)) {
                String fieldName = component.getName();
                try {
                    Method accessor = recordClass.getMethod(fieldName);
                    return (T record) -> {
                        try {
                            Object value = accessor.invoke(record);
                            return value != null ? value.toString() : "";
                        } catch (Exception e) {
                            return "";
                        }
                    };
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException("Accessor method not found for field: " + fieldName, e);
                }
            }
        }

        throw new RuntimeException("No @DefaultSortField annotation found in " + recordClass.getSimpleName());
    }
}