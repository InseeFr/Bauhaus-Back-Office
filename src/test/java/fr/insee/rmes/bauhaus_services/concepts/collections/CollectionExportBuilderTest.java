package fr.insee.rmes.bauhaus_services.concepts.collections;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CollectionExportBuilderTest {

    String keyName = "prefLabelLg1";
    JSONObject members1 = new JSONObject().put("id","members1").put(keyName,"en");
    JSONObject members2 = new JSONObject().put("id","members2").put(keyName,"fr");
    JSONObject members3 = new JSONObject().put("id","members3").put(keyName,"en");
    JSONObject members4 = new JSONObject().put("id","members4").put(keyName,"fr");

    @Test
    void shouldCompareTwoJsonObjects() {

        Collator instance = Collator.getInstance();

        String valA = (String) members1.get(keyName);
        String valB = (String) members2.get(keyName);
        String valC = (String) members3.get(keyName);
        String valD = (String) members4.get(keyName);

        List<Integer> actual = List.of(instance.compare(valA.toLowerCase(), valB.toLowerCase()),
        instance.compare(valB.toLowerCase(), valC.toLowerCase()),
        instance.compare(valA.toLowerCase(), valC.toLowerCase()),
        instance.compare(valB.toLowerCase(), valD.toLowerCase()));

        List<Integer> expected = List.of(-1,1,0,0);

        assertEquals(expected,actual);
    }

    @Test
    void shouldSortJsonObjects() {

        List<JSONObject> orderMembers= List.of(members1,members3,members2,members4);

        List<JSONObject>  notOrderMembers = new ArrayList<>();
        notOrderMembers.add(members1);
        notOrderMembers.add(members2);
        notOrderMembers.add(members3);
        notOrderMembers.add(members4);

        notOrderMembers.sort(new Comparator<>() {
            private static final String KEY_NAME = "prefLabelLg1";
            final Collator instance = Collator.getInstance();

            @Override
            public int compare(JSONObject a, JSONObject b) {
                String valA = (String) a.get(KEY_NAME);
                String valB = (String) b.get(KEY_NAME);

                return instance.compare(valA.toLowerCase(), valB.toLowerCase());
            }
        });

        assertEquals(orderMembers,notOrderMembers);
}

}
