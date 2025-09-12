package fr.insee.rmes.domain.model.operations;

import org.json.JSONObject;

public record DocumentationAttribute (
        String rangeType,
        String masLabelLg1,
        String masLabelLg2,
        String id,
        String maxOccurs,
        Boolean isPresentational,
        Boolean sansObject
) {

    public static DocumentationAttribute fromJson(JSONObject obj) {
        String rt = obj.optString("rangeType", null);
        String lg1 = obj.optString("masLabelLg1", null);
        String lg2 = obj.optString("masLabelLg2", null);
        String id  = obj.optString("id", null);
        String max = obj.optString("maxOccurs", null);
        Boolean presentational = toNullableBoolean(obj.opt("isPresentational"));
        Boolean sansObject = obj.optBooleanObject("sansObject");

        return new DocumentationAttribute(rt, lg1, lg2, id, max, presentational, sansObject);
    }

    private static Boolean toNullableBoolean(Object v) {
        if (v == null) return false;
        if (v instanceof Boolean b) return b;
        return Boolean.parseBoolean(String.valueOf(v));
    }
}