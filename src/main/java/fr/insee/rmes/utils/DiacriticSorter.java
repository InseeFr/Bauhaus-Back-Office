package fr.insee.rmes.utils;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.AppendableLabel;

import java.text.Collator;
import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DiacriticSorter {
    private DiacriticSorter(){}
    /**
     * Sorts a JSON array of objects by a given key extracted from each object.
     *
     * @param jsonArray the JSON array as a string
     * @param targetArrayClass the class of the array elements
     * @param keyExtractor a function to extract the sorting key from each element
     * @param businessProcessor an optional processing function to modify the stream before sorting
     * @param <S> the type of the sorting key (must be Comparable)
     * @param <T> the type of the elements in the array
     * @return a sorted list of objects
     * @throws RmesException if the JSON cannot be deserialized or an error occurs during processing
     */
    private static <S extends Comparable<S>, T> List<T> sort(String jsonArray, Class<T[]> targetArrayClass, Function<T, S> keyExtractor, Optional<UnaryOperator<Stream<T>>> businessProcessor) throws RmesException {
        var stream= Arrays.stream(Deserializer.deserializeJsonString(jsonArray, targetArrayClass));
        stream = businessProcessor.orElse(UnaryOperator.identity()).apply(stream);
        return stream.sorted(getComparator(keyExtractor)).toList();

    }

    private static <T extends AppendableLabel<T>> UnaryOperator<Stream<T>> businessProcessor(){
        return stream -> stream.collect(Collectors.toMap(
                T::id,
                Function.identity(),
                T::appendLabel
        )).values().stream();
    }

    public static <S extends Comparable<S>, T extends AppendableLabel<T>> List<T> sortGroupingByIdConcatenatingAltLabels(String jsonArray, Class<T[]> targetArrayClass, Function<T, S> keyExtractor) throws RmesException {
        return sort(jsonArray, targetArrayClass, keyExtractor, Optional.of(businessProcessor()));
    }

    public static <S extends Comparable<S>, T> List<T> sort(String jsonArray, Class<T[]> targetArrayClass, Function<T, S> keyExtractor) throws RmesException {
        return sort(jsonArray, targetArrayClass, keyExtractor, Optional.empty());
    }

    private static <S extends Comparable<S>, T> Comparator<T> getComparator(Function<T, S> projectToComparableField) {
        Collator collator = Collator.getInstance(Locale.FRENCH);
        collator.setStrength(Collator.PRIMARY);
        return Comparator.comparing(projectToComparableField, collator);
    }

}
