package fr.insee.rmes.modules.operations.documents.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FileSizeTest {

    @ParameterizedTest(name = "{0} octets -> {1}")
    @CsvSource(
            delimiter = '|',
            value = {
                "0             | 0 o",
                "512           | 512 o",
                "999           | 999 o",
                "1000          | 1 ko",
                "130048        | 130 ko",
                "1500          | 1,5 ko",
                "999949        | 999,9 ko",
                "1234567       | 1,2 Mo",
                "20000000      | 20 Mo",
                "3400000000    | 3,4 Go",
                "5000000000000 | 5 To"
            })
    void should_be_expressed_in_the_largest_unit_keeping_the_value_under_1000(long bytes, String expected) {
        assertThat(new FileSize(bytes).humanReadable()).isEqualTo(expected);
    }

    @Test
    void should_switch_to_the_next_unit_when_rounding_reaches_1000() {
        assertThat(new FileSize(999_999).humanReadable()).isEqualTo("1 Mo");
    }

    @Test
    void should_reject_a_negative_size() {
        assertThatThrownBy(() -> new FileSize(-1)).isInstanceOf(IllegalArgumentException.class);
    }
}
