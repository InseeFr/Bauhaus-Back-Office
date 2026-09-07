package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Conversions de dates entre le format Colectica et le modèle. */
final class ColecticaDates {

    private static final Logger logger = LoggerFactory.getLogger(ColecticaDates.class);

    private static final String COLECTICA_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    private ColecticaDates() {
    }

    /**
     * Parse un instant Colectica {@code yyyy-MM-dd'T'HH:mm:ss} en UTC. Une éventuelle fraction de
     * seconde ({@code …:32.961778}, émise par {@code _query/advanced}) est ignorée, pour rester
     * aligné sur le listing historique. {@code null} si la valeur est absente ou illisible.
     */
    static Date parse(String raw) {
        if (raw == null) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(COLECTICA_PATTERN);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return formatter.parse(raw);
        } catch (ParseException _) {
            logger.debug("Impossible to parse {}", raw);
            return null;
        }
    }

    /** Horodatage courant au format ISO offset, tel que Colectica l'attend en {@code versionDate}. */
    static String nowIso() {
        return ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
