package fr.insee.rmes.config.swagger.model.code_list;


import com.fasterxml.jackson.annotation.JsonProperty;
import fr.insee.rmes.bauhaus_services.code_list.CodeListItem;

import java.util.List;


public class Page {
    @JsonProperty
    public String total;

    @JsonProperty
    public String page;

    @JsonProperty
    public List<CodeListItem> items;


    public String getPage() {
        return page;
    }

    public String getTotal() {
        return total;
    }

    public List<CodeListItem> getItems() {
        return items;
    }
}
