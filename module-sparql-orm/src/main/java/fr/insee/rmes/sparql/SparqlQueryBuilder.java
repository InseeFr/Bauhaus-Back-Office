package fr.insee.rmes.sparql;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.sparql.annotations.Entity;
import fr.insee.rmes.sparql.annotations.Graph;
import fr.insee.rmes.sparql.annotations.Predicate;
import fr.insee.rmes.sparql.annotations.Statement;
import fr.insee.rmes.sparql.annotations.Type;
import fr.insee.rmes.sparql.utils.PropertyResolver;
import fr.opensagres.xdocreport.template.freemarker.internal.XDocFreemarkerContext;
import org.springframework.core.env.Environment;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.*;
import java.util.stream.Collectors;


public class SparqlQueryBuilder<T> implements ApplicationContextAware {

    private final Class<T> entityClass;
    private final Map<String, String> fieldToPredicateMap = new HashMap<>();
    private final Map<String, String> predicateToFieldMap = new HashMap<>();
    private final Map<String, Boolean> fieldToOptionalMap = new HashMap<>();
    private final Map<String, Boolean> fieldToInverseMap = new HashMap<>();
    private final Map<String, String> namespacePrefixes = new HashMap<>();
    private final Map<String, String> fieldToLangMap = new HashMap<>();

    private final String entityName;
    private final String graphName;
    private final String entityType;
    private Environment environment;

    private final Set<String> selectFields = new LinkedHashSet<>();
    private final List<String> whereConditions = new ArrayList<>();
    private final Map<String, Object> filters = new HashMap<>();
    private final List<String> orderByFields = new ArrayList<>();
    private String limitValue;
    private String offsetValue;
    private boolean distinct;

    public SparqlQueryBuilder(final Class<T> entityClass) {
        this.entityClass = entityClass;
        entityName = this.extractEntityName();
        graphName = this.extractGraphName();
        entityType = this.extractEntityType();
        this.buildFieldMappings();
    }
    
    public SparqlQueryBuilder(final Class<T> entityClass, final Environment environment) {
        this.entityClass = entityClass;
        this.environment = environment;
        entityName = this.extractEntityName();
        graphName = this.extractGraphName();
        entityType = this.extractEntityType();
        this.buildFieldMappings();
    }

    public static <T> SparqlQueryBuilder<T> forEntity(final Class<T> entityClass) {
        return new SparqlQueryBuilder<>(entityClass);
    }

   public SparqlQueryBuilder<T> select(final String... fields) {
        Collections.addAll(selectFields, fields);
        return this;
    }

    public SparqlQueryBuilder<T> selectAll() {
        this.selectFields.addAll(this.fieldToPredicateMap.keySet());
        return this;
    }
    
    public SparqlQueryBuilder<T> selectAllExcept(final String... excludedFields) {
        final Set<String> excluded = Set.of(excludedFields);
        this.fieldToPredicateMap.keySet().stream()
                .filter(field -> !excluded.contains(field))
                .forEach(this.selectFields::add);
        return this;
    }

     public SparqlQueryBuilder<T> where(final String field, final Object value) {
        if (!this.fieldToPredicateMap.containsKey(field)) {
            throw new IllegalArgumentException("Field '" + field + "' not found in entity " + this.entityClass.getSimpleName());
        }

         this.filters.put(field, value);
        final String predicate = this.fieldToPredicateMap.get(field);
        final String variable = "?" + field;

        if ("uri".equals(field)) {
            this.whereConditions.add("BIND(<" + value + "> AS " + variable + ")");
        } else {
            this.whereConditions.add("?" + this.entityName.toLowerCase() + " " + predicate + " " + variable + " .");
            if (value instanceof String) {
                this.whereConditions.add("FILTER(" + variable + " = \"" + value + "\")");
            }
        }
        return this;
    }


    public SparqlQueryBuilder<T> orderBy(final String field, final String direction) {
        if (!this.fieldToPredicateMap.containsKey(field)) {
            throw new IllegalArgumentException("Field '" + field + "' not found in entity " + this.entityClass.getSimpleName());
        }
        this.orderByFields.add(direction + "(?" + field + ")");
        return this;
    }

    public SparqlQueryBuilder<T> limit(final int limit) {
        limitValue = String.valueOf(limit);
        return this;
    }

    public SparqlQueryBuilder<T> offset(final int offset) {
        offsetValue = String.valueOf(offset);
        return this;
    }

    public SparqlQueryBuilder<T> distinct() {
        distinct = true;
        return this;
    }

