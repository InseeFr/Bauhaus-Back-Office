package fr.insee.rmes.utils;

import fr.insee.rmes.domain.exceptions.RmesException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.Assert.assertThrows;

class DeserializerTest {

    JSONObject jsonObjectFirst = new JSONObject().put("id","2025").put("creator","Unknown");
    JSONObject jsonObjectSecond = new JSONObject().put("id","2026");
    JSONArray jsonArray = new JSONArray().put(jsonObjectFirst).put(jsonObjectSecond);

    @ParameterizedTest
    @ValueSource(classes = { String.class, Integer.class})
    void shouldNotDeserializeJsonString(Class target){
        assertThrows(RmesException.class, () -> Deserializer.deserializeJsonString(jsonObjectFirst.toString(),target));
    }

    @ParameterizedTest
    @ValueSource(classes = { String.class, Integer.class})
    void shouldNotDeserializeJSONArray(Class target) {
        assertThrows(RmesException.class, () -> Deserializer.deserializeJSONArray(jsonArray,target));
    }

    @ParameterizedTest
    @ValueSource(classes = { String.class, Integer.class})
    void shouldNotDeserializeJSONObject(Class target){
        assertThrows(RmesException.class, () -> Deserializer.deserializeJSONObject(jsonObjectSecond,target));
    }

}