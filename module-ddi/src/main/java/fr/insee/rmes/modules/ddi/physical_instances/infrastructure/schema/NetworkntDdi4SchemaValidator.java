package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import fr.insee.rmes.modules.commons.hexagonal.ServerSideAdaptor;
import fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions.InvalidDdi4JsonException;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.Ddi4SchemaRepository;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.Ddi4SchemaValidator;

import java.util.List;

/**
 * Valide un document DDI 4 contre le JSON Schema, via la bibliothèque networknt.
 * <p>
 * Le schéma pèse 1,1 Mo et déclare 161 types : le lire, le parser et le compiler coûte cher, il
 * n'est donc payé qu'une fois puis réutilisé. Deux réglages font l'essentiel du gain :
 * <ul>
 *   <li>la compilation est mémorisée (avant, chaque requête repartait du fichier) ;</li>
 *   <li>{@code preloadJsonSchema(false)} : la résolution <em>eager</em> de tous les {@code $ref}
 *       du schéma prenait à elle seule une vingtaine de secondes. En résolution paresseuse, seules
 *       les branches réellement traversées par le document sont montées — même verdict, pour
 *       quelques millisecondes.</li>
 * </ul>
 * Contrepartie de la résolution paresseuse : un {@code $ref} cassé n'est plus détecté à la
 * compilation mais à la première validation qui l'emprunte. Le schéma est une ressource interne
 * sans référence distante, le risque est théorique.
 * <p>
 * L'instance compilée est partagée entre requêtes : {@code JsonSchema} est documenté comme
 * thread-safe tant que sa configuration n'est pas modifiée, et la résolution paresseuse des
 * {@code $ref} se synchronise sur la fabrique.
 */
@ServerSideAdaptor
public class NetworkntDdi4SchemaValidator implements Ddi4SchemaValidator {

    private final Ddi4SchemaRepository schemaRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    /** Compilé à la première validation, puis réutilisé. */
    private volatile JsonSchema schema;

    public NetworkntDdi4SchemaValidator(Ddi4SchemaRepository schemaRepository) {
        this.schemaRepository = schemaRepository;
    }

    @Override
    public List<String> validate(String json) {
        JsonNode document = readDocument(json);
        return compiledSchema()
                .validate(document)
                .stream()
                .map(ValidationMessage::getMessage)
                .toList();
    }

    private JsonNode readDocument(String json) {
        try {
            return mapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new InvalidDdi4JsonException(e.getOriginalMessage(), e);
        }
    }

    private JsonSchema compiledSchema() {
        JsonSchema compiled = this.schema;
        if (compiled == null) {
            synchronized (this) {
                compiled = this.schema;
                if (compiled == null) {
                    compiled = compile();
                    this.schema = compiled;
                }
            }
        }
        return compiled;
    }

    private JsonSchema compile() {
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder()
                .preloadJsonSchema(false)
                .build();
        try {
            JsonNode schemaNode = mapper.readTree(schemaRepository.schemaDocument());
            return JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012)
                    .getSchema(schemaNode, config);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Schéma DDI 4 illisible", e);
        }
    }
}
