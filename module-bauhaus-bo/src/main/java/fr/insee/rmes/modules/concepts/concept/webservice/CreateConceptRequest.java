package fr.insee.rmes.modules.concepts.concept.webservice;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.insee.rmes.modules.concepts.concept.domain.exceptions.InvalidCreateConceptCommandException;
import fr.insee.rmes.modules.concepts.concept.domain.model.commands.CreateConceptCommand;
import fr.insee.rmes.modules.concepts.concept.webservice.response.LocalisedLabelResponse;
import fr.insee.rmes.modules.shared_kernel.domain.model.Lang;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CreateConceptRequest {

    protected final String prefLabelLg1;
    protected final String prefLabelLg2;
    protected final String creator;
    protected final String contributor;
    protected final String disseminationStatus;
    protected final List<String> collections;

    @JsonCreator
    public CreateConceptRequest(
            @JsonProperty("prefLabelLg1") String prefLabelLg1,
            @JsonProperty("prefLabelLg2") String prefLabelLg2,
            @JsonProperty("creator") String creator,
            @JsonProperty("contributor") String contributor,
            @JsonProperty("disseminationStatus") String disseminationStatus,
            @JsonProperty("collections") List<String> collections) {
        this.prefLabelLg1 = prefLabelLg1;
        this.prefLabelLg2 = prefLabelLg2;
        this.creator = creator;
        this.contributor = contributor;
        this.disseminationStatus = disseminationStatus;
        this.collections = collections;
    }

    public CreateConceptCommand toCommand() throws InvalidCreateConceptCommandException {
        return new CreateConceptCommand(
                buildLabels(),
                creator,
                contributor,
                disseminationStatus,
                collections == null ? Collections.emptyList() : collections);
    }

    protected List<LocalisedLabel> buildLabels() {
        List<LocalisedLabel> labels = new ArrayList<>();
        if (prefLabelLg1 != null && !prefLabelLg1.isEmpty()) {
            labels.add(new LocalisedLabel(prefLabelLg1, Lang.defaultLanguage()));
        }
        if (prefLabelLg2 != null && !prefLabelLg2.isEmpty()) {
            labels.add(new LocalisedLabel(prefLabelLg2, Lang.alternativeLanguage()));
        }
        return labels;
    }

    public String prefLabelLg1() {
        return prefLabelLg1;
    }

    public String prefLabelLg2() {
        return prefLabelLg2;
    }

    public String creator() {
        return creator;
    }

    public String contributor() {
        return contributor;
    }

    public String disseminationStatus() {
        return disseminationStatus;
    }

    public List<String> collections() {
        return collections;
    }

    public List<LocalisedLabelResponse> labels() {
        return buildLabels().stream().map(LocalisedLabelResponse::fromDomain).toList();
    }
}
