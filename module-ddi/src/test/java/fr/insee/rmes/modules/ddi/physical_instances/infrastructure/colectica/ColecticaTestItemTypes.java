package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import java.util.HashMap;
import java.util.Map;

/**
 * Copie, pour les tests, de la map {@code itemTypes} de {@code colectica.yml} (module-bauhaus-bo,
 * hors classpath de ce module). Un stub de {@code itemTypes()} part de cette map et n'y surcharge
 * que les clés propres au test, comme le ferait la configuration réelle.
 */
final class ColecticaTestItemTypes {

    static final Map<String, String> CONFIGURED = Map.ofEntries(
            Map.entry("PhysicalInstance", "a51e85bb-6259-4488-8df2-f08cb43485f8"),
            Map.entry("LogicalProduct", "965c8d28-7d48-4950-bea7-04b27e52bb9b"),
            Map.entry("DataRelationship", "f39ff278-8500-45fe-a850-3906da2d242b"),
            Map.entry("Variable", "683889c6-f74b-4d5e-92ed-908c0a42bb2d"),
            Map.entry("CodeList", "8b108ef8-b642-4484-9c49-f88e4bf7cf1d"),
            Map.entry("CodeListScheme", "4193d389-b5ae-4368-b399-cd5a7ee3653c"),
            Map.entry("CodeListGroup", "394b9ff3-7248-4ede-b945-9bebdbf56bed"),
            Map.entry("Category", "7e47c269-bcab-40f7-a778-af7bbc4e3d00"),
            Map.entry("CategoryScheme", "1c11de94-a36d-4d80-95dc-950c6f37f624"),
            Map.entry("VariableScheme", "50907716-b67a-4dcd-8f9f-8a283cb5fee0"),
            Map.entry("ManagedRepresentationScheme", "16d4d829-41e1-4677-aa17-81190b6a0e66"),
            Map.entry("ManagedMissingValuesRepresentation", "c29c3125-2a53-4179-8fa6-aa3beb2bb5ed"),
            Map.entry("StudyUnit", "30ea0200-7121-4f01-8d21-a931a182b86d"),
            Map.entry("Group", "4bd6eef6-99df-40e6-9b11-5b8f64e5cb23"),
            Map.entry("VariableGroup", "91da6c62-c2c2-4173-8958-22c518d1d40d"));

    private ColecticaTestItemTypes() {}

    /** La configuration réelle, dont {@code overrides} remplace ou complète des clés. */
    static Map<String, String> configuredWith(Map<String, String> overrides) {
        Map<String, String> itemTypes = new HashMap<>(CONFIGURED);
        itemTypes.putAll(overrides);
        return itemTypes;
    }
}
