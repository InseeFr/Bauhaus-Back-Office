package fr.insee.rmes.utils;

import fr.insee.rmes.domain.exceptions.RmesException;
import org.json.JSONArray;

import java.lang.invoke.MethodHandle;
import java.text.Collator;
import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.insee.rmes.utils.MethodHandleUtils.findMethodHandle;
import static fr.insee.rmes.utils.MethodHandleUtils.safeInvokeMethodHandle;

public class DiacriticSorter {

    private DiacriticSorter(){}

    /**
     * Sorts a JSON array of objects by a given key extracted from each object.
     *
     * @param jsonArray the JSON array as a string
     * @param targetArrayClass the class of the array elements
     * @param keyExtractor a function to extract the sorting key from each element
     * @param businessProcessor an optional processing function to modify the stream before sorting
     * @param <T> the type of the elements in the array
     * @return a sorted list of objects
     * @throws RmesException if the JSON cannot be deserialized or an error occurs during processing
     */
    private static <T> List<T> sort(JSONArray jsonArray, Class<T[]> targetArrayClass, Function<T, String> keyExtractor, Optional<UnaryOperator<Stream<T>>> businessProcessor) throws RmesException {
        var stream = Arrays.stream(Deserializer.deserializeJSONArray(jsonArray, targetArrayClass));
        stream = businessProcessor.orElse(UnaryOperator.identity()).apply(stream);
        return stream.sorted(getComparator(keyExtractor)).toList();

    }

    private static <T extends AppendableObject<T>> UnaryOperator<Stream<T>> businessProcessor() {
        return stream -> stream.collect(Collectors.toMap(
                T::id,
                Function.identity(),
                T::appendObject
        )).values().stream();
    }

    public static <T extends AppendableObject<T>> List<T> sortGroupingByIdConcatenatingAltLabels(JSONArray jsonArray, Class<T[]> targetArrayClass, Function<T, String> keyExtractor) throws RmesException {
        return sort(jsonArray, targetArrayClass, keyExtractor, Optional.of(businessProcessor()));
    }

    public static <T> List<T> sort(JSONArray jsonArray, Class<T[]> targetArrayClass, Function<T, String> keyExtractor) throws RmesException {
        return sort(jsonArray, targetArrayClass, keyExtractor, Optional.empty());
    }

    private static <T> Comparator<T> getComparator(Function<T, String> projectToComparableField) {
        Collator collator = Collator.getInstance(Locale.FRENCH);
        collator.setStrength(Collator.PRIMARY);
        UnaryOperator<String> nullSafer = s -> s == null ? "" : s;
        return Comparator.comparing(nullSafer.compose(projectToComparableField), collator);
    }

    private static Class<?> getClassSafe(Object object) {
        return object == null ? null : object.getClass();
    }

    private static <R extends AppendableObject<R>> R withNewValue(R instance, String witherMethodName, String newValue) {
        var classR = instance.getClass();

        Optional<MethodHandle> wither = findMethodHandle(classR, witherMethodName, classR, String.class);

        if (wither.isEmpty()) {
            throw new IllegalStateException("Method 'public " + classR + " " + witherMethodName + "(String)' not found for '" + classR + "'. The class should implements XBuilder.With from @RecordBuilder");
        }
        var instanceWithNewAltLabel = safeInvokeMethodHandle(wither.get(), instance, newValue);
        if (!classR.isInstance(instanceWithNewAltLabel)) {
            throw new IllegalStateException("Method 'public " + classR + " " + witherMethodName + "(String)' for '" + classR + "' should return a type of " + classR + " instead of " + getClassSafe(instanceWithNewAltLabel) + ". Check that it implements XBuilder.With from @RecordBuilder");
        }
        return (R) instanceWithNewAltLabel;
    }

    interface AppendableObject<R extends AppendableObject<R>> {
        default R appendObject(R other) {
            return withNewValueForAttribute((R) this, this.appendedAttribute() + " || " + other.appendedAttribute());
        }

        R withNewValueForAttribute(R instance, String newValueForAttribute);

        String id();

        String appendedAttribute();
    }

    public interface AppendableLabels<R extends AppendableLabels<R>> extends AppendableObject<R> {

        @Override
        default String appendedAttribute() {
            return altLabels();
        }

        @Override
        default R withNewValueForAttribute(R instance, String newValueForAttribute) {
            return withNewValue(instance, "withAltLabels", newValueForAttribute);
        }

        String altLabels();
    }

    public interface AppendableLabel<R extends AppendableLabel<R>> extends AppendableObject<R> {

        @Override
        default String appendedAttribute() {
            return altLabel();
        }

        @Override
        default R withNewValueForAttribute(R instance, String newValueForAttribute) {
            return withNewValue(instance, "withAltLabel", newValueForAttribute);
        }

        String altLabel();

    }

}
