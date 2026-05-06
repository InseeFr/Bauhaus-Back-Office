package fr.insee.rmes.modules.users.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StampTest {

    @Test
    void to_string_returns_stamp_value_so_it_can_be_interpolated_in_queries() {
        assertThat(new Stamp("HIE2000069")).hasToString("HIE2000069");
    }
}
