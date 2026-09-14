package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

/**
 * Identifiants imposés par l'appelant lors de la création d'une PhysicalInstance, au lieu des UUID
 * aléatoires tirés par défaut.
 *
 * <p>Seul l'init local ({@code fr.insee.rmes.bauhaus.colectica.init=true}) s'en sert, avec des ids
 * déterministes dérivés du seed de la StudyUnit : une relance réécrit alors les mêmes items au lieu
 * d'en créer de nouveaux et de laisser les précédents orphelins dans Colectica.
 */
public record PhysicalInstanceIds(String physicalInstance, String dataRelationship, String logicalRecord) {}
