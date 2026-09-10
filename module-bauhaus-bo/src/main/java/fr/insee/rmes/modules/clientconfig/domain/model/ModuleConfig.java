package fr.insee.rmes.modules.clientconfig.domain.model;

/**
 * Un module actif, tel qu'il est annoncé au front : son identifiant et les deux réglages
 * résolus par {@link ModuleSettings}. Les modules désactivés n'apparaissent pas.
 */
public record ModuleConfig(String identifier, boolean show, boolean directAccess) {}
