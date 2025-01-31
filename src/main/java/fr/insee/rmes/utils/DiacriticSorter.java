package fr.insee.rmes.utils;

import fr.insee.rmes.exceptions.RmesException;

import java.text.Collator;
import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class DiacriticSorter {
    private DiacriticSorter(){}
    public static <S extends Comparable<S>, T> List<T> sort(String jsonArray, Class<T[]> targetArrayClass, Function<T, S> projectToComparableField, Optional<UnaryOperator<Stream<T>>> businessProcessor) throws RmesException {
        var stream= Arrays.stream(Deserializer.deserializeJsonString(jsonArray, targetArrayClass));
        if (businessProcessor.isPresent()) {
            stream = businessProcessor.get().apply(stream);
        }
        return stream.sorted(getComparator(projectToComparableField)).toList();

    }

    private static <S extends Comparable<S>, T> Comparator<T> getComparator(Function<T, S> projectToComparableField) {
        Collator collator = Collator.getInstance(Locale.FRENCH);
        collator.setStrength(Collator.PRIMARY);
        return Comparator.comparing(projectToComparableField, collator);
    }

}
