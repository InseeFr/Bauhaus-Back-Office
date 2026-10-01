package fr.insee.rmes.modules.commons.webservice;

import static fr.insee.rmes.modules.commons.webservice.ApiErrorContract.assertApiError;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ApiErrorContractTest {

    @ParameterizedTest
    @ValueSource(strings = {"""
                {"message":"Boom"}""", """
                {"message":"Boom","code":"804","params":{"id":"s1001"}}""", """
                {"message":"The submitted data is invalid","code":"INVALID_REQUEST_BODY",
                 "errors":[{"field":"prefLabelLg1","message":"Ce champ est obligatoire."}]}"""})
    void accepts_the_bodies_of_the_adr(String body) {
        assertThatCode(() -> assertApiError(body)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "plain text", """
                {"code":"804"}""", """
                {"message":""}""", """
                {"message":"Boom","code":804}""", """
                {"message":"Boom","details":"[]"}""", """
                {"message":"Boom","detail":"Boom","status":500}""", """
                {"message":"Boom","params":{"ids":["a","b"]}}""", """
                {"message":"Boom","errors":["$.x: is missing"]}""", """
                {"message":"Boom","errors":[]}"""})
    void rejects_any_other_body(String body) {
        assertThatThrownBy(() -> assertApiError(body)).isInstanceOf(Throwable.class);
    }
}
