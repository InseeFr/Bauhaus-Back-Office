package fr.insee.rmes.utils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public interface DocumentBuilders
{
    interface ParserConfigurer
    {
        void configureParser(DocumentBuilderFactory factory) throws ParserConfigurationException;
    }

    static DocumentBuilder createSaferDocumentBuilder(ParserConfigurer parserConfigurer) throws ParserConfigurationException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        parserConfigurer.configureParser(factory);

        return factory.newDocumentBuilder();
    }
}