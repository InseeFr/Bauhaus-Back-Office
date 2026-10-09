package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class MutualizedCodeListCodesTest {

    private static Code code(String id, String value, String categoryId) {
        return new Code(
                Code.TYPE,
                "urn:ddi:fr.insee:" + id + ":1",
                "fr.insee",
                id,
                "1",
                categoryId == null ? null : Reference.of("fr.insee", categoryId, "1", "Category"),
                ValueType.of(value),
                null);
    }

    private static Ddi4Category category(String id, List<LangString> label) {
        return new Ddi4Category(Ddi4Category.TYPE, null, "urn:ddi:fr.insee:" + id + ":1", "fr.insee", id, "1", label);
    }

    @Test
    void keepsTheValueAndTheCategoryLabelOfEachCodeInOrder() {
        Ddi4CodeList codeList = new Ddi4CodeList(
                Ddi4CodeList.TYPE,
                null,
                "urn:ddi:fr.insee:cl-1:3",
                "fr.insee",
                "cl-1",
                "3",
                List.of(new LangString("en-GB", "Activities"), new LangString("fr-FR", "Activités")),
                null,
                List.of(code("c-2", "02", "cat-2"), code("c-1", "01", "cat-1"), code("c-3", "03", null)));
        Ddi4Response response = new Ddi4Response(
                Ddi4Response.SCHEMA,
                List.of(Reference.of("fr.insee", "cl-1", "3", "CodeList")),
                null,
                null,
                null,
                List.of(codeList),
                List.of(
                        category(
                                "cat-1",
                                List.of(new LangString("en-GB", "Farming"), new LangString("fr-FR", "Agriculture"))),
                        category("cat-2", LangStrings.of("en-GB", "Industry"))),
                null);

        MutualizedCodeListCodes result = MutualizedCodeListCodes.from(response, "fr-FR");

        assertThat(result)
                .isEqualTo(new MutualizedCodeListCodes(
                        "fr.insee",
                        "cl-1",
                        "3",
                        "Activités",
                        List.of(
                                new MutualizedCodeListCodes.Entry("c-2", "02", "Industry"),
                                new MutualizedCodeListCodes.Entry("c-1", "01", "Agriculture"),
                                new MutualizedCodeListCodes.Entry("c-3", "03", ""))));
    }

    @Test
    void isNullWithoutCodeList() {
        assertThat(MutualizedCodeListCodes.from(
                        new Ddi4Response(Ddi4Response.SCHEMA, null, null, null, null, null, null, null), "fr-FR"))
                .isNull();
    }
}
