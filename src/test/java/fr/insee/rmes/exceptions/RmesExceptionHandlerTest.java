package fr.insee.rmes.exceptions;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RmesExceptionHandlerTest {

    @ParameterizedTest
    @ValueSource(ints = { 100,200,201,202,203,204,205,206,300,301,302,303,304,305,400,401,402,403,404,405,406,407,408,409,410,411,412,413,414,415,500,501,502,503,504,505 })
    void shouldReturnHandleSubclassesOfRmesExceptionDetails(int codeError) {
        RmesExceptionHandler rmesExceptionHandler = new RmesExceptionHandler();
        RmesException rmesException = new RmesException(codeError,"RmesException message", "RmesExceptionDetails");
        ResponseEntity<String> actual = rmesExceptionHandler.handleSubclassesOfRmesException(rmesException);

        String[] handleSubclassesBegin = actual.toString().split(",");
        String[] handleSubclassesBeginWithoutSpace = handleSubclassesBegin[0].split(" ");

        String beginningOfRmesException= "<"+codeError;
        boolean isCorrectBeginMessage = beginningOfRmesException.equals(handleSubclassesBeginWithoutSpace[0]);

        String second = switch (handleSubclassesBeginWithoutSpace.length) {
            case 3 -> handleSubclassesBeginWithoutSpace[2].toUpperCase();
            case 4 -> handleSubclassesBeginWithoutSpace [2].toUpperCase()+"_"+handleSubclassesBeginWithoutSpace [3].toUpperCase();
            case 5 -> handleSubclassesBeginWithoutSpace [2].toUpperCase()+"_"+handleSubclassesBeginWithoutSpace [3].toUpperCase()+"_"+handleSubclassesBeginWithoutSpace [4].toUpperCase();
            case 6 -> handleSubclassesBeginWithoutSpace [2].toUpperCase()+"_"+handleSubclassesBeginWithoutSpace [3].toUpperCase()+"_"+handleSubclassesBeginWithoutSpace [4].toUpperCase()+"_"+handleSubclassesBeginWithoutSpace [5].toUpperCase();
            default -> "";};

        if(codeError==203){second = "NON_AUTHORITATIVE_INFORMATION";}

        boolean isCorrectMiddleMessage = second.equals(handleSubclassesBeginWithoutSpace [1]);
        assertTrue(isCorrectBeginMessage && isCorrectMiddleMessage );
    }

    @ParameterizedTest
    @ValueSource(strings = {"Bauhaus","back","unknown123","---"})
    void shouldReturnHandleRmesFileExceptionDetails(String myFileName) {
        RmesExceptionHandler rmesExceptionHandler = new RmesExceptionHandler();
        RmesFileException rmesFileException = new RmesFileException(myFileName,"message",new Throwable("throwable"));
        ResponseEntity<String> actual = rmesExceptionHandler.handleRmesFileException(rmesFileException);
        String expected="<500 INTERNAL_SERVER_ERROR Internal Server Error,RmesFileException{fileName='"+myFileName+"'},[]>";
        assertEquals(expected,actual.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"message-detail","myMessage-myDetail"})
    void shouldReturnHandleRmesExceptionDetails(String rmesExceptionInformation) {
        String message = rmesExceptionInformation.split("-")[0];
        String detail = rmesExceptionInformation.split("-")[1];
        RmesExceptionHandler rmesExceptionHandler = new RmesExceptionHandler();
        RmesException rmesException = new RmesException(401,message, detail);
        ResponseEntity<String> actual = rmesExceptionHandler.handleRmesException(rmesException);
        String expected="<500 INTERNAL_SERVER_ERROR Internal Server Error,{\"details\":\""+detail+"\",\"message\":\""+message+"\"},[]>";
        assertEquals(expected,actual.toString());
    }

}