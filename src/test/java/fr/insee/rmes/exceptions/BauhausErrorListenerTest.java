package fr.insee.rmes.exceptions;

import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.apache.commons.lang3.time.StopWatch;
import javax.xml.transform.TransformerException;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BauhausErrorListenerTest {

    @Test
    void shouldCheckTheCorrectProgramExecutionTime() throws TransformerException {

        BauhausErrorListener bauhausErrorListener = new BauhausErrorListener();
        TransformerException transformerException = new TransformerException("mockedTransformerException");

        int timeLimit = 30;

        StopWatch stopWatch = new StopWatch();
        List<Long> times = new ArrayList<>();

        stopWatch.start();
        bauhausErrorListener.error(transformerException);
        stopWatch.stop();
        times.add(stopWatch.getTime());
        stopWatch.reset();

        stopWatch.start();
        bauhausErrorListener.warning(transformerException);
        stopWatch.stop();
        times.add(stopWatch.getTime());
        stopWatch.reset();

        stopWatch.start();
        bauhausErrorListener.fatalError(transformerException);
        stopWatch.stop();
        times.add(stopWatch.getTime());
        stopWatch.reset();

        boolean correctExecutionTimeInMillisecondsForWarning= times.getFirst()<timeLimit;
        boolean correctExecutionTimeInMillisecondsForError=times.get(1)<timeLimit;
        boolean correctExecutionTimeInMillisecondsForFatalError= times.getLast()<timeLimit;

        assertTrue( correctExecutionTimeInMillisecondsForWarning && correctExecutionTimeInMillisecondsForError && correctExecutionTimeInMillisecondsForFatalError);
    }
}