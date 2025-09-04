package fr.insee.rmes.onion.domain.model.operations;

import org.json.JSONObject;

public record OperationFamilySeries(
        String id,
        String labelLg1,
        String labelLg2
) {
    public static OperationFamilySeries fromJSON(JSONObject obj){
        String id = obj.optString("id", null);
        String labelLg1 = obj.optString("labelLg1", null);
        String labelLg2 = obj.optString("labelLg2", null);

        return new OperationFamilySeries(id, labelLg1, labelLg2);
    }
}
