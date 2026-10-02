package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.Test;

class ColecticaDatesTest {

    @Test
    void parseInstant_honoursTheOffsetStampedByBauhaus() {
        assertThat(ColecticaDates.parseInstant("2026-10-02T12:00:00.123+02:00"))
                .isEqualTo(Date.from(Instant.parse("2026-10-02T10:00:00.123Z")));
    }

    @Test
    void parseInstant_readsADateTimeWithoutOffsetAsUtc() {
        assertThat(ColecticaDates.parseInstant("2026-10-02T10:00:00"))
                .isEqualTo(Date.from(Instant.parse("2026-10-02T10:00:00Z")));
    }

    @Test
    void parseInstant_returnsNullForAnUnreadableValue() {
        assertThat(ColecticaDates.parseInstant("pas une date")).isNull();
    }

    @Test
    void parseInstant_returnsNullForAMissingValue() {
        assertThat(ColecticaDates.parseInstant(null)).isNull();
    }
}
