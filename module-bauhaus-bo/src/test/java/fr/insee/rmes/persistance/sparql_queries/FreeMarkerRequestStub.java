package fr.insee.rmes.persistance.sparql_queries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import org.junit.jupiter.api.function.Executable;
import org.mockito.MockedStatic;

/**
 * Bouchonne {@link FreeMarkerUtils#buildRequest} le temps d'un appel à une classe de requêtes SPARQL,
 * pour vérifier le template choisi et les paramètres qui lui sont passés.
 */
public final class FreeMarkerRequestStub {

    /** Les paramètres ne sont pas contrôlés, seul le template l'est. */
    public static final Predicate<Map<String, Object>> ANY_PARAMS = Objects::nonNull;

    @FunctionalInterface
    public interface QueryCall {
        String call() throws RmesException;
    }

    private FreeMarkerRequestStub() {}

    /**
     * Stubs {@code buildRequest(folder, template, any map)} to return {@code builtQuery} and runs the call,
     * without verifying the parameters.
     */
    public static String callWithStubbedTemplate(String folder, String template, String builtQuery, QueryCall call)
            throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            stubTemplate(mockedFreeMarker, folder, template, builtQuery);
            return call.call();
        }
    }

    /**
     * Stubs {@code buildRequest(folder, template, any map)} to return {@code builtQuery}, runs the call, then
     * verifies the template was built with parameters matching {@code expectedParams}.
     */
    @SuppressWarnings("unchecked")
    public static String callWithStubbedTemplate(
            String folder,
            String template,
            String builtQuery,
            QueryCall call,
            Predicate<Map<String, Object>> expectedParams)
            throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            stubTemplate(mockedFreeMarker, folder, template, builtQuery);
            String result = call.call();
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(
                    eq(folder), eq(template), argThat(params -> expectedParams.test((Map<String, Object>) params))));
            return result;
        }
    }

    /** The call returns the query built from the template, without verifying the parameters. */
    public static void assertQueryBuiltFromTemplate(String folder, String template, String builtQuery, QueryCall call)
            throws RmesException {
        String result = callWithStubbedTemplate(folder, template, builtQuery, call);

        assertNotNull(result);
        assertEquals(builtQuery, result);
    }

    /** The call returns the query built from the template, whose parameters match {@code expectedParams}. */
    public static void assertQueryBuiltFromTemplate(
            String folder,
            String template,
            String builtQuery,
            QueryCall call,
            Predicate<Map<String, Object>> expectedParams)
            throws RmesException {
        String result = callWithStubbedTemplate(folder, template, builtQuery, call, expectedParams);

        assertNotNull(result);
        assertEquals(builtQuery, result);
    }

    /** An {@link RmesException} thrown while building the template is propagated as is. */
    public static void assertRmesExceptionPropagated(String folder, String template, Executable call) {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            RmesException testException = new RmesException(500, "Test error", "Test error message");
            mockedFreeMarker
                    .when(() -> FreeMarkerUtils.buildRequest(eq(folder), eq(template), any(Map.class)))
                    .thenThrow(testException);

            RmesException exception = assertThrows(RmesException.class, call);

            assertEquals(testException, exception);
        }
    }

    private static void stubTemplate(
            MockedStatic<FreeMarkerUtils> mockedFreeMarker, String folder, String template, String builtQuery) {
        mockedFreeMarker
                .when(() -> FreeMarkerUtils.buildRequest(eq(folder), eq(template), any(Map.class)))
                .thenReturn(builtQuery);
    }
}