    public String build() throws RmesException {
        final StringBuilder query = new StringBuilder();
        
        // Ajouter les préfixes
        for (final Map.Entry<String, String> prefix : this.namespacePrefixes.entrySet()) {
            query.append("PREFIX ").append(prefix.getKey()).append(":<").append(prefix.getValue()).append(">\n");
        }
        if (!this.namespacePrefixes.isEmpty()) {
            query.append("\n");
        }

        query.append("SELECT ");
        if (this.distinct) {
            query.append("DISTINCT ");
        }

        if (this.selectFields.isEmpty()) {
            query.append("*");
        } else {
            query.append(this.selectFields.stream()
                    .map(field -> "?" + field)
                    .collect(Collectors.joining(" ")));
        }
        query.append("\n");

        final String resolvedGraphName = this.resolveGraphName();
        if (null != resolvedGraphName) {
            query.append("FROM <").append(resolvedGraphName).append(">\n");
        }

        query.append("WHERE {\n");
        
        if (null != entityType) {
            query.append("  ?" + this.entityName.toLowerCase() + " rdf:type " + this.entityType + " .\n");
        }

        final Set<String> fieldsToRetrieve = this.selectFields.isEmpty() ? this.fieldToPredicateMap.keySet() : this.selectFields;
        
        final boolean hasNonUriField = fieldsToRetrieve.stream().anyMatch(field -> !"uri".equals(field));
        if (!hasNonUriField && fieldsToRetrieve.contains("uri")) {
            final String firstField = this.fieldToPredicateMap.keySet().stream()
                .filter(field -> !"uri".equals(field))
                .findFirst()
                .orElse(null);
            if (null != firstField) {
                final String predicate = this.fieldToPredicateMap.get(firstField);
                query.append("  OPTIONAL { ?" + this.entityName.toLowerCase() + " " + predicate + " ?" + firstField + " }\n");
            }
        }
        
        for (final String field : fieldsToRetrieve) {
            final String predicate = this.fieldToPredicateMap.get(field);
            final boolean isOptional = this.fieldToOptionalMap.getOrDefault(field, true);
            final boolean isInverse = this.fieldToInverseMap.getOrDefault(field, false);
            
            if (null != predicate) {
                if ("URI".equals(predicate)) {
                    query.append("  BIND(?" + this.entityName.toLowerCase() + " AS ?" + field + ")\n");
                } else if (!"uri".equals(field)) {
                    String triple;
                    if (isInverse) {
                        triple = "?" + field + " " + predicate + " ?" + this.entityName.toLowerCase();
                    } else {
                        triple = "?" + this.entityName.toLowerCase() + " " + predicate + " ?" + field;
                    }
                    
                    if (this.fieldToLangMap.containsKey(field)) {
                        final String lang = this.getLangForField(this.fieldToLangMap.get(field));
                        triple += " . FILTER(lang(?" + field + ") = \"" + lang + "\")";
                    }
                    
                    if (isOptional) {
                        query.append("  OPTIONAL { " + triple + " }\n");
                    } else {
                        query.append("  " + triple + " .\n");
                    }
                } else {
                    query.append("  BIND(?" + this.entityName.toLowerCase() + " AS ?uri)\n");
                }
            }
        }

        for (final String condition : this.whereConditions) {
            query.append("  ").append(condition).append("\n");
        }

        query.append("}\n");

        if (!this.orderByFields.isEmpty()) {
            query.append("ORDER BY ").append(String.join(" ", this.orderByFields)).append("\n");
        }

        if (null != offsetValue) {
            query.append("OFFSET ").append(this.offsetValue).append("\n");
        }
        if (null != limitValue) {
            query.append("LIMIT ").append(this.limitValue).append("\n");
        }

        return query.toString();
    }

