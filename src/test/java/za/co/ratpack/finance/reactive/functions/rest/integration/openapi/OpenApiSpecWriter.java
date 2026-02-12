package za.co.ratpack.finance.reactive.functions.rest.integration.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Writes OpenAPI specifications by introspecting request/response bodies
 * and matching them to known Java classes using the ClasspathSchemaRegistry.
 * Falls back to JSON structure inference when no class match is found.
 *
 * @author markmngoma
 */
public class OpenApiSpecWriter {
    private static final Logger logger = LoggerFactory.getLogger(OpenApiSpecWriter.class);
    
    private final ClasspathSchemaRegistry registry;
    private final SchemaIntrospector introspector;
    private final ObjectMapper objectMapper;
    
    /**
     * Creates a writer with default package scanning configuration.
     */
    public OpenApiSpecWriter() {
        this(new ClasspathSchemaRegistry());
    }
    
    /**
     * Creates a writer with a custom registry.
     * 
     * @param registry the classpath schema registry to use
     */
    public OpenApiSpecWriter(ClasspathSchemaRegistry registry) {
        this.registry = registry;
        this.introspector = new SchemaIntrospector();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Attempts to introspect a known class from a JSON body.
     * Uses the ClasspathSchemaRegistry to match JSON field names to registered classes.
     * 
     * @param jsonBody the JSON body as a string
     * @return schema map, or empty optional if cannot introspect
     */
    public Optional<Map<String, Object>> tryIntrospectKnownClass(String jsonBody) {
        if (jsonBody == null || jsonBody.trim().isEmpty()) {
            return Optional.empty();
        }
        
        try {
            JsonNode node = objectMapper.readTree(jsonBody);
            return tryIntrospectKnownClass(node);
        } catch (Exception e) {
            logger.debug("Failed to parse JSON body", e);
            return Optional.empty();
        }
    }
    
    /**
     * Attempts to introspect a known class from a JsonNode.
     * 
     * @param node the JSON node
     * @return schema map, or empty optional if cannot introspect
     */
    public Optional<Map<String, Object>> tryIntrospectKnownClass(JsonNode node) {
        if (node == null) {
            return Optional.empty();
        }
        
        try {
            // Handle array case
            if (node.isArray()) {
                return handleArrayNode(node);
            }
            
            // Handle object case
            if (node.isObject()) {
                return handleObjectNode(node);
            }
            
            return Optional.empty();
        } catch (Exception e) {
            logger.debug("Failed to introspect known class", e);
            return Optional.empty();
        }
    }
    
    /**
     * Handles array JSON nodes.
     */
    private Optional<Map<String, Object>> handleArrayNode(JsonNode node) {
        if (node.size() == 0) {
            // Empty array - return generic array schema
            return Optional.of(createGenericArraySchema());
        }
        
        // Introspect first element
        JsonNode firstElement = node.get(0);
        if (!firstElement.isObject()) {
            // Array of primitives
            return Optional.of(createPrimitiveArraySchema(firstElement));
        }
        
        // Try to match the first element to a known class
        Set<String> fieldNames = extractFieldNames(firstElement);
        Optional<Class<?>> matchedClass = registry.findMatchingClass(fieldNames);
        
        if (matchedClass.isPresent()) {
            Class<?> clazz = matchedClass.get();
            logger.debug("Matched array element to class: {}", clazz.getSimpleName());
            
            // Introspect the class to register its schema
            introspector.introspectClass(clazz);
            
            // Return array schema with reference
            Map<String, Object> schema = new LinkedHashMap<>();
            schema.put("type", "array");
            
            Map<String, Object> items = new LinkedHashMap<>();
            items.put("$ref", "#/components/schemas/" + clazz.getSimpleName());
            schema.put("items", items);
            
            return Optional.of(schema);
        }
        
        // Fallback to JSON inference for array element
        return Optional.empty();
    }
    
    /**
     * Handles object JSON nodes.
     */
    private Optional<Map<String, Object>> handleObjectNode(JsonNode node) {
        Set<String> fieldNames = extractFieldNames(node);
        
        if (fieldNames.isEmpty()) {
            return Optional.empty();
        }
        
        // Try to match to a known class
        Optional<Class<?>> matchedClass = registry.findMatchingClass(fieldNames);
        
        if (matchedClass.isPresent()) {
            Class<?> clazz = matchedClass.get();
            logger.debug("Matched object to class: {}", clazz.getSimpleName());
            
            // Introspect and return schema reference
            introspector.introspectClass(clazz);
            
            Map<String, Object> schema = new LinkedHashMap<>();
            schema.put("$ref", "#/components/schemas/" + clazz.getSimpleName());
            
            return Optional.of(schema);
        }
        
        return Optional.empty();
    }
    
    /**
     * Extracts field names from a JSON object node.
     */
    private Set<String> extractFieldNames(JsonNode node) {
        Set<String> fieldNames = new HashSet<>();
        if (node.isObject()) {
            node.fieldNames().forEachRemaining(fieldNames::add);
        }
        return fieldNames;
    }
    
    /**
     * Creates a generic array schema.
     */
    private Map<String, Object> createGenericArraySchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "array");
        
