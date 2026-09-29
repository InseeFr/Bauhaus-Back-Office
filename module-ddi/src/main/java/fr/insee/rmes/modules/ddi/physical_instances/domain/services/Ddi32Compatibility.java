package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import java.util.regex.Pattern;

/**
 * Des PhysicalInstances importées dans Colectica contiennent des items DDI 3.2. Leur structure est
 * traitée comme celle du DDI 3.3 : seuls les espaces de noms ({@code ddi:instance:3_2},
 * {@code ddi:reusable:3_2}…) sont réécrits en 3.3 avant le parsing XmlBeans, qui refuserait sinon
 * le document.
 */
public final class Ddi32Compatibility {

    private static final Pattern DDI_32_NAMESPACE = Pattern.compile("\"(ddi:[A-Za-z]+:)3_2\"");

    private Ddi32Compatibility() {}

    /** Le fragment avec ses espaces de noms DDI 3.2 réécrits en 3.3 ; inchangé s'il est déjà en 3.3. */
    public static String asDdi33(String fragmentXml) {
        if (fragmentXml == null) {
            return null;
        }
        return DDI_32_NAMESPACE.matcher(fragmentXml).replaceAll("\"$13_3\"");
    }
}
