package fr.insee.rmes.utils;

import fr.insee.rmes.exceptions.RmesException;

import java.util.function.Consumer;

public class StreamUtils {

    private StreamUtils() {}

    public static <T> Consumer<T> safeConsumer(UnsafeConsumer<T> unsafeConsumer) {
        return t -> {
            try{
                unsafeConsumer.accept(t);
            }catch (Exception e) {
                throw new WrappedUnsafeException(e);
            }
        };
    }

    public static void executeAndThrow(Consumer<Void> unsafeStream) throws RmesException {
        try{
            unsafeStream.accept(null);
        }catch (WrappedUnsafeException e) {
            if (e.getCause() instanceof RmesException rmesException) {
                throw rmesException;
            }
            throw e;
        }
    }

    public interface UnsafeConsumer<T> {
        void accept(T t) throws Exception;
    }

    public static class WrappedUnsafeException extends RuntimeException {
        public WrappedUnsafeException(Exception e) {
        }
    }
}
