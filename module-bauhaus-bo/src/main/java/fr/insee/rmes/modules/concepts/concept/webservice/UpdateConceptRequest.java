package fr.insee.rmes.modules.concepts.concept.webservice;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.insee.rmes.modules.concepts.concept.domain.exceptions.InvalidConceptIdException;
import fr.insee.rmes.modules.concepts.concept.domain.exceptions.InvalidCreateConceptCommandException;
import fr.insee.rmes.modules.concepts.concept.domain.model.commands.UpdateConceptCommand;
import java.util.Collections;
import java.util.List;

public class UpdateConceptRequest extends CreateConceptRequest {

    @JsonCreator
    public UpdateConceptRequest(
            @JsonProperty("prefLabelLg1") String prefLabelLg1,
            @JsonProperty("prefLabelLg2") String prefLabelLg2,
            @JsonProperty("creator") String creator,
            @JsonProperty("contributor") String contributor,
            @JsonProperty("disseminationStatus") String disseminationStatus,
            @JsonProperty("collections") List<String> collections) {
        super(prefLabelLg1, prefLabelLg2, creator, contributor, disseminationStatus, collections);
    }

    public UpdateConceptCommand toUpdateCommand(String id)
            throws InvalidCreateConceptCommandException, InvalidConceptIdException {
        return new UpdateConceptCommand(
                id,
                buildLabels(),
                creator,
                contributor,
                disseminationStatus,
                collections == null ? Collections.emptyList() : collections);
    }
}
