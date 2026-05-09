package fr.insee.rmes.domain.model.operations.families;

import org.json.JSONObject;

public record OperationFamilySubject(
        String id,
        String labelLg1,
        String labelLg2
) {
    public static OperationFamilySubject fromJSON(JSONObject obj){
        String id = obj.optString("id", null);
        String labelLg1 = obj.optString("labelLg1", null);
        String labelLg2 = obj.optString("labelLg2", null);

        return new OperationFamilySubject(id, labelLg1, labelLg2);
    }
}