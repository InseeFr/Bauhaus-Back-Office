package fr.insee.rmes.bauhaus_services.concepts;

import fr.insee.rmes.bauhaus_services.concepts.concepts.ConceptsExportBuilder;
import fr.insee.rmes.bauhaus_services.concepts.concepts.ConceptsUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.utils.ExportUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

class ConceptsImplTest {

    @Mock
    ConceptsUtils conceptsUtils;

    @Mock
    RepositoryGestion repoGestion;


/*
[2024-06-21 18:06:09] [info] 2024-06-21T18:06:09.085+02:00  INFO 34379 --- [Bauhaus-Back-Office] [nio-8080-exec-8] fr.insee.rmes.config.LogRequestFilter    : START From gestion-metadonnees-api.developpement2.insee.fr by user xrmfux DR59-SNDI59 call GET /concepts/concept/export/c1116
[2024-06-21 18:06:10] [info] 2024-06-21T18:06:10.179+02:00 TRACE 34379 --- [Bauhaus-Back-Office] [nio-8080-exec-8] f.i.r.b.rdf_utils.RepositoryUtils        : Repo http://dvrmesrdfglm001:8080/repositories/gestion --- Executed query ---
[2024-06-21 18:06:10] [info] PREFIX dcterms:<http://purl.org/dc/terms/>
[2024-06-21 18:06:10] [info] PREFIX xkos:<http://rdf-vocabulary.ddialliance.org/xkos#>
[2024-06-21 18:06:10] [info] PREFIX evoc:<http://eurovoc.europa.eu/schema#>
[2024-06-21 18:06:10] [info] PREFIX skos:<http://www.w3.org/2004/02/skos/core#>
[2024-06-21 18:06:10] [info] PREFIX skosxl:<http://www.w3.org/2008/05/skos-xl#>
[2024-06-21 18:06:10] [info] PREFIX dc:<http://purl.org/dc/elements/1.1/>
[2024-06-21 18:06:10] [info] PREFIX insee:<http://rdf.insee.fr/def/base#>
[2024-06-21 18:06:10] [info] PREFIX geo:<http://www.opengis.net/ont/geosparql#>
[2024-06-21 18:06:10] [info] PREFIX igeo:<http://rdf.insee.fr/def/geo#>
[2024-06-21 18:06:10] [info] PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
[2024-06-21 18:06:10] [info] PREFIX pav:<http://purl.org/pav/>
[2024-06-21 18:06:10] [info] PREFIX foaf:<http://xmlns.com/foaf/0.1/>
[2024-06-21 18:06:10] [info] PREFIX org:<http://www.w3.org/ns/org#>
[2024-06-21 18:06:10] [info] PREFIX prov:<http://www.w3.org/ns/prov#>
[2024-06-21 18:06:10] [info] PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>
[2024-06-21 18:06:10] [info] PREFIX sdmx-mm:<http://www.w3.org/ns/sdmx-mm#>
[2024-06-21 18:06:10] [info] PREFIX qb:<http://purl.org/linked-data/cube#>
[2024-06-21 18:06:10] [info] PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>
[2024-06-21 18:06:10] [info] PREFIX dcat:<http://www.w3.org/ns/dcat#>
[2024-06-21 18:06:10] [info] PREFIX adms: <http://www.w3.org/ns/adms#>
[2024-06-21 18:06:10] [info] PREFIX dcmitype:<http://purl.org/dc/dcmitype/>
[2024-06-21 18:06:10] [info]
[2024-06-21 18:06:10] [info] ASK
[2024-06-21 18:06:10] [info] WHERE
[2024-06-21 18:06:10] [info] { ?uri ?b ?c .
[2024-06-21 18:06:10] [info]  FILTER(STRENDS(STR(?uri),'/concepts/definition/c1116')) . }
[2024-06-21 18:06:10] [info] 2024-06-21T18:06:10.179+02:00 TRACE 34379 --- [Bauhaus-Back-Office] [nio-8080-exec-8] f.i.r.b.rdf_utils.RepositoryUtils        : --- Results ---
[2024-06-21 18:06:10] [info] true
[2024-06-21 18:06:10] [info] 2024-06-21T18:06:10.220+02:00 TRACE 34379 --- [Bauhaus-Back-Office] [nio-8080-exec-8] f.i.r.b.rdf_utils.RepositoryUtils        : Repo http://dvrmesrdfglm001:8080/repositories/gestion --- Executed query ---
[2024-06-21 18:06:10] [info] PREFIX dcterms:<http://purl.org/dc/terms/>
[2024-06-21 18:06:10] [info] PREFIX xkos:<http://rdf-vocabulary.ddialliance.org/xkos#>
[2024-06-21 18:06:10] [info] PREFIX evoc:<http://eurovoc.europa.eu/schema#>
[2024-06-21 18:06:10] [info] PREFIX skos:<http://www.w3.org/2004/02/skos/core#>
[2024-06-21 18:06:10] [info] PREFIX skosxl:<http://www.w3.org/2008/05/skos-xl#>
[2024-06-21 18:06:10] [info] PREFIX dc:<http://purl.org/dc/elements/1.1/>
[2024-06-21 18:06:10] [info] PREFIX insee:<http://rdf.insee.fr/def/base#>
[2024-06-21 18:06:10] [info] PREFIX geo:<http://www.opengis.net/ont/geosparql#>
[2024-06-21 18:06:10] [info] PREFIX igeo:<http://rdf.insee.fr/def/geo#>
[2024-06-21 18:06:10] [info] PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
[2024-06-21 18:06:10] [info] PREFIX pav:<http://purl.org/pav/>
[2024-06-21 18:06:10] [info] PREFIX foaf:<http://xmlns.com/foaf/0.1/>
[2024-06-21 18:06:10] [info] PREFIX org:<http://www.w3.org/ns/org#>
[2024-06-21 18:06:10] [info] PREFIX prov:<http://www.w3.org/ns/prov#>
[2024-06-21 18:06:10] [info] PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>
[2024-06-21 18:06:10] [info] PREFIX sdmx-mm:<http://www.w3.org/ns/sdmx-mm#>
[2024-06-21 18:06:10] [info] PREFIX qb:<http://purl.org/linked-data/cube#>
[2024-06-21 18:06:10] [info] PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>
[2024-06-21 18:06:10] [info] PREFIX dcat:<http://www.w3.org/ns/dcat#>
[2024-06-21 18:06:10] [info] PREFIX adms: <http://www.w3.org/ns/adms#>
[2024-06-21 18:06:10] [info] PREFIX dcmitype:<http://purl.org/dc/dcmitype/>
[2024-06-21 18:06:10] [info]
[2024-06-21 18:06:10] [info] SELECT ?id ?prefLabelLg1 ?prefLabelLg2 ?creator ?contributor ?disseminationStatus ?additionalMaterial ?created ?modified ?valid ?conceptVersion ?isValidated
[2024-06-21 18:06:10] [info] WHERE { GRAPH <http://rdf.insee.fr/graphes/concepts/definitions> {
[2024-06-21 18:06:10] [info] ?concept skos:prefLabel ?prefLabelLg1 .
[2024-06-21 18:06:10] [info] FILTER(REGEX(STR(?concept),'/concepts/definition/c1116')) .
[2024-06-21 18:06:10] [info] BIND(STRAFTER(STR(?concept),'/definition/') AS ?id) .
[2024-06-21 18:06:10] [info] ?concept ?versionnedNote ?versionnedNoteURI .
[2024-06-21 18:06:10] [info] ?versionnedNoteURI insee:conceptVersion ?conceptVersion .
[2024-06-21 18:06:10] [info] ?concept insee:isValidated ?isValidated .
[2024-06-21 18:06:10] [info] FILTER (lang(?prefLabelLg1) = 'fr') .
[2024-06-21 18:06:10] [info] OPTIONAL {?concept skos:prefLabel ?prefLabelLg2 .
[2024-06-21 18:06:10] [info] FILTER (lang(?prefLabelLg2) = 'en') } .
[2024-06-21 18:06:10] [info] OPTIONAL {?concept dc:creator ?creator} .
[2024-06-21 18:06:10] [info] ?concept dc:contributor ?contributor .
[2024-06-21 18:06:10] [info] ?concept insee:disseminationStatus ?disseminationStatus
[2024-06-21 18:06:10] [info] OPTIONAL {?concept insee:additionalMaterial ?additionalMaterial} .
[2024-06-21 18:06:10] [info] ?concept dcterms:created ?created .
[2024-06-21 18:06:10] [info] OPTIONAL {?concept dcterms:modified ?modified} .
[2024-06-21 18:06:10] [info] OPTIONAL {?concept dcterms:valid ?valid} .
[2024-06-21 18:06:10] [info] }}
[2024-06-21 18:06:10] [info] ORDER BY DESC(xsd:integer(?conceptVersion))
[2024-06-21 18:06:10] [info] LIMIT 1
[2024-06-21 18:06:10] [info] 2024-06-21T18:06:10.220+02:00 TRACE 34379 --- [Bauhaus-Back-Office] [nio-8080-exec-8] f.i.r.b.rdf_utils.RepositoryUtils        : --- Results ---
[2024-06-21 18:06:10] [info] {
[2024-06-21 18:06:10] [info]   "head" : {
[2024-06-21 18:06:10] [info]     "vars" : [
[2024-06-21 18:06:10] [info]       "id",
[2024-06-21 18:06:10] [info]       "prefLabelLg1",
[2024-06-21 18:06:10] [info]       "prefLabelLg2",
[2024-06-21 18:06:10] [info]       "creator",
[2024-06-21 18:06:10] [info]       "contributor",
[2024-06-21 18:06:10] [info]       "disseminationStatus",
[2024-06-21 18:06:10] [info]       "additionalMaterial",
[2024-06-21 18:06:10] [info]       "created",
[2024-06-21 18:06:10] [info]       "modified",
[2024-06-21 18:06:10] [info]       "valid",
[2024-06-21 18:06:10] [info]       "conceptVersion",
[2024-06-21 18:06:10] [info]       "isValidated"
[2024-06-21 18:06:10] [info]     ]
[2024-06-21 18:06:10] [info]   },
[2024-06-21 18:06:10] [info]   "results" : {
[2024-06-21 18:06:10] [info]     "bindings" : [
[2024-06-21 18:06:10] [info]       {
[2024-06-21 18:06:10] [info]         "id" : {
[2024-06-21 18:06:10] [info]           "type" : "literal",
[2024-06-21 18:06:10] [info]           "value" : "c1116"
[2024-06-21 18:06:10] [info]         },
[2024-06-21 18:06:10] [info]         "prefLabelLg1" : {
[2024-06-21 18:06:10] [info]           "xml:lang" : "fr",
[2024-06-21 18:06:10] [info]           "type" : "literal",
[2024-06-21 18:06:10] [info]           "value" : "Accidents corporels de la circulation"
[2024-06-21 18:06:10] [info]         },
[2024-06-21 18:06:10] [info]         "prefLabelLg2" : {
[2024-06-21 18:06:10] [info]           "xml:lang" : "en",
[2024-06-21 18:06:10] [info]           "type" : "literal",
[2024-06-21 18:06:10] [info]           "value" : "Road accidents"
[2024-06-21 18:06:10] [info]         },
[2024-06-21 18:06:10] [info]         "creator" : {
[2024-06-21 18:06:10] [info]           "type" : "literal",
[2024-06-21 18:06:10] [info]           "value" : "SSM-SDES"
[2024-06-21 18:06:10] [info]         },
[2024-06-21 18:06:10] [info]         "contributor" : {
[2024-06-21 18:06:10] [info]           "type" : "literal",
[2024-06-21 18:06:10] [info]           "value" : "DG75-L201"
[2024-06-21 18:06:10] [info]         },
[2024-06-21 18:06:10] [info]         "disseminationStatus" : {
[2024-06-21 18:06:10] [info]           "type" : "uri",
[2024-06-21 18:06:10] [info]           "value" : "http://id.insee.fr/codes/base/statutDiffusion/PublicGenerique"
[2024-06-21 18:06:10] [info]         },
[2024-06-21 18:06:10] [info]         "created" : {
[2024-06-21 18:06:10] [info]           "datatype" : "http://www.w3.org/2001/XMLSchema#dateTime",
[2024-06-21 18:06:10] [info]           "type" : "literal",
[2024-06-21 18:06:10] [info]           "value" : "2002-12-23T00:00:00"
[2024-06-21 18:06:10] [info]         },
[2024-06-21 18:06:10] [info]         "modified" : {
[2024-06-21 18:06:10] [info]           "datatype" : "http://www.w3.org/2001/XMLSchema#dateTime",
[2024-06-21 18:06:10] [info]           "type" : "literal",
[2024-06-21 18:06:10] [info]           "value" : "2023-10-19T08:52:59.170187"
[2024-06-21 18:06:10] [info]         },
[2024-06-21 18:06:10] [info]         "valid" : {
[2024-06-21 18:06:10] [info]           "datatype" : "http://www.w3.org/2001/XMLSchema#dateTime",
[2024-06-21 18:06:10] [info]           "type" : "literal",
[2024-06-21 18:06:10] [info]           "value" : "2023-10-18T00:00:00"
[2024-06-21 18:06:10] [info]         },
[2024-06-21 18:06:10] [info]         "conceptVersion" : {
[2024-06-21 18:06:10] [info]           "datatype" : "http://www.w3.org/2001/XMLSchema#int",
[2024-06-21 18:06:10] [info]           "type" : "literal",
[2024-06-21 18:06:10] [info]           "value" : "2"
[2024-06-21 18:06:10] [info]         },
[2024-06-21 18:06:10] [info]         "isValidated" : {
[2024-06-21 18:06:10] [info]           "datatype" : "http://www.w3.org/2001/XMLSchema#boolean",
[2024-06-21 18:06:10] [info]           "type" : "literal",
[2024-06-21 18:06:10] [info]           "value" : "false"
[2024-06-21 18:06:10] [info]         }
[2024-06-21 18:06:10] [info]       }
[2024-06-21 18:06:10] [info]     ]
[2024-06-21 18:06:10] [info]   }
[2024-06-21 18:06:10] [info] }
[2024-06-21 18:06:10] [info] 2024-06-21T18:06:10.236+02:00 TRACE 34379 --- [Bauhaus-Back-Office] [nio-8080-exec-8] f.i.r.b.rdf_utils.RepositoryUtils        : Repo http://dvrmesrdfglm001:8080/repositories/gestion --- Executed query ---
[2024-06-21 18:06:10] [info] PREFIX dcterms:<http://purl.org/dc/terms/>
[2024-06-21 18:06:10] [info] PREFIX xkos:<http://rdf-vocabulary.ddialliance.org/xkos#>
[2024-06-21 18:06:10] [info] PREFIX evoc:<http://eurovoc.europa.eu/schema#>
[2024-06-21 18:06:10] [info] PREFIX skos:<http://www.w3.org/2004/02/skos/core#>
[2024-06-21 18:06:10] [info] PREFIX skosxl:<http://www.w3.org/2008/05/skos-xl#>
[2024-06-21 18:06:10] [info] PREFIX dc:<http://purl.org/dc/elements/1.1/>
[2024-06-21 18:06:10] [info] PREFIX insee:<http://rdf.insee.fr/def/base#>
[2024-06-21 18:06:10] [info] PREFIX geo:<http://www.opengis.net/ont/geosparql#>
[2024-06-21 18:06:10] [info] PREFIX igeo:<http://rdf.insee.fr/def/geo#>
[2024-06-21 18:06:10] [info] PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
[2024-06-21 18:06:10] [info] PREFIX pav:<http://purl.org/pav/>
[2024-06-21 18:06:10] [info] PREFIX foaf:<http://xmlns.com/foaf/0.1/>
[2024-06-21 18:06:10] [info] PREFIX org:<http://www.w3.org/ns/org#>
[2024-06-21 18:06:10] [info] PREFIX prov:<http://www.w3.org/ns/prov#>
[2024-06-21 18:06:10] [info] PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>
[2024-06-21 18:06:10] [info] PREFIX sdmx-mm:<http://www.w3.org/ns/sdmx-mm#>
[2024-06-21 18:06:10] [info] PREFIX qb:<http://purl.org/linked-data/cube#>
[2024-06-21 18:06:10] [info] PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>
[2024-06-21 18:06:10] [info] PREFIX dcat:<http://www.w3.org/ns/dcat#>
[2024-06-21 18:06:10] [info] PREFIX adms: <http://www.w3.org/ns/adms#>
[2024-06-21 18:06:10] [info] PREFIX dcmitype:<http://purl.org/dc/dcmitype/>
[2024-06-21 18:06:10] [info]
[2024-06-21 18:06:10] [info] SELECT ?altLabel
[2024-06-21 18:06:10] [info] WHERE {
[2024-06-21 18:06:10] [info] ?concept skos:altLabel ?altLabel
[2024-06-21 18:06:10] [info] FILTER (lang(?altLabel) = 'fr') .
[2024-06-21 18:06:10] [info] FILTER(REGEX(STR(?concept),'/concepts/definition/c1116')) .
[2024-06-21 18:06:10] [info] }
[2024-06-21 18:06:10] [info] 2024-06-21T18:06:10.237+02:00 TRACE 34379 --- [Bauhaus-Back-Office] [nio-8080-exec-8] f.i.r.b.rdf_utils.RepositoryUtils        : --- Results ---
[2024-06-21 18:06:10] [info] {
[2024-06-21 18:06:10] [info]   "head" : {
[2024-06-21 18:06:10] [info]     "vars" : [
[2024-06-21 18:06:10] [info]       "altLabel"
[2024-06-21 18:06:10] [info]     ]
[2024-06-21 18:06:10] [info]   },
[2024-06-21 18:06:10] [info]   "results" : {
[2024-06-21 18:06:10] [info]     "bindings" : [ ]
[2024-06-21 18:06:10] [info]   }
[2024-06-21 18:06:10] [info] }
[2024-06-21 18:06:10] [info] 2024-06-21T18:06:10.246+02:00 TRACE 34379 --- [Bauhaus-Back-Office] [nio-8080-exec-8] f.i.r.b.rdf_utils.RepositoryUtils        : Repo http://dvrmesrdfglm001:8080/repositories/gestion --- Executed query ---
[2024-06-21 18:06:10] [info] PREFIX dcterms:<http://purl.org/dc/terms/>
[2024-06-21 18:06:10] [info] PREFIX xkos:<http://rdf-vocabulary.ddialliance.org/xkos#>
[2024-06-21 18:06:10] [info] PREFIX evoc:<http://eurovoc.europa.eu/schema#>
[2024-06-21 18:06:10] [info] PREFIX skos:<http://www.w3.org/2004/02/skos/core#>
[2024-06-21 18:06:10] [info] PREFIX skosxl:<http://www.w3.org/2008/05/skos-xl#>
[2024-06-21 18:06:10] [info] PREFIX dc:<http://purl.org/dc/elements/1.1/>
[2024-06-21 18:06:10] [info] PREFIX insee:<http://rdf.insee.fr/def/base#>
[2024-06-21 18:06:10] [info] PREFIX geo:<http://www.opengis.net/ont/geosparql#>
[2024-06-21 18:06:10] [info] PREFIX igeo:<http://rdf.insee.fr/def/geo#>
[2024-06-21 18:06:10] [info] PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
[2024-06-21 18:06:10] [info] PREFIX pav:<http://purl.org/pav/>
[2024-06-21 18:06:10] [info] PREFIX foaf:<http://xmlns.com/foaf/0.1/>
[2024-06-21 18:06:10] [info] PREFIX org:<http://www.w3.org/ns/org#>
[2024-06-21 18:06:10] [info] PREFIX prov:<http://www.w3.org/ns/prov#>
[2024-06-21 18:06:10] [info] PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>
[2024-06-21 18:06:10] [info] PREFIX sdmx-mm:<http://www.w3.org/ns/sdmx-mm#>
[2024-06-21 18:06:10] [info] PREFIX qb:<http://purl.org/linked-data/cube#>
[2024-06-21 18:06:10] [info] PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>
[2024-06-21 18:06:10] [info] PREFIX dcat:<http://www.w3.org/ns/dcat#>
[2024-06-21 18:06:10] [info] PREFIX adms: <http://www.w3.org/ns/adms#>
[2024-06-21 18:06:10] [info] PREFIX dcmitype:<http://purl.org/dc/dcmitype/>
[2024-06-21 18:06:10] [info]
[2024-06-21 18:06:10] [info] SELECT ?altLabel
[2024-06-21 18:06:10] [info] WHERE {
[2024-06-21 18:06:10] [info] ?concept skos:altLabel ?altLabel
[2024-06-21 18:06:10] [info] FILTER (lang(?altLabel) = 'en') .
[2024-06-21 18:06:10] [info] FILTER(REGEX(STR(?concept),'/concepts/definition/c1116')) .
[2024-06-21 18:06:10] [info] }
[2024-06-21 18:06:10] [info] 2024-06-21T18:06:10.247+02:00 TRACE 34379 --- [Bauhaus-Back-Office] [nio-8080-exec-8] f.i.r.b.rdf_utils.RepositoryUtils        : --- Results ---
[2024-06-21 18:06:10] [info] {
[2024-06-21 18:06:10] [info]   "head" : {
[2024-06-21 18:06:10] [info]     "vars" : [
[2024-06-21 18:06:10] [info]       "altLabel"
[2024-06-21 18:06:10] [info]     ]
[2024-06-21 18:06:10] [info]   },
[2024-06-21 18:06:10] [info]   "results" : {
[2024-06-21 18:06:10] [info]     "bindings" : [ ]
[2024-06-21 18:06:10] [info]   }
[2024-06-21 18:06:10] [info] }
[2024-06-21 18:06:10] [info] 2024-06-21T18:06:10.366+02:00  INFO 34379 --- [Bauhaus-Back-Office] [nio-8080-exec-8] f.i.r.c.freemarker.FreemarkerConfig      : Init freemarker templateloader jar:nested:/opt/cactus/apps/rmesgncs.jar/!BOOT-INF/classes/!/request , null
[2024-06-21 18:06:10] [info] 2024-06-21T18:06:10.517+02:00 TRACE 34379 --- [Bauhaus-Back-Office] [nio-8080-exec-8] f.i.r.b.rdf_utils.RepositoryUtils        : Repo http://dvrmesrdfglm001:8080/repositories/gestion --- Executed query ---
[2024-06-21 18:06:10] [info] PREFIX dcterms:<http://purl.org/dc/terms/>
[2024-06-21 18:06:10] [info] PREFIX xkos:<http://rdf-vocabulary.ddialliance.org/xkos#>
[2024-06-21 18:06:10] [info] PREFIX evoc:<http://eurovoc.europa.eu/schema#>
[2024-06-21 18:06:10] [info] PREFIX skos:<http://www.w3.org/2004/02/skos/core#>
[2024-06-21 18:06:10] [info] PREFIX skosxl:<http://www.w3.org/2008/05/skos-xl#>
[2024-06-21 18:06:10] [info] PREFIX dc:<http://purl.org/dc/elements/1.1/>
[2024-06-21 18:06:10] [info] PREFIX insee:<http://rdf.insee.fr/def/base#>
[2024-06-21 18:06:10] [info] PREFIX geo:<http://www.opengis.net/ont/geosparql#>
[2024-06-21 18:06:10] [info] PREFIX igeo:<http://rdf.insee.fr/def/geo#>
[2024-06-21 18:06:10] [info] PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
[2024-06-21 18:06:10] [info] PREFIX pav:<http://purl.org/pav/>
[2024-06-21 18:06:10] [info] PREFIX foaf:<http://xmlns.com/foaf/0.1/>
[2024-06-21 18:06:10] [info] PREFIX org:<http://www.w3.org/ns/org#>
[2024-06-21 18:06:10] [info] PREFIX prov:<http://www.w3.org/ns/prov#>
[2024-06-21 18:06:10] [info] PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>
[2024-06-21 18:06:10] [info] PREFIX sdmx-mm:<http://www.w3.org/ns/sdmx-mm#>
[2024-06-21 18:06:10] [info] PREFIX qb:<http://purl.org/linked-data/cube#>
[2024-06-21 18:06:10] [info] PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>
[2024-06-21 18:06:10] [info] PREFIX dcat:<http://www.w3.org/ns/dcat#>
[2024-06-21 18:06:10] [info] PREFIX adms: <http://www.w3.org/ns/adms#>
[2024-06-21 18:06:10] [info] PREFIX dcmitype:<http://purl.org/dc/dcmitype/>
[2024-06-21 18:06:10] [info]
[2024-06-21 18:06:10] [info] SELECT ?id ?typeOfLink ?prefLabelLg1 ?prefLabelLg2 ?urn
[2024-06-21 18:06:10] [info] WHERE {
[2024-06-21 18:06:10] [info] #011GRAPH <http://rdf.insee.fr/graphes/concepts/definitions> {
[2024-06-21 18:06:10] [info] #011#011#011#011
[2024-06-21 18:06:10] [info] #011#011?concept rdf:type skos:Concept .
[2024-06-21 18:06:10] [info] #011#011FILTER(REGEX(STR(?concept),'/concepts/definition/c1116')) .
[2024-06-21 18:06:10] [info] #011#011{
[2024-06-21 18:06:10] [info] #011#011#011{
[2024-06-21 18:06:10] [info] #011#011#011#011?concept skos:narrower ?conceptlinked .
[2024-06-21 18:06:10] [info] #011#011#011#011BIND('narrower' AS ?typeOfLink) .
[2024-06-21 18:06:10] [info] #011#011#011}
[2024-06-21 18:06:10] [info] #011#011#011UNION
[2024-06-21 18:06:10] [info] #011#011#011{
[2024-06-21 18:06:10] [info] #011#011#011#011?concept skos:broader ?conceptlinked .
[2024-06-21 18:06:10] [info] #011#011#011#011BIND('broader' AS ?typeOfLink)
[2024-06-21 18:06:10] [info] #011#011#011}
[2024-06-21 18:06:10] [info] #011#011#011UNION
[2024-06-21 18:06:10] [info] #011#011#011{
[2024-06-21 18:06:10] [info] #011#011#011#011?concept dcterms:references ?conceptlinked .
[2024-06-21 18:06:10] [info] #011#011#011#011BIND('references' AS ?typeOfLink)
[2024-06-21 18:06:10] [info] #011#011#011}
[2024-06-21 18:06:10] [info] #011#011#011UNION
[2024-06-21 18:06:10] [info] #011#011#011{
[2024-06-21 18:06:10] [info] #011#011#011#011?concept dcterms:replaces ?conceptlinked .
[2024-06-21 18:06:10] [info] #011#011#011#011BIND('succeed' AS ?typeOfLink)
[2024-06-21 18:06:10] [info] #011#011#011}
[2024-06-21 18:06:10] [info] #011#011#011UNION
[2024-06-21 18:06:10] [info] #011#011#011{
[2024-06-21 18:06:10] [info] #011#011#011#011?concept dcterms:isReplacedBy ?conceptlinked .
[2024-06-21 18:06:10] [info] #011#011#011#011BIND('succeededBy' AS ?typeOfLink)
[2024-06-21 18:06:10] [info] #011#011#011}
[2024-06-21 18:06:10] [info] #011#011#011UNION
[2024-06-21 18:06:10] [info] #011#011#011{
[2024-06-21 18:06:10] [info] #011#011#011#011?concept skos:related ?conceptlinked .
[2024-06-21 18:06:10] [info] #011#011#011#011BIND('related' AS ?typeOfLink)
[2024-06-21 18:06:10] [info] #011#011#011}
[2024-06-21 18:06:10] [info] #011#011#011OPTIONAL{
[2024-06-21 18:06:10] [info] #011#011#011#011?conceptlinked skos:prefLabel ?prefLabelLg1 .
[2024-06-21 18:06:10] [info] #011#011#011#011FILTER (lang(?prefLabelLg1) = 'fr')
[2024-06-21 18:06:10] [info] #011#011#011} .
[2024-06-21 18:06:10] [info] #011#011#011OPTIONAL {
[2024-06-21 18:06:10] [info] #011#011#011#011?conceptlinked skos:prefLabel ?prefLabelLg2 .
[2024-06-21 18:06:10] [info] #011#011#011#011FILTER (lang(?prefLabelLg2) = 'en')
[2024-06-21 18:06:10] [info] #011#011#011} .
[2024-06-21 18:06:10] [info] }
[2024-06-21 18:06:10] [info] #011#011UNION
[2024-06-21 18:06:10] [info] #011#011{
[2024-06-21 18:06:10] [info] #011#011#011?concept skos:closeMatch ?urn .
[2024-06-21 18:06:10] [info] #011#011#011BIND('closeMatch' AS ?typeOfLink)
[2024-06-21 18:06:10] [info] #011#011}
[2024-06-21 18:06:10] [info] #011#011BIND(STRAFTER(STR(?conceptlinked),'/definition/') AS ?id) .
[2024-06-21 18:06:10] [info] #011}
[2024-06-21 18:06:10] [info] }
[2024-06-21 18:06:10] [info] ORDER BY ?typeOfLink
[2024-06-21 18:06:10] [info] 2024-06-21T18:06:10.519+02:00 TRACE 34379 --- [Bauhaus-Back-Office] [nio-8080-exec-8] f.i.r.b.rdf_utils.RepositoryUtils        : --- Results ---
[2024-06-21 18:06:10] [info] {
[2024-06-21 18:06:10] [info]   "head" : {
[2024-06-21 18:06:10] [info]     "vars" : [
[2024-06-21 18:06:10] [info]       "id",
[2024-06-21 18:06:10] [info]       "typeOfLink",
[2024-06-21 18:06:10] [info]       "prefLabelLg1",
[2024-06-21 18:06:10] [info]       "prefLabelLg2",
[2024-06-21 18:06:10] [info]       "urn"
[2024-06-21 18:06:10] [info]     ]
[2024-06-21 18:06:10] [info]   },
[2024-06-21 18:06:10] [info]   "results" : {
[2024-06-21 18:06:10] [info]     "bindings" : [ ]
[2024-06-21 18:06:10] [info]   }
[2024-06-21 18:06:10] [info] }
[2024-06-21 18:06:10] [info] 2024-06-21T18:06:10.551+02:00 TRACE 34379 --- [Bauhaus-Back-Office] [nio-8080-exec-8] f.i.r.b.rdf_utils.RepositoryUtils        : Repo http://dvrmesrdfglm001:8080/repositories/gestion --- Executed query ---
[2024-06-21 18:06:10] [info] PREFIX dcterms:<http://purl.org/dc/terms/>
[2024-06-21 18:06:10] [info] PREFIX xkos:<http://rdf-vocabulary.ddialliance.org/xkos#>
[2024-06-21 18:06:10] [info] PREFIX evoc:<http://eurovoc.europa.eu/schema#>
[2024-06-21 18:06:10] [info] PREFIX skos:<http://www.w3.org/2004/02/skos/core#>
[2024-06-21 18:06:10] [info] PREFIX skosxl:<http://www.w3.org/2008/05/skos-xl#>
[2024-06-21 18:06:10] [info] PREFIX dc:<http://purl.org/dc/elements/1.1/>
[2024-06-21 18:06:10] [info] PREFIX insee:<http://rdf.insee.fr/def/base#>
[2024-06-21 18:06:10] [info] PREFIX geo:<http://www.opengis.net/ont/geosparql#>
[2024-06-21 18:06:10] [info] PREFIX igeo:<http://rdf.insee.fr/def/geo#>
[2024-06-21 18:06:10] [info] PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
[2024-06-21 18:06:10] [info] PREFIX pav:<http://purl.org/pav/>
[2024-06-21 18:06:10] [info] PREFIX foaf:<http://xmlns.com/foaf/0.1/>
[2024-06-21 18:06:10] [info] PREFIX org:<http://www.w3.org/ns/org#>
[2024-06-21 18:06:10] [info] PREFIX prov:<http://www.w3.org/ns/prov#>
[2024-06-21 18:06:10] [info] PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>
[2024-06-21 18:06:10] [info] PREFIX sdmx-mm:<http://www.w3.org/ns/sdmx-mm#>
[2024-06-21 18:06:10] [info] PREFIX qb:<http://purl.org/linked-data/cube#>
[2024-06-21 18:06:10] [info] PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>
[2024-06-21 18:06:10] [info] PREFIX dcat:<http://www.w3.org/ns/dcat#>
[2024-06-21 18:06:10] [info] PREFIX adms: <http://www.w3.org/ns/adms#>
[2024-06-21 18:06:10] [info] PREFIX dcmitype:<http://purl.org/dc/dcmitype/>
[2024-06-21 18:06:10] [info]
[2024-06-21 18:06:10] [info] SELECT ?definitionLg1 ?definitionLg2 ?scopeNoteLg1 ?scopeNoteLg2 ?editorialNoteLg1 ?editorialNoteLg2 ?changeNoteLg1 ?changeNoteLg2
[2024-06-21 18:06:10] [info] WHERE { GRAPH <http://rdf.insee.fr/graphes/concepts/definitions> {
[2024-06-21 18:06:10] [info] ?concept skos:prefLabel ?prefLabelLg1 .
[2024-06-21 18:06:10] [info] FILTER(REGEX(STR(?concept),'/concepts/definition/c1116')) .
[2024-06-21 18:06:10] [info] BIND(STRAFTER(STR(?concept),'/definition/') AS ?id) .
[2024-06-21 18:06:10] [info] OPTIONAL {?concept skos:definition ?defLg1 .
[2024-06-21 18:06:10] [info] ?defLg1 dcterms:language 'fr'^^xsd:language .
[2024-06-21 18:06:10] [info] ?defLg1 evoc:noteLiteral ?definitionLg1 .
[2024-06-21 18:06:10] [info] ?defLg1 insee:conceptVersion '2'^^xsd:int .
[2024-06-21 18:06:10] [info] } .
[2024-06-21 18:06:10] [info] OPTIONAL {?concept skos:definition ?defLg2 .
[2024-06-21 18:06:10] [info] ?defLg2 dcterms:language 'en'^^xsd:language .
[2024-06-21 18:06:10] [info] ?defLg2 evoc:noteLiteral ?definitionLg2 .
[2024-06-21 18:06:10] [info] ?defLg2 insee:conceptVersion '2'^^xsd:int .
[2024-06-21 18:06:10] [info] } .
[2024-06-21 18:06:10] [info] OPTIONAL {?concept skos:scopeNote ?scopeLg1 .
[2024-06-21 18:06:10] [info] ?scopeLg1 dcterms:language 'fr'^^xsd:language .
[2024-06-21 18:06:10] [info] ?scopeLg1 evoc:noteLiteral ?scopeNoteLg1 .
[2024-06-21 18:06:10] [info] ?scopeLg1 insee:conceptVersion '2'^^xsd:int .
[2024-06-21 18:06:10] [info] } .
[2024-06-21 18:06:10] [info] OPTIONAL {?concept skos:scopeNote ?scopeLg2 .
[2024-06-21 18:06:10] [info] ?scopeLg2 dcterms:language 'en'^^xsd:language .
[2024-06-21 18:06:10] [info] ?scopeLg2 evoc:noteLiteral ?scopeNoteLg2 .
[2024-06-21 18:06:10] [info] ?scopeLg2 insee:conceptVersion '2'^^xsd:int .
[2024-06-21 18:06:10] [info] } .
[2024-06-21 18:06:10] [info] OPTIONAL {?concept skos:editorialNote ?editorialLg1 .
[2024-06-21 18:06:10] [info] ?editorialLg1 dcterms:language 'fr'^^xsd:language .
[2024-06-21 18:06:10] [info] ?editorialLg1 evoc:noteLiteral ?editorialNoteLg1 .
[2024-06-21 18:06:10] [info] ?editorialLg1 insee:conceptVersion '2'^^xsd:int .
[2024-06-21 18:06:10] [info] } .
[2024-06-21 18:06:10] [info] OPTIONAL {?concept skos:editorialNote ?editorialLg2 .
[2024-06-21 18:06:10] [info] ?editorialLg2 dcterms:language 'en'^^xsd:language .
[2024-06-21 18:06:10] [info] ?editorialLg2 evoc:noteLiteral ?editorialNoteLg2 .
[2024-06-21 18:06:10] [info] ?editorialLg2 insee:conceptVersion '2'^^xsd:int .
[2024-06-21 18:06:10] [info] } .
[2024-06-21 18:06:10] [info] OPTIONAL {?concept skos:changeNote ?noteChangeLg1 .
[2024-06-21 18:06:10] [info] ?noteChangeLg1 dcterms:language 'fr'^^xsd:language .
[2024-06-21 18:06:10] [info] ?noteChangeLg1 evoc:noteLiteral ?changeNoteLg1 .
[2024-06-21 18:06:10] [info] ?noteChangeLg1 insee:conceptVersion '2'^^xsd:int} .
[2024-06-21 18:06:10] [info] OPTIONAL {?concept skos:changeNote ?noteChangeLg2 .
[2024-06-21 18:06:10] [info] ?noteChangeLg2 dcterms:language 'en'^^xsd:language .
[2024-06-21 18:06:10] [info] ?noteChangeLg2 evoc:noteLiteral ?changeNoteLg2 .
[2024-06-21 18:06:10] [info] ?noteChangeLg2 insee:conceptVersion '2'^^xsd:int} .
[2024-06-21 18:06:10] [info] }}
[2024-06-21 18:06:10] [info] 2024-06-21T18:06:10.553+02:00 TRACE 34379 --- [Bauhaus-Back-Office] [nio-8080-exec-8] f.i.r.b.rdf_utils.RepositoryUtils        : --- Results ---
[2024-06-21 18:06:10] [info] {
[2024-06-21 18:06:10] [info]   "head" : {
[2024-06-21 18:06:10] [info]     "vars" : [
[2024-06-21 18:06:10] [info]       "definitionLg1",
[2024-06-21 18:06:10] [info]       "definitionLg2",
[2024-06-21 18:06:10] [info]       "scopeNoteLg1",
[2024-06-21 18:06:10] [info]       "scopeNoteLg2",
[2024-06-21 18:06:10] [info]       "editorialNoteLg1",
[2024-06-21 18:06:10] [info]       "editorialNoteLg2",
[2024-06-21 18:06:10] [info]       "changeNoteLg1",
[2024-06-21 18:06:10] [info]       "changeNoteLg2"
[2024-06-21 18:06:10] [info]     ]
[2024-06-21 18:06:10] [info]   },
[2024-06-21 18:06:10] [info]   "results" : {
[2024-06-21 18:06:10] [info]     "bindings" : [
[2024-06-21 18:06:10] [info]       {
[2024-06-21 18:06:10] [info]         "definitionLg1" : {
[2024-06-21 18:06:10] [info]           "datatype" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral",
[2024-06-21 18:06:10] [info]           "type" : "literal",
[2024-06-21 18:06:10] [info]           "value" : "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p>Est défini comme accident corporel de la circulation tout accident impliquant au moins un véhicule, survenant sur une voie ouverte à la circulation publique, et dans lequel au moins une personne est blessée ou tuée.</p></div>"
[2024-06-21 18:06:10] [info]         },
[2024-06-21 18:06:10] [info]         "definitionLg2" : {
[2024-06-21 18:06:10] [info]           "datatype" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral",
[2024-06-21 18:06:10] [info]           "type" : "literal",
[2024-06-21 18:06:10] [info]           "value" : "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p>A traffic accident is defined as an accident involving at least one vehicle on a road open to public traffic in which at least one person is injured or killed.</p></div>"
[2024-06-21 18:06:10] [info]         },
[2024-06-21 18:06:10] [info]         "scopeNoteLg1" : {
[2024-06-21 18:06:10] [info]           "datatype" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral",
[2024-06-21 18:06:10] [info]           "type" : "literal",
[2024-06-21 18:06:10] [info]           "value" : "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p>Est défini comme accident corporel de la circulation tout accident impliquant au moins un véhicule, survenant sur une voie ouverte à la circulation publique, et dans lequel au moins une personne est blessée ou tuée.</p></div>"
[2024-06-21 18:06:10] [info]         },
[2024-06-21 18:06:10] [info]         "scopeNoteLg2" : {
[2024-06-21 18:06:10] [info]           "datatype" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral",
[2024-06-21 18:06:10] [info]           "type" : "literal",
[2024-06-21 18:06:10] [info]           "value" : "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p>A traffic accident is defined as an accident involving at least one vehicle on a road open to public traffic in which at least one person is injured or killed.</p></div>"
[2024-06-21 18:06:10] [info]         },
[2024-06-21 18:06:10] [info]         "editorialNoteLg1" : {
[2024-06-21 18:06:10] [info]           "datatype" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral",
[2024-06-21 18:06:10] [info]           "type" : "literal",
[2024-06-21 18:06:10] [info]           "value" : "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p>Les accidents corporels de la circulation sont définis par l'arrêté du 27 mars 2007 relatif aux conditions d'élaboration des statistiques relatives aux accidents corporels de la circulation.</p></div>"
[2024-06-21 18:06:10] [info]         },
[2024-06-21 18:06:10] [info]         "editorialNoteLg2" : {
[2024-06-21 18:06:10] [info]           "datatype" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral",
[2024-06-21 18:06:10] [info]           "type" : "literal",
[2024-06-21 18:06:10] [info]           "value" : "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p>Accidents involving bodily injury are defined by the order of 27 March 2007 relating to the conditions for compiling statistics on accidents involving bodily injury.</p></div>"
[2024-06-21 18:06:10] [info]         },
[2024-06-21 18:06:10] [info]         "changeNoteLg1" : {
[2024-06-21 18:06:10] [info]           "datatype" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral",
[2024-06-21 18:06:10] [info]           "type" : "literal",
[2024-06-21 18:06:10] [info]           "value" : "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p>Ajout définition courte</p></div>"
[2024-06-21 18:06:10] [info]         }
[2024-06-21 18:06:10] [info]       },
[2024-06-21 18:06:10] [info]       {
[2024-06-21 18:06:10] [info]         "definitionLg1" : {
[2024-06-21 18:06:10] [info]           "datatype" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral",
[2024-06-21 18:06:10] [info]           "type" : "literal",
[2024-06-21 18:06:10] [info]           "value" : "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p>Est défini comme accident corporel de la circulation tout accident impliquant au moins un véhicule, survenant sur une voie ouverte à la circulation publique, et dans lequel au moins une personne est blessée ou tuée.</p></div>"
[2024-06-21 18:06:10] [info]         },
[2024-06-21 18:06:10] [info]         "definitionLg2" : {
[2024-06-21 18:06:10] [info]           "datatype" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral",
[2024-06-21 18:06:10] [info]           "type" : "literal",
[2024-06-21 18:06:10] [info]           "value" : "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p>A traffic accident is defined as an accident involving at least one vehicle on a road open to public traffic in which at least one person is injured or killed.</p></div>"
[2024-06-21 18:06:10] [info]         },
[2024-06-21 18:06:10] [info]         "scopeNoteLg1" : {
[2024-06-21 18:06:10] [info]           "datatype" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral",
[2024-06-21 18:06:10] [info]           "type" : "literal",
[2024-06-21 18:06:10] [info]           "value" : "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p>Est défini comme accident corporel de la circulation tout accident impliquant au moins un véhicule, survenant sur une voie ouverte à la circulation publique, et dans lequel au moins une personne est blessée ou tuée.</p></div>"
[2024-06-21 18:06:10] [info]         },
[2024-06-21 18:06:10] [info]         "scopeNoteLg2" : {
[2024-06-21 18:06:10] [info]           "datatype" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral",
[2024-06-21 18:06:10] [info]           "type" : "literal",
[2024-06-21 18:06:10] [info]           "value" : "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p>A traffic accident is defined as an accident involving at least one vehicle on a road open to public traffic in which at least one person is injured or killed.</p></div>"
[2024-06-21 18:06:10] [info]         },
[2024-06-21 18:06:10] [info]         "editorialNoteLg1" : {
[2024-06-21 18:06:10] [info]           "datatype" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral",
[2024-06-21 18:06:10] [info]           "type" : "literal",
[2024-06-21 18:06:10] [info]           "value" : "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p>Les accidents corporels de la circulation sont définis par l'arrêté du 27 mars 2007 relatif aux conditions d'élaboration des statistiques relatives aux accidents corporels de la circulation.</p></div>"
[2024-06-21 18:06:10] [info]         },
[2024-06-21 18:06:10] [info]         "editorialNoteLg2" : {
[2024-06-21 18:06:10] [info]           "datatype" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral",
[2024-06-21 18:06:10] [info]           "type" : "literal",
[2024-06-21 18:06:10] [info]           "value" : "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p>Accidents involving bodily injury are defined by the order of 27 March 2007 relating to the conditions for compiling statistics on accidents involving bodily injury.</p></div>"
[2024-06-21 18:06:10] [info]         },
[2024-06-21 18:06:10] [info]         "changeNoteLg1" : {
[2024-06-21 18:06:10] [info]           "datatype" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral",
[2024-06-21 18:06:10] [info]           "type" : "literal",
[2024-06-21 18:06:10] [info]           "value" : "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p>Ajout définition courte</p></div>"
[2024-06-21 18:06:10] [info]         }
[2024-06-21 18:06:10] [info]       }
[2024-06-21 18:06:10] [info]     ]
[2024-06-21 18:06:10] [info]   }
[2024-06-21 18:06:10] [info] }
[2024-06-21 18:06:12] [info] 2024-06-21T18:06:12.407+02:00  INFO 34379 --- [Bauhaus-Back-Office] [nio-8080-exec-8] fr.insee.rmes.config.LogRequestFilter    : END From gestion-metadonnees-api.developpement2.insee.fr by user xrmfux DR59-SNDI59 call GET /concepts/concept/export/c1116
 */



    @Test
    void exportConceptTest() throws RmesException {
        ConceptsImpl concepts = new ConceptsImpl(null, null, new ConceptsExportBuilder(conceptsUtils, new ExportUtils(200, null)), null, 200);
        assertThat(concepts.exportConcept("c1116", MediaType.APPLICATION_OCTET_STREAM_VALUE)).isNotNull();
    }

}