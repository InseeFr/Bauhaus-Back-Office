package fr.insee.rmes.graphdb;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.annotations.Entity;
import fr.insee.rmes.graphdb.annotations.Graph;
import fr.insee.rmes.graphdb.annotations.Predicate;
import fr.insee.rmes.graphdb.annotations.Statement;
import fr.insee.rmes.graphdb.annotations.Type;
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
    private boolean distinct = false;

    public SparqlQueryBuilder(Class<T> entityClass) {
        this.entityClass = entityClass;
        this.entityName = extractEntityName();
        this.graphName = extractGraphName();
        this.entityType = extractEntityType();
        buildFieldMappings();
    }
    
    public SparqlQueryBuilder(Class<T> entityClass, Environment environment) {
        this.entityClass = entityClass;
        this.environment = environment;
        this.entityName = extractEntityName();
        this.graphName = extractGraphName();
        this.entityType = extractEntityType();
        buildFieldMappings();
    }

    public static <T> SparqlQueryBuilder<T> forEntity(Class<T> entityClass) {
        return new SparqlQueryBuilder<>(entityClass);
    }

   public SparqlQueryBuilder<T> select(String... fields) {
        Collections.addAll(this.selectFields, fields);
        return this;
    }

    public SparqlQueryBuilder<T> selectAll() {
        selectFields.addAll(fieldToPredicateMap.keySet());
        return this;
    }

     public SparqlQueryBuilder<T> where(String field, Object value) {
        if (!fieldToPredicateMap.containsKey(field)) {
            throw new IllegalArgumentException("Field '" + field + "' not found in entity " + entityClass.getSimpleName());
        }

        filters.put(field, value);
        String predicate = fieldToPredicateMap.get(field);
        String variable = "?" + field;

        if ("uri".equals(field)) {
            whereConditions.add("BIND(<" + value + "> AS " + variable + ")");
        } else {
            whereConditions.add("?" + entityName.toLowerCase() + " " + predicate + " " + variable + " .");
            if (value instanceof String) {
                whereConditions.add("FILTER(" + variable + " = \"" + value + "\")");
            }
        }
        return this;
    }


    public SparqlQueryBuilder<T> orderBy(String field, String direction) {
        if (!fieldToPredicateMap.containsKey(field)) {
            throw new IllegalArgumentException("Field '" + field + "' not found in entity " + entityClass.getSimpleName());
        }
        orderByFields.add(direction + "(?" + field + ")");
        return this;
    }

    public SparqlQueryBuilder<T> limit(int limit) {
        this.limitValue = String.valueOf(limit);
        return this;
    }

    public SparqlQueryBuilder<T> offset(int offset) {
        this.offsetValue = String.valueOf(offset);
        return this;
    }

    public SparqlQueryBuilder<T> distinct() {
        this.distinct = true;
        return this;
    }

    public String build() throws RmesException {
        StringBuilder query = new StringBuilder();
        
        // Ajouter les préfixes
        for (Map.Entry<String, String> prefix : namespacePrefixes.entrySet()) {
            query.append("PREFIX ").append(prefix.getKey()).append(":<").append(prefix.getValue()).append(">\n");
        }
        if (!namespacePrefixes.isEmpty()) {
            query.append("\n");
        }

        query.append("SELECT ");
        if (distinct) {
            query.append("DISTINCT ");
        }

        if (selectFields.isEmpty()) {
            query.append("*");
        } else {
            query.append(selectFields.stream()
                    .map(field -> "?" + field)
                    .collect(Collectors.joining(" ")));
        }
        query.append("\n");

        String resolvedGraphName = resolveGraphName();
        if (resolvedGraphName != null) {
            query.append("FROM <").append(resolvedGraphName).append(">\n");
        }

        query.append("WHERE {\n");
        
        if (entityType != null) {
            query.append("  ?" + entityName.toLowerCase() + " rdf:type " + entityType + " .\n");
        }

        Set<String> fieldsToRetrieve = selectFields.isEmpty() ? fieldToPredicateMap.keySet() : selectFields;
        
        boolean hasNonUriField = fieldsToRetrieve.stream().anyMatch(field -> !"uri".equals(field));
        if (!hasNonUriField && fieldsToRetrieve.contains("uri")) {
            String firstField = fieldToPredicateMap.keySet().stream()
                .filter(field -> !"uri".equals(field))
                .findFirst()
                .orElse(null);
            if (firstField != null) {
                String predicate = fieldToPredicateMap.get(firstField);
                query.append("  OPTIONAL { ?" + entityName.toLowerCase() + " " + predicate + " ?" + firstField + " }\n");
            }
        }
        
        for (String field : fieldsToRetrieve) {
            String predicate = fieldToPredicateMap.get(field);
            boolean isOptional = fieldToOptionalMap.getOrDefault(field, true);
            boolean isInverse = fieldToInverseMap.getOrDefault(field, false);
            
            if (predicate != null) {
                if ("URI".equals(predicate)) {
                    query.append("  BIND(?" + entityName.toLowerCase() + " AS ?" + field + ")\n");
                } else if (!"uri".equals(field)) {
                    String triple;
                    if (isInverse) {
                        triple = "?" + field + " " + predicate + " ?" + entityName.toLowerCase();
                    } else {
                        triple = "?" + entityName.toLowerCase() + " " + predicate + " ?" + field;
                    }
                    
                    if (fieldToLangMap.containsKey(field)) {
                        String lang = getLangForField(fieldToLangMap.get(field));
                        triple += " . FILTER(lang(?" + field + ") = \"" + lang + "\")";
                    }
                    
                    if (isOptional) {
                        query.append("  OPTIONAL { " + triple + " }\n");
                    } else {
                        query.append("  " + triple + " .\n");
                    }
                } else {
                    query.append("  BIND(?" + entityName.toLowerCase() + " AS ?uri)\n");
                }
            }
        }

        for (String condition : whereConditions) {
            query.append("  ").append(condition).append("\n");
        }

        query.append("}\n");

        if (!orderByFields.isEmpty()) {
            query.append("ORDER BY ").append(String.join(" ", orderByFields)).append("\n");
        }

        if (offsetValue != null) {
            query.append("OFFSET ").append(offsetValue).append("\n");
        }
        if (limitValue != null) {
            query.append("LIMIT ").append(limitValue).append("\n");
        }

        return query.toString();
    }

    private void buildFieldMappings() {
        if (entityClass.isRecord()) {
            for (RecordComponent component : entityClass.getRecordComponents()) {
                Predicate predicate = component.getAnnotation(Predicate.class);
                Statement statement = component.getAnnotation(Statement.class);
                
                if (predicate != null) {
                    String predicateUri = buildPredicateUri(predicate);
                    fieldToPredicateMap.put(component.getName(), predicateUri);
                    predicateToFieldMap.put(predicateUri, component.getName());
                    fieldToOptionalMap.put(component.getName(), predicate.optional());
                    fieldToInverseMap.put(component.getName(), predicate.inverse());
                    if(!predicate.lang().isEmpty()){
                        fieldToLangMap.put(component.getName(), predicate.lang());
                    }
                    collectNamespacePrefix(predicate);
                } else if (statement != null) {
                    // Pour les champs @Statement, on les traite comme des URIs de subject
                    fieldToPredicateMap.put(component.getName(), "URI");
                    predicateToFieldMap.put("URI", component.getName());
                    fieldToOptionalMap.put(component.getName(), false);
                    fieldToInverseMap.put(component.getName(), false);
                }
            }
        } else {
            for (Field field : entityClass.getDeclaredFields()) {
                Predicate predicate = field.getAnnotation(Predicate.class);
                Statement statement = field.getAnnotation(Statement.class);
                
                if (predicate != null) {
                    String predicateUri = buildPredicateUri(predicate);
                    fieldToPredicateMap.put(field.getName(), predicateUri);
                    predicateToFieldMap.put(predicateUri, field.getName());
                    fieldToOptionalMap.put(field.getName(), predicate.optional());
                    fieldToInverseMap.put(field.getName(), predicate.inverse());
                    collectNamespacePrefix(predicate);
                } else if (statement != null) {
                    // Pour les champs @Statement, on les traite comme des URIs de subject
                    fieldToPredicateMap.put(field.getName(), "URI");
                    predicateToFieldMap.put("URI", field.getName());
                    fieldToOptionalMap.put(field.getName(), false);
                    fieldToInverseMap.put(field.getName(), false);
                }
            }
        }
    }

    private String buildPredicateUri(Predicate predicate) {
        if (predicate.value().startsWith("http")) {
            return "<" + predicate.value() + ">";
        }
        return predicate.value();
    }

    private String extractEntityName() {
        Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
        if (entityAnnotation != null && !entityAnnotation.value().isEmpty()) {
            return entityAnnotation.value();
        }
        return entityClass.getSimpleName();
    }

    private String extractGraphName() {
        Graph graphAnnotation = entityClass.getAnnotation(Graph.class);
        return graphAnnotation != null ? graphAnnotation.value() : null;
    }
    
    private String resolveGraphName() {
        if (graphName == null) {
            return null;
        }
        
        return PropertyResolver.resolve(graphName);
    }
    
    private String extractEntityType() {
        Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
        if (entityAnnotation != null && !entityAnnotation.type().isEmpty()) {
            return entityAnnotation.type();
        }
        return null;
    }
    
    private String getLangForField(String field) {
        if (field.equalsIgnoreCase("lg1")) {
            return PropertyResolver.resolve("${fr.insee.rmes.bauhaus.lg1}");
        } else if (field.equalsIgnoreCase("lg2")) {
            return PropertyResolver.resolve("${fr.insee.rmes.bauhaus.lg2}");
        }
        return "";
    }
    
    private void collectNamespacePrefix(Predicate predicate) {
        if (!predicate.namespace().isEmpty()) {
            String prefix = extractPrefixFromValue(predicate.value());
            if (prefix != null) {
                namespacePrefixes.put(prefix, predicate.namespace());
            }
        }
    }
    
    private String extractPrefixFromValue(String value) {
        if (value.contains(":")) {
            return value.substring(0, value.indexOf(":"));
        }
        return null;
    }


    public Set<String> getAvailableFields() {
        return fieldToPredicateMap.keySet();
    }

    public Map<String, String> getFieldToPredicateMapping() {
        return new HashMap<>(fieldToPredicateMap);
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        if (this.environment == null) {
            this.environment = applicationContext.getEnvironment();
        }
    }
}