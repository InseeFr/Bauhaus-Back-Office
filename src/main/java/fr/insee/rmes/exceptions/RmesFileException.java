package fr.insee.rmes.exceptions;

public class RmesFileException extends RuntimeException {

    private final String fileName;

    public RmesFileException(String filename, String message, Throwable cause) {
        super(message, cause);
        this.fileName = filename;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public String toString() {
        return "RmesFileException{" +
                "fileName='" + fileName + '\'' +
                '}';
    }
}