        Map<String, Object> items = new LinkedHashMap<>();
        items.put("type", "object");
        schema.put("items", items);
        
        return schema;
    }
    
    /**
     * Creates an array schema for primitive values.
     */
    private Map<String, Object> createPrimitiveArraySchema(JsonNode firstElement) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "array");
        
        Map<String, Object> items = new LinkedHashMap<>();
        items.put("type", inferPrimitiveType(firstElement));
        schema.put("items", items);
        
        return schema;
    }
    
    /**
     * Infers primitive type from a JSON node.
     */
    private String inferPrimitiveType(JsonNode node) {
        if (node.isTextual()) {
            return "string";
        } else if (node.isInt()) {
            return "integer";
        } else if (node.isNumber()) {
            return "number";
        } else if (node.isBoolean()) {
            return "boolean";
        } else {
            return "string";
        }
    }
    
    /**
     * Infers a schema from JSON structure when no class match is found.
     * This is the fallback method for unknown structures.
     * 
     * @param jsonBody the JSON body as a string
     * @return inferred schema map
     */
    public Map<String, Object> inferSchemaFromJson(String jsonBody) {
        try {
            JsonNode node = objectMapper.readTree(jsonBody);
            return inferSchemaFromNode(node);
        } catch (Exception e) {
            logger.warn("Failed to infer schema from JSON", e);
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("type", "object");
            return fallback;
        }
    }
    
    /**
     * Infers schema from a JsonNode recursively.
     */
    private Map<String, Object> inferSchemaFromNode(JsonNode node) {
        Map<String, Object> schema = new LinkedHashMap<>();
        
        if (node.isArray()) {
            schema.put("type", "array");
            if (node.size() > 0) {
                schema.put("items", inferSchemaFromNode(node.get(0)));
            } else {
                Map<String, Object> items = new LinkedHashMap<>();
                items.put("type", "object");
                schema.put("items", items);
            }
        } else if (node.isObject()) {
            schema.put("type", "object");
            Map<String, Object> properties = new LinkedHashMap<>();
            
            node.fields().forEachRemaining(entry -> {
                properties.put(entry.getKey(), inferSchemaFromNode(entry.getValue()));
            });
            
            schema.put("properties", properties);
        } else if (node.isTextual()) {
            schema.put("type", "string");
        } else if (node.isInt()) {
            schema.put("type", "integer");
        } else if (node.isNumber()) {
            schema.put("type", "number");
        } else if (node.isBoolean()) {
            schema.put("type", "boolean");
        } else {
            schema.put("type", "string");
        }
        
        return schema;
    }
    
    /**
     * Gets the schema introspector to access generated schemas.
     */
    public SchemaIntrospector getIntrospector() {
        return introspector;
    }
    
    /**
     * Gets all generated component schemas.
     */
    public Map<String, Map<String, Object>> getComponentSchemas() {
        return introspector.getGeneratedSchemas();
    }
    
    /**
     * Gets the classpath registry.
     */
    public ClasspathSchemaRegistry getRegistry() {
        return registry;
    }
}
