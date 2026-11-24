package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Le corps DDI 4 du remplacement d'une PhysicalInstance doit porter la PhysicalInstance qu'il
 * remplace. Posée sur le paramètre plutôt que sur {@link Ddi4Response} : le domaine ne dépend pas de
 * Bean Validation. L'erreur porte donc sur le corps entier.
 */
@Documented
@Constraint(validatedBy = ContainsPhysicalInstance.Validator.class)
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface ContainsPhysicalInstance {

    String message() default "physicalInstance is required";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<ContainsPhysicalInstance, Ddi4Response> {
        @Override
        public boolean isValid(Ddi4Response value, ConstraintValidatorContext context) {
            return value == null
                    || (value.physicalInstance() != null
                            && !value.physicalInstance().isEmpty());
        }
    }
}
