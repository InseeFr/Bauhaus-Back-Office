package fr.insee.rmes.exceptions.errors;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import static org.junit.jupiter.api.Assertions.*;

class CodesListErrorCodesTest {

    @Test
    void shouldCheckIntegerCodesListErrorCodesAreNotUniqueness() {

        List<Integer> actual = List.of(
                CodesListErrorCodes.CODE_LIST_UNICITY,
                CodesListErrorCodes.CODE_LIST_AT_LEAST_ONE_CODE,
                CodesListErrorCodes.CODE_LIST_DELETE_ONLY_UNPUBLISHED,
                CodesListErrorCodes.CODE_LIST_DELETE_CODELIST_WITHOUT_PARTIAL,
                CodesListErrorCodes.CODE_LIST_UNKNOWN_ID,
                CodesListErrorCodes.STRUCTURE_DELETE_ONLY_UNPUBLISHED);

        SortedSet<Integer> expected = new TreeSet<>();
        expected.addAll(actual);
        boolean existDuplicates = expected.size()!= actual.size();
        boolean existSameCode = CodesListErrorCodes.CODE_LIST_DELETE_ONLY_UNPUBLISHED==CodesListErrorCodes.STRUCTURE_DELETE_ONLY_UNPUBLISHED;

        assertTrue( existDuplicates && existSameCode);

    }

}