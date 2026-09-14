package fr.insee.rmes.modules.commons.configuration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Conditional;

/**
 * Conditional annotation that checks if a specific module is active.
 * A module is active when it is declared in {@code fr.insee.rmes.bauhaus.modules};
 * to deactivate it, remove it from that list.
 *
 * <p>Example usage:</p>
 * <pre>
 * {@literal @}ConditionalOnModule("ddi")
 * public class DdiResources {
 *     // This class will only be loaded if 'ddi' is declared in modules
 * }
 * </pre>
 *
 * <p>Example configuration:</p>
 * <pre>
 * fr.insee.rmes.bauhaus.modules[0].identifier=concepts
 * fr.insee.rmes.bauhaus.modules[1].identifier=ddi
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
