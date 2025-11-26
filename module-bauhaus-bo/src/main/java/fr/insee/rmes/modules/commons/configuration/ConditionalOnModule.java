package fr.insee.rmes.modules.commons.configuration;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Conditional annotation that checks if a specific module is active.
 * A module is considered active if it is contained in the property
 * {@code fr.insee.rmes.bauhaus.activeModules}.
 *
 * <p>Example usage:</p>
 * <pre>
 * {@literal @}ConditionalOnModule("ddi")
 * public class DdiResources {
 *     // This class will only be loaded if 'ddi' is in activeModules
 * }
 * </pre>
 *
 * <p>The property can contain multiple modules separated by commas:</p>
 * <pre>
 * fr.insee.rmes.bauhaus.activeModules=concepts,ddi,operations
 * </pre>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnModuleCondition.class)
public @interface ConditionalOnModule {
    /**
     * The module name to check for in the active modules property.
     * @return the module name
     */
    String value();
}