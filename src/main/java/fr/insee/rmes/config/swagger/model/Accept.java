package fr.insee.rmes.config.swagger.model;

import org.springframework.http.MediaType;

public enum Accept {

    JSON(MediaType.APPLICATION_JSON_VALUE),
    XML(MediaType.APPLICATION_XML_VALUE);


    Accept(String applicationValue) {
    }
}
