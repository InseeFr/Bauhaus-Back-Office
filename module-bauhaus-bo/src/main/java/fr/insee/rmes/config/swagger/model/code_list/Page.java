package fr.insee.rmes.config.swagger.model.code_list;


import com.fasterxml.jackson.annotation.JsonProperty;
import fr.insee.rmes.bauhaus_services.code_list.CodeListItem;

import java.util.List;


public class Page {
    @JsonProperty
    public int total;

    @JsonProperty
    public int page;

    @JsonProperty
    public List<CodeListItem> items;


    public int getPage() {
        return page;
    }

    public int getTotal() {
        return total;
    }

    public List<CodeListItem> getItems() {
        return items;
    }
}