    private void buildFieldMappings() {
        if (this.entityClass.isRecord()) {
            for (final RecordComponent component : this.entityClass.getRecordComponents()) {
                final Predicate predicate = component.getAnnotation(Predicate.class);
                final Statement statement = component.getAnnotation(Statement.class);
                
                if (null != predicate) {
                    final String predicateUri = this.buildPredicateUri(predicate);
                    this.fieldToPredicateMap.put(component.getName(), predicateUri);
                    this.predicateToFieldMap.put(predicateUri, component.getName());
                    this.fieldToOptionalMap.put(component.getName(), predicate.optional());
                    this.fieldToInverseMap.put(component.getName(), predicate.inverse());
                    if(!predicate.lang().isEmpty()){
                        this.fieldToLangMap.put(component.getName(), predicate.lang());
                    }
                    this.collectNamespacePrefix(predicate);
                } else if (null != statement) {
                    // Pour les champs @Statement, on les traite comme des URIs de subject
                    this.fieldToPredicateMap.put(component.getName(), "URI");
                    this.predicateToFieldMap.put("URI", component.getName());
                    this.fieldToOptionalMap.put(component.getName(), false);
                    this.fieldToInverseMap.put(component.getName(), false);
                }
            }
        } else {
            for (final Field field : this.entityClass.getDeclaredFields()) {
                final Predicate predicate = field.getAnnotation(Predicate.class);
                final Statement statement = field.getAnnotation(Statement.class);
                
                if (null != predicate) {
                    final String predicateUri = this.buildPredicateUri(predicate);
                    this.fieldToPredicateMap.put(field.getName(), predicateUri);
                    this.predicateToFieldMap.put(predicateUri, field.getName());
                    this.fieldToOptionalMap.put(field.getName(), predicate.optional());
                    this.fieldToInverseMap.put(field.getName(), predicate.inverse());
                    this.collectNamespacePrefix(predicate);
                } else if (null != statement) {
                    // Pour les champs @Statement, on les traite comme des URIs de subject
                    this.fieldToPredicateMap.put(field.getName(), "URI");
                    this.predicateToFieldMap.put("URI", field.getName());
                    this.fieldToOptionalMap.put(field.getName(), false);
                    this.fieldToInverseMap.put(field.getName(), false);
                }
            }
        }
    }

    private String buildPredicateUri(final Predicate predicate) {
        if (predicate.value().startsWith("http")) {
            return "<" + predicate.value() + ">";
        }
        return predicate.value();
    }

    private String extractEntityName() {
        final Entity entityAnnotation = this.entityClass.getAnnotation(Entity.class);
        if (null != entityAnnotation && !entityAnnotation.value().isEmpty()) {
            return entityAnnotation.value();
        }
        return this.entityClass.getSimpleName();
    }

    private String extractGraphName() {
        final Graph graphAnnotation = this.entityClass.getAnnotation(Graph.class);
        return null != graphAnnotation ? graphAnnotation.value() : null;
    }
    
    private String resolveGraphName() {
        if (null == graphName) {
            return null;
        }
        
        return PropertyResolver.resolve(this.graphName);
    }
    
    private String extractEntityType() {
        final Entity entityAnnotation = this.entityClass.getAnnotation(Entity.class);
        if (null != entityAnnotation && !entityAnnotation.type().isEmpty()) {
            return entityAnnotation.type();
        }
        return null;
    }
    
    private String getLangForField(final String field) {
        if ("lg1".equalsIgnoreCase(field)) {
            return PropertyResolver.resolve("${fr.insee.rmes.bauhaus.lg1}");
        } else if ("lg2".equalsIgnoreCase(field)) {
            return PropertyResolver.resolve("${fr.insee.rmes.bauhaus.lg2}");
        }
        return "";
    }
    
    private void collectNamespacePrefix(final Predicate predicate) {
        if (!predicate.namespace().isEmpty()) {
            final String prefix = this.extractPrefixFromValue(predicate.value());
            if (null != prefix) {
                this.namespacePrefixes.put(prefix, predicate.namespace());
            }
        }
    }
    
    private String extractPrefixFromValue(final String value) {
        if (value.contains(":")) {
            return value.substring(0, value.indexOf(':'));
        }
        return null;
    }


    public Set<String> getAvailableFields() {
        return this.fieldToPredicateMap.keySet();
    }

    public Map<String, String> getFieldToPredicateMapping() {
        return new HashMap<>(this.fieldToPredicateMap);
    }
    
    /**
     * Returns the set of fields that should be lazy-loaded based on annotations.
     * Currently identifies fields that are collections (List, Set) as lazy-loaded.
     * 
     * @return Set of field names that should be lazy-loaded
     */
    public Set<String> getLazyLoadedFields() {
        final Set<String> lazyFields = new HashSet<>();
        
        if (this.entityClass.isRecord()) {
            for (final RecordComponent component : this.entityClass.getRecordComponents()) {
                if (this.isLazyLoadedType(component.getType())) {
                    lazyFields.add(component.getName());
                }
            }
        } else {
            for (final Field field : this.entityClass.getDeclaredFields()) {
                if (this.isLazyLoadedType(field.getType())) {
                    lazyFields.add(field.getName());
                }
            }
        }
        
        return lazyFields;
    }
    
    /**
     * Checks if a type should be lazy-loaded.
     * Currently considers List and Set types as lazy-loaded.
     */
    private boolean isLazyLoadedType(final Class<?> type) {
        return List.class.isAssignableFrom(type) || Set.class.isAssignableFrom(type);
    }
    
    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        if (null == this.environment) {
            environment = applicationContext.getEnvironment();
        }
    }
}