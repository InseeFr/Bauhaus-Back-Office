package fr.insee.rmes.modules.shared_kernel.domain.model;

/**
 * Les deux langues configurées pour l'instance, transportées ensemble plutôt que sous la forme
 * de deux {@code String} interchangeables.
 */
public record ConfiguredLanguages(String lg1, String lg2) {}
