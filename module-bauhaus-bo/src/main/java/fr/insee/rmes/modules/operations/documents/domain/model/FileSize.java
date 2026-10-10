package fr.insee.rmes.modules.operations.documents.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Taille d'un fichier, stockée en octets ({@code dcterms:extent}).
 * <p>
 * La forme lisible utilise les unités SI (1 ko = 1 000 octets) : la plus grande unité qui garde la
 * valeur sous 1 000, arrondie à une décimale, virgule décimale, sans « ,0 » final.
 */
public record FileSize(long bytes) {

    public static final FileSize ZERO = new FileSize(0);

    private static final List<String> UNITS = List.of("o", "ko", "Mo", "Go", "To");
    private static final BigDecimal THOUSAND = BigDecimal.valueOf(1000);

    public FileSize {
        if (bytes < 0) {
            throw new IllegalArgumentException("A file size cannot be negative: " + bytes);
        }
    }

    public String humanReadable() {
        BigDecimal value = BigDecimal.valueOf(bytes);
        int unit = 0;
        while (unit < UNITS.size() - 1 && rounded(value).compareTo(THOUSAND) >= 0) {
            value = value.divide(THOUSAND);
            unit++;
        }
        String number = rounded(value).stripTrailingZeros().toPlainString().replace('.', ',');
        return number + " " + UNITS.get(unit);
    }

    private static BigDecimal rounded(BigDecimal value) {
        return value.setScale(1, RoundingMode.HALF_UP);
    }
}
