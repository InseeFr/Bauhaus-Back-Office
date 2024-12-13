package fr.insee.rmes.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ConsoleCapture{

    private final ByteArrayOutputStream standardOut;
    private final ByteArrayOutputStream standardErr;
    private boolean capturing;
    private PrintStream lastErr;
    private PrintStream lastOut;

    private ConsoleCapture(ByteArrayOutputStream standardOut, ByteArrayOutputStream standardErr) {
        this.standardOut = standardOut;
        this.standardErr = standardErr;
        this.capturing=true;
    }

    public static ConsoleCapture startCapturingConsole() {
        ConsoleCapture consoleCapture = new ConsoleCapture(new ByteArrayOutputStream(), new ByteArrayOutputStream());
        consoleCapture.lastErr=System.err;
        consoleCapture.lastOut=System.out;
        System.setOut(new PrintStream(consoleCapture.standardOut));
        System.setErr(new PrintStream(consoleCapture.standardErr));
        return consoleCapture;
    }

    public void stop() {
        System.setErr(this.lastErr);
        System.setOut(this.lastOut);
        capturing=false;
    }

    public String standardOut() {
        if(! capturing) {
            throw new IllegalStateException("No capturing any more");
        }
        return standardOut.toString();
    }
}
