package fr.insee.rmes.model.concepts;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CollectionForExportTest {

    private CollectionForExport collectionForExport;

    @BeforeEach
    public void setUp() {
        collectionForExport = new CollectionForExport();
    }

    @Test
    public void testAddMembers() {
        JSONArray membersArray = new JSONArray();

        JSONObject member1 = new JSONObject();
        member1.put("id", "1");
        member1.put("prefLabelLg1", "Membre 1 Label Lg1");
        member1.put("prefLabelLg2", "Membre 1 Label Lg2");
        member1.put("creator", "Créateur 1");

        JSONObject member2 = new JSONObject();
        member2.put("id", "2");
        member2.put("prefLabelLg1", "Membre 2 Label Lg1");

        membersArray.put(member1);
        membersArray.put(member2);

        collectionForExport.addMembers(membersArray);

        List<MembersLg> membersLg = collectionForExport.getMembersLg();
        assertEquals(2, membersLg.size());

        MembersLg firstMember = membersLg.get(0);
        assertEquals("1", firstMember.getId());
        assertEquals("Membre 1 Label Lg1", firstMember.getPrefLabelLg1());
        assertEquals("Membre 1 Label Lg2", firstMember.getPrefLabelLg2());
        assertEquals("Créateur 1", firstMember.getCreator());

        MembersLg secondMember = membersLg.get(1);
        assertEquals("2", secondMember.getId());
        assertEquals("Membre 2 Label Lg1", secondMember.getPrefLabelLg1());
        assertEquals(null, secondMember.getPrefLabelLg2()); // prefLabelLg2 n'existe pas
        assertEquals(null, secondMember.getCreator()); // creator n'existe pas
    }
}