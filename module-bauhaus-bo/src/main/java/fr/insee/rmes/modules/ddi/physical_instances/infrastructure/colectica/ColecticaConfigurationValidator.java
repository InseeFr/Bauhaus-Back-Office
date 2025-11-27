package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Validates the Colectica configuration at application startup.
 *
 * <p>This validator ensures that all deny list entries are properly configured
 * with non-null and non-empty values for both agencyId and id fields.
 *
 * <p>If validation fails, the application will fail to start with a clear
 * error message indicating which configuration entry is invalid.
 *
 * @see ColecticaConfiguration
 * @see ColecticaConfiguration.CodeListDenyEntry
 */
@Configuration
public class ColecticaConfigurationValidator {

    private static final Logger logger = LoggerFactory.getLogger(ColecticaConfigurationValidator.class);

    /**
     * Validates the Colectica configuration and returns a validator bean.
     *
     * <p>This method is called during Spring context initialization and will
     * throw an IllegalStateException if any validation errors are found.
     *
     * @param config The Colectica configuration to validate
     * @return A validator bean instance (used as a marker for successful validation)
     * @throws IllegalStateException if the configuration is invalid
     */
    @Bean
    public ColecticaConfigurationValidatorBean validateColecticaConfiguration(
            ColecticaConfiguration config) {

        logger.info("Validating Colectica configuration...");

        if (config == null) {
            logger.warn("Colectica configuration is null, skipping validation");
            return new ColecticaConfigurationValidatorBean();
        }

        // Validate deny list entries
        if (config.codeListDenyList() != null && !config.codeListDenyList().isEmpty()) {
            logger.info("Validating {} code list deny list entries", config.codeListDenyList().size());

            int entryIndex = 0;
            for (ColecticaConfiguration.CodeListDenyEntry entry : config.codeListDenyList()) {
                validateDenyListEntry(entry, entryIndex);
                entryIndex++;
            }

            logger.info("Successfully validated {} code list deny list entries", config.codeListDenyList().size());
        } else {
            logger.info("No code list deny list entries configured");
        }

        logger.info("Colectica configuration validation completed successfully");
        return new ColecticaConfigurationValidatorBean();
    }

    /**
     * Validates a single deny list entry.
     *
     * @param entry The entry to validate
     * @param index The index of the entry in the list (for error messages)
     * @throws IllegalStateException if the entry is invalid
     */
    private void validateDenyListEntry(ColecticaConfiguration.CodeListDenyEntry entry, int index) {
        if (entry == null) {
            throw new IllegalStateException(
                    String.format("Code list deny list entry at index %d is null", index)
            );
        }

        if (entry.agencyId() == null || entry.agencyId().isBlank()) {
            throw new IllegalStateException(
                    String.format("Code list deny list entry at index %d has null or empty agencyId: %s",
                            index, entry)
            );
        }

        if (entry.id() == null || entry.id().isBlank()) {
            throw new IllegalStateException(
                    String.format("Code list deny list entry at index %d has null or empty id: %s",
                            index, entry)
            );
        }

        logger.debug("Validated deny list entry [{}]: agencyId={}, id={}", index, entry.agencyId(), entry.id());
    }

    /**
     * Marker bean class to indicate successful configuration validation.
     *
     * <p>This bean is created only if all validation checks pass.
     */
    public static class ColecticaConfigurationValidatorBean {
        // Marker class - no implementation needed
    }
}
