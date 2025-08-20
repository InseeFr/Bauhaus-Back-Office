package fr.insee.rmes.domain.exceptions;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class RmesExceptionTest {


    private static JSONObject detailsJson(RmesException ex) {
        assertNotNull(ex.getDetails(), "details should not be null");
        return new JSONObject(ex.getDetails());
    }


    @Test
    void ctor_status_message_details() throws Exception {
        RmesException ex = new RmesException(400, "bad request", "oops");
        assertEquals(400, ex.getStatus());
        JSONObject d = detailsJson(ex);
        assertEquals("bad request", d.getString("message"));
        assertEquals("oops", d.getString("details"));
        assertFalse(d.has("code"));
    }

    @Test
    void ctor_status_message_jsonArrayDetails() throws Exception {
        JSONArray arr = new JSONArray().put("e1").put("e2");
        RmesException ex = new RmesException(422, "validation failed", arr);

        assertEquals(422, ex.getStatus());
        JSONObject d = detailsJson(ex);
        assertEquals("validation failed", d.getString("message"));

        JSONArray got = new JSONArray(d.getString("details"));
        assertEquals(arr.toList(), got.toList());
    }

    @Test
    void ctor_status_errorCode_message_details() throws Exception {
        RmesException ex = new RmesException(404, 1001, "not found", "resource X");
        assertEquals(404, ex.getStatus());
        JSONObject d = detailsJson(ex);
        assertEquals(1001, d.getInt("code"));
        assertEquals("not found", d.getString("message"));
        assertEquals("resource X", d.getString("details"));
    }

    @Test
    void ctor_status_errorCode_details_only() throws Exception {
        RmesException ex = new RmesException(409, 2001, "conflict occurred");
        assertEquals(409, ex.getStatus());
        JSONObject d = detailsJson(ex);
        assertEquals(2001, d.getInt("code"));
        assertFalse(d.has("message"));
        assertEquals("conflict occurred", d.getString("details"));
    }

    @Test
    void ctor_status_errorCode_jsonArrayDetails() throws Exception {
        JSONArray arr = new JSONArray().put(1).put(2).put(3);
        RmesException ex = new RmesException(400, 3001, arr);

        assertEquals(400, ex.getStatus());
        JSONObject d = detailsJson(ex);
        assertEquals(3001, d.getInt("code"));

        JSONArray got = new JSONArray(d.getString("details"));
        assertEquals(arr.toList(), got.toList());
    }

    @Test
    void ctor_status_errorCode_message_jsonArrayDetails() throws Exception {
        JSONArray arr = new JSONArray().put("e1");
        RmesException ex = new RmesException(400, 3002, "oops", arr);

        assertEquals(400, ex.getStatus());
        JSONObject d = detailsJson(ex);
        assertEquals(3002, d.getInt("code"));
        assertEquals("oops", d.getString("message"));

        JSONArray got = new JSONArray(d.getString("details"));
        assertEquals(arr.toList(), got.toList());
    }

    @Test
    void ctor_status_errorCode_message_jsonObjectDetails() throws Exception {
        JSONObject det = new JSONObject().put("path", "/api/resource").put("hint", "retry");
        RmesException ex = new RmesException(401, 5001, "unauthorized", det);

        assertEquals(401, ex.getStatus());

        JSONObject d = new JSONObject(ex.getDetails());
        assertEquals(5001, d.getInt("code"));
        assertEquals("unauthorized", d.getString("message"));
        assertEquals("/api/resource", d.getString("path"));
        assertEquals("retry", d.getString("hint"));
        assertFalse(d.has("details"));
    }

    @Test
    void ctor_httpStatus_message_details() throws Exception {
        RmesException ex = new RmesException(HttpStatus.FORBIDDEN, "nope", "denied");
        assertEquals(HttpStatus.FORBIDDEN.value(), ex.getStatus());
        JSONObject d = detailsJson(ex);
        assertEquals("nope", d.getString("message"));
        assertEquals("denied", d.getString("details"));
        assertFalse(d.has("code"));
    }

    @Test
    void ctor_message_exceptionCause_sets500_and_cause() throws Exception {
        Exception cause = new IllegalStateException("BOOM");
        RmesException ex = new RmesException("wrapper message", cause);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getStatus());
        assertEquals("wrapper message", ex.getMessage());
        assertSame(cause, ex.getCause());
        assertEquals("BOOM", ex.getDetails()); // ici details = e.getMessage()
    }

    @Test
    void ctor_status_message_details_cause() throws Exception {
        Throwable cause = new RuntimeException("root");
        RmesException ex = new RmesException(418, "I’m a teapot", "brew failed", cause);

        assertEquals(418, ex.getStatus());
        assertEquals("I’m a teapot", ex.getMessage());
        assertSame(cause, ex.getCause());

        JSONObject d = detailsJson(ex);
        assertEquals("I’m a teapot", d.getString("message"));
        assertEquals("brew failed", d.getString("details"));
        assertFalse(d.has("code"));
    }
}