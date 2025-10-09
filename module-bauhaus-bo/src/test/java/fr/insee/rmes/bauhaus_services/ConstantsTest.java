package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.Constants;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConstantsTest {

    @Test

    void shouldCheckConstantsAreNotUniqueness(){
        List<String> startingWithTheLetterA = List.of(
                Constants.ACCRUAL_PERIODICITY_LIST,
                Constants.ALT_LABEL_LG1,
                Constants.ALT_LABEL_LG2);


        List<String> startingWithTheLetterC = List.of(
                Constants.CODELIST ,
                Constants.COLLECTION,
                Constants.CONCEPT,
                Constants.CONTRIBUTOR ,
                Constants.CONTRIBUTORS,
                Constants.CREATED,
                Constants.CREATOR ,
                Constants.CREATORS);

        List<String> startingWithTheLetterD = List.of(
                Constants.DATASET ,
                Constants.DISTRIBUTION,
                Constants.DATA_COLLECTOR,
                Constants.DATA_COLLECTORS ,
                Constants.DEF_COURTE_LG1,
                Constants.DEF_COURTE_LG2,
                Constants.DEF_LONGUE_LG1 ,
                Constants.DEF_LONGUE_LG2,
                Constants.DESCRIPTION_LG1,
                Constants.DESCRIPTION_LG2,
                Constants.DOCUMENT ,
                Constants.DOCUMENTS_LG1,
                Constants.DOCUMENTS_LG2
                );

        List<String> startingWithTheLetterE = List.of(
                Constants.EDITORIAL_NOTE_LG1 ,
                Constants.EDITORIAL_NOTE_LG2
        );

        List<String> startingWithTheLetterF = List.of(
                Constants.FAMILY
        );

        List<String> startingWithTheLetterG = List.of(
                Constants.GOAL_COMITE_LABEL ,
                Constants.GOAL_RMES
        );

        List<String> startingWithTheLetterH = List.of(
                Constants.HAS_DOC_LG1 ,
                Constants.HAS_DOC_LG2
        );

        List<String> startingWithTheLetterI = List.of(
                Constants.ID ,
                Constants.ID_ATTRIBUTE,
                Constants.ID_INDICATOR,
                Constants.ID_OPERATION,
                Constants.ID_SERIES,
                Constants.ID_SIMS,
                Constants.INDICATOR_UP,
                Constants.ISREPLACEDBY,
                Constants.ISVALIDATED
        );


        List<String> startingWithTheLetterL = List.of(
                Constants.LABEL ,
                Constants.LABEL_LG1,
                Constants.LABEL_LG2,
                Constants.LANG
        );

        List<String> startingWithTheLetterM = List.of(
                Constants.MANAGER ,
                Constants.MODIFIED
        );

        List<String> startingWithTheLetterN = List.of(
                Constants.NOTATION
        );

        List<String> startingWithTheLetterO = List.of(
                Constants.OPERATIONS,
                Constants.OPERATION_UP,
                Constants.ORGANIZATIONS,
                Constants.OUTPUT,
                Constants.OWNER
                );

        List<String> startingWithTheLetterP = List.of(
                Constants.PARAMETERS_FILE,
                Constants.PARENTS,
                Constants.PREF_LABEL_LG1,
                Constants.PREF_LABEL_LG2,
                Constants.PUBLISHER,
                Constants.PUBLISHERS
        );

        List<String> startingWithTheLetterR = List.of(
                Constants.RANGE_TYPE,
                Constants.REPLACES,
                Constants.REPOSITORY_EXCEPTION
        );

        List<String> startingWithTheLetterS = List.of(
                Constants.SEEALSO,
                Constants.SERIES_UP,
                Constants.STAMP
        );


        List<String> startingWithTheLetterT = List.of(
                Constants.TEXT,
                Constants.TEXT_LG1,
                Constants.TEXT_LG2,
                Constants.TYPE_OF_OBJECT,
                Constants.TYPE_STRING,
                Constants.TYPELIST
        );

        List<String> startingWithTheLetterU = List.of(
                Constants.UNDEFINED,
                Constants.UPDATED_DATE,
                Constants.URI,
                Constants.URL
        );

        List<String> startingWithTheLetterV = List.of(
                Constants.VALUE
        );

        List<String> startingWithTheLetterW = List.of(
                Constants.WASGENERATEDBY
        );

        List<String> startingWithTheLetterX = List.of(
                Constants.XML,
                Constants.XML_EMPTY_TAG,
                Constants.XML_OPEN_CODELIST_TAG,
                Constants.XML_END_CODELIST_TAG,
                Constants.XML_OPEN_PARAMETERS_TAG,
                Constants.XML_END_PARAMETERS_TAG,
                Constants.XML_OPEN_LANGUAGES_TAG,
                Constants.XML_END_LANGUAGES_TAG,
                Constants.XML_OPEN_TARGET_TYPE_TAG,
                Constants.XML_END_TARGET_TYPE_TAG,
                Constants.XML_OPEN_INCLUDE_EMPTY_FIELDS_TAG,
                Constants.XML_END_INCLUDE_EMPTY_FIELDS_TAG,
                Constants.XML_INF_REPLACEMENT,
                Constants.XML_SUP_REPLACEMENT,
                Constants.XML_ESPERLUETTE_REPLACEMENT
                );

        List<String> actual =new ArrayList<>();
        actual.addAll(startingWithTheLetterA);
        actual.addAll(startingWithTheLetterC);
        actual.addAll(startingWithTheLetterD);
        actual.addAll(startingWithTheLetterE);
        actual.addAll(startingWithTheLetterF);
        actual.addAll(startingWithTheLetterG);
        actual.addAll(startingWithTheLetterH);
        actual.addAll(startingWithTheLetterI);
        actual.addAll(startingWithTheLetterL);
        actual.addAll(startingWithTheLetterM);
        actual.addAll(startingWithTheLetterN);
        actual.addAll(startingWithTheLetterO);
        actual.addAll(startingWithTheLetterP);
        actual.addAll(startingWithTheLetterR);
        actual.addAll(startingWithTheLetterS);
        actual.addAll(startingWithTheLetterT);
        actual.addAll(startingWithTheLetterU);
        actual.addAll(startingWithTheLetterV);
        actual.addAll(startingWithTheLetterW);
        actual.addAll(startingWithTheLetterX);

        SortedSet<String> expected = new TreeSet<>(actual);

        boolean isSizesDifferent = expected.size()!=actual.size();
        boolean isSameValue = Objects.equals(Constants.TEXT, Constants.TEXT_LG2);

        assertTrue(isSizesDifferent && isSameValue);

    }

        }