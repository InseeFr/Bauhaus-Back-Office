package fr.insee.rmes.modules.commons.healthcheck;

/**
 * Sonde d'un service externe affichée par le healthcheck : {@code check} lève une exception quand le
 * service ne répond pas. Les sondes sont des beans, déclarés là où le client du service est construit.
 */
public record DependencyProbe(String name, Check check) {

    @FunctionalInterface
    public interface Check {
        void run() throws Exception;
    }
}
