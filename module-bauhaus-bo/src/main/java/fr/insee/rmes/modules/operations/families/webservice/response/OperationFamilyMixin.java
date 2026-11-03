package fr.insee.rmes.modules.operations.families.webservice.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.insee.rmes.modules.operations.families.domain.model.OperationFamily;
import java.util.List;
import org.springframework.boot.jackson.JacksonMixin;

@JacksonMixin(OperationFamily.class)
@JsonIgnoreProperties(value = {"series", "subjects"})
public abstract class OperationFamilyMixin {

    /** Même clé que dans le corps des requêtes d'écriture, et que pour les autres objets. */
    @JsonProperty("contributor")
    abstract List<String> contributors();
}
