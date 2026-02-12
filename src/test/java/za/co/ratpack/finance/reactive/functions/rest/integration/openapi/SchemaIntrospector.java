package za.co.ratpack.finance.reactive.functions.rest.integration.openapi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Introspects Java classes using reflection to generate OpenAPI schema definitions.
 * Handles primitives, collections, nested objects, and Jackson annotations.
 *
 * @author markmngoma
 */
public class SchemaIntrospector {
    private static final Logger logger = LoggerFactory.getLogger(SchemaIntrospector.class);
    
    private final Map<String, Map<String, Object>> generatedSchemas;
    
    public SchemaIntrospector() {
        this.generatedSchemas = new LinkedHashMap<>();
    }
    
    /**
     * Introspects a class and returns its schema definition.
     * Also registers the schema in the components/schemas section.
     * 
     * @param clazz the class to introspect
     * @return schema definition map
     */
    public Map<String, Object> introspectClass(Class<?> clazz) {
        String schemaName = clazz.getSimpleName();
        
        // Check if already generated
        if (generatedSchemas.containsKey(schemaName)) {
            return createSchemaRef(schemaName);
        }
        
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        
        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();
        
        // Process all fields including inherited ones
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (shouldIncludeField(field)) {
                    String fieldName = getFieldName(field);
                    Map<String, Object> fieldSchema = introspectField(field);
                    properties.put(fieldName, fieldSchema);
                    
                    // Check if required (simplified - assumes all non-null fields are required)
                    if (isRequiredField(field)) {
                        required.add(fieldName);
                    }
                }
            }
            current = current.getSuperclass();
        }
        
        schema.put("properties", properties);
        if (!required.isEmpty()) {
            schema.put("required", required);
        }
        
        // Register this schema
        generatedSchemas.put(schemaName, schema);
        
        logger.debug("Introspected class: {} with {} properties", schemaName, properties.size());
        
        return createSchemaRef(schemaName);
    }
    
    /**
     * Determines if a field should be included in the schema.
     */
    private boolean shouldIncludeField(Field field) {
        // Skip static, synthetic, and @JsonIgnore fields
        if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
            return false;
        }
        
        return !field.isAnnotationPresent(JsonIgnore.class);
    }
    
    /**
     * Gets the JSON field name, respecting @JsonProperty annotation.
     */
    private String getFieldName(Field field) {
        JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
        if (jsonProperty != null && !jsonProperty.value().isEmpty()) {
            return jsonProperty.value();
        }
        return field.getName();
    }
    
    /**
     * Simplified required check - currently only marks primitive types as required.
     * 
     * LIMITATION: This is a basic implementation. In production, this should:
     * - Check for @NotNull, @NotEmpty, @NotBlank Jakarta validation annotations
     * - Consider wrapper types (Integer, Boolean) which can be null
     * - Handle @JsonProperty(required=true) Jackson annotations
     * 
     * For this test infrastructure, we keep it simple to avoid false positives.
     */
    private boolean isRequiredField(Field field) {
        // Only mark primitive types as required (they can't be null in Java)
        // Wrapper types and objects are considered optional unless annotated
        return field.getType().isPrimitive();
    }
    
    /**
     * Introspects a field and returns its schema definition.
     */
    private Map<String, Object> introspectField(Field field) {
        Type fieldType = field.getGenericType();
        Class<?> fieldClass = field.getType();
        
        // Handle collections
        if (Collection.class.isAssignableFrom(fieldClass)) {
            return introspectCollection(fieldType);
        }
        
        // Handle maps
        if (Map.class.isAssignableFrom(fieldClass)) {
            return introspectMap(fieldType);
        }
        
        // Handle regular types
        return introspectType(fieldClass);
    }
    
    /**
     * Introspects a collection type.
     */
    private Map<String, Object> introspectCollection(Type type) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "array");
        
        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            Type[] typeArgs = pType.getActualTypeArguments();
            if (typeArgs.length > 0) {
                Type itemType = typeArgs[0];
                
                // Handle nested generics (e.g., List<List<String>>)
                if (itemType instanceof ParameterizedType) {
                    schema.put("items", introspectCollection(itemType));
                } else if (itemType instanceof Class) {
                    Class<?> itemClass = (Class<?>) itemType;
                    schema.put("items", introspectType(itemClass));
                } else {
                    // Fallback for TypeVariable or other complex types
                    Map<String, Object> items = new LinkedHashMap<>();
                    items.put("type", "object");
                    schema.put("items", items);
                }
            } else {
                // Fallback for raw collection
                Map<String, Object> items = new LinkedHashMap<>();
                items.put("type", "object");
                schema.put("items", items);
            }
        } else {
            // Fallback for raw collection
            Map<String, Object> items = new LinkedHashMap<>();
            items.put("type", "object");
            schema.put("items", items);
        }
        
        return schema;
    }
    
    /**
     * Introspects a map type.
     */
    private Map<String, Object> introspectMap(Type type) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("additionalProperties", true);
        return schema;
    }
    
    /**
     * Introspects a type and returns its schema definition.
     */
    private Map<String, Object> introspectType(Class<?> clazz) {
        Map<String, Object> schema = new LinkedHashMap<>();
        
        // Primitives and common types
        if (clazz == String.class) {
            schema.put("type", "string");
        } else if (clazz == Integer.class || clazz == int.class) {
            schema.put("type", "integer");
            schema.put("format", "int32");
        } else if (clazz == Long.class || clazz == long.class) {
            schema.put("type", "integer");
            schema.put("format", "int64");
        } else if (clazz == Double.class || clazz == double.class) {
            schema.put("type", "number");
            schema.put("format", "double");
        } else if (clazz == Float.class || clazz == float.class) {
            schema.put("type", "number");
            schema.put("format", "float");
        } else if (clazz == Boolean.class || clazz == boolean.class) {
            schema.put("type", "boolean");
        } else if (clazz == LocalDate.class) {
            schema.put("type", "string");
            schema.put("format", "date");
        } else if (clazz == LocalDateTime.class) {
            schema.put("type", "string");
            schema.put("format", "date-time");
        } else if (clazz.isArray()) {
            schema.put("type", "array");
            Map<String, Object> items = introspectType(clazz.getComponentType());
            schema.put("items", items);
        } else if (clazz.isEnum()) {
            schema.put("type", "string");
            Object[] constants = clazz.getEnumConstants();
            List<String> enumValues = new ArrayList<>();
            for (Object constant : constants) {
                enumValues.add(constant.toString());
            }
            schema.put("enum", enumValues);
        } else {
            // Complex object - introspect recursively
            return introspectClass(clazz);
        }
        
        return schema;
    }
    
    /**
     * Creates a schema reference.
     */
    private Map<String, Object> createSchemaRef(String schemaName) {
        Map<String, Object> ref = new LinkedHashMap<>();
        ref.put("$ref", "#/components/schemas/" + schemaName);
        return ref;
    }
    
    /**
     * Gets all generated schemas for the components/schemas section.
     */
    public Map<String, Map<String, Object>> getGeneratedSchemas() {
        return new LinkedHashMap<>(generatedSchemas);
    }
    
    /**
     * Clears all generated schemas.
     */
    public void clear() {
        generatedSchemas.clear();
    }
}
