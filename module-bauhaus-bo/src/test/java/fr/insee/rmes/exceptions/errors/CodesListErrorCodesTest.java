package fr.insee.rmes.exceptions.errors;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class CodesListErrorCodesTest {

    @Test
    void shouldGiveEachCodesListErrorItsOwnCode() {
        List<Integer> codes = List.of(
                CodesListErrorCodes.CODE_LIST_UNICITY,
                CodesListErrorCodes.CODE_LIST_AT_LEAST_ONE_CODE,
                CodesListErrorCodes.CODE_LIST_DELETE_ONLY_UNPUBLISHED,
                CodesListErrorCodes.CODE_LIST_DELETE_CODELIST_WITHOUT_PARTIAL,
                CodesListErrorCodes.CODE_LIST_UNKNOWN_ID);

        assertThat(codes).doesNotHaveDuplicates();
    }
}
