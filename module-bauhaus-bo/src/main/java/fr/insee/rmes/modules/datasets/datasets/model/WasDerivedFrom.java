package fr.insee.rmes.modules.datasets.datasets.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * Lignage d'un jeu de données : les jeux dont il est construit ({@code prov:wasDerivedFrom}) et,
 * facultativement, une description commune de la construction ({@code prov:qualifiedDerivation}).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record WasDerivedFrom(List<String> datasets, String descriptionLg1, String descriptionLg2) {}
