package za.co.ratpack.finance.reactive.functions.rest.integration.openapi;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for introspecting Java classes and generating OpenAPI Schema objects
 * with full field-level documentation from @Schema annotations.
 * 
 * @author Generated
 * @created at 12:30 on 12/02/2026
 */
public class SchemaIntrospector {
  
  private static final Logger LOG = LoggerFactory.getLogger(SchemaIntrospector.class);
  
  /**
   * Introspects a Java class and generates an OpenAPI Schema with field descriptions,
   * examples, required fields, and other metadata from @Schema annotations.
   * 
   * @param clazz The class to introspect
   * @param schemaName Name to give the schema in the OpenAPI spec
   * @return Fully documented OpenAPI Schema object
   */
  public static io.swagger.v3.oas.models.media.Schema<?> introspectClass(Class<?> clazz, String schemaName) {
    LOG.debug("Introspecting class: {} for schema: {}", clazz.getName(), schemaName);
    
    ObjectSchema schema = new ObjectSchema();
    schema.setName(schemaName);
    
    // Read class-level @Schema annotation
    Schema classSchema = clazz.getAnnotation(Schema.class);
    if (classSchema != null && !classSchema.description().isEmpty()) {
      schema.setDescription(classSchema.description());
    }
    
    List<String> requiredFields = new ArrayList<>();
    
    // Introspect all fields
    for (Field field : clazz.getDeclaredFields()) {
      // Skip static and transient fields
      if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) || 
          java.lang.reflect.Modifier.isTransient(field.getModifiers())) {
        continue;
      }
      
      String fieldName = field.getName();
      io.swagger.v3.oas.models.media.Schema<?> fieldSchema = createFieldSchema(field);
      
      if (fieldSchema != null) {
        schema.addProperty(fieldName, fieldSchema);
        
        // Check if field is required
        if (isFieldRequired(field)) {
          requiredFields.add(fieldName);
        }
      }
    }
    
    if (!requiredFields.isEmpty()) {
      schema.setRequired(requiredFields);
    }
    
    return schema;
  }
  
  /**
   * Creates a Schema for an individual field based on its type and annotations.
   */
  private static io.swagger.v3.oas.models.media.Schema<?> createFieldSchema(Field field) {
    Class<?> fieldType = field.getType();
    Schema schemaAnnotation = field.getAnnotation(Schema.class);
    
    io.swagger.v3.oas.models.media.Schema<?> fieldSchema;
    
    // Handle List/Collection types
    if (List.class.isAssignableFrom(fieldType)) {
      fieldSchema = createArraySchema(field);
    }
    // Handle basic types
    else if (fieldType == String.class) {
      StringSchema stringSchema = new StringSchema();
      fieldSchema = stringSchema;
    }
    else if (fieldType == Integer.class || fieldType == int.class) {
      IntegerSchema intSchema = new IntegerSchema();
      
      // Apply format from @Schema
      if (schemaAnnotation != null && !schemaAnnotation.format().isEmpty()) {
        intSchema.setFormat(schemaAnnotation.format());
      } else {
        intSchema.setFormat("int32");
      }
      
      fieldSchema = intSchema;
    }
    else if (fieldType == Long.class || fieldType == long.class) {
      IntegerSchema longSchema = new IntegerSchema();
      longSchema.setFormat("int64");
      fieldSchema = longSchema;
    }
    else {
      // For complex types, create a reference or basic object
      ObjectSchema objSchema = new ObjectSchema();
      fieldSchema = objSchema;
    }
    
    // Apply @Schema annotation properties
    if (schemaAnnotation != null) {
      if (!schemaAnnotation.description().isEmpty()) {
        fieldSchema.setDescription(schemaAnnotation.description());
      }
      if (!schemaAnnotation.example().isEmpty()) {
        fieldSchema.setExample(schemaAnnotation.example());
      }
      if (!schemaAnnotation.format().isEmpty() && fieldSchema.getFormat() == null) {
        fieldSchema.setFormat(schemaAnnotation.format());
      }
    }
    
    return fieldSchema;
  }
  
  /**
   * Creates an ArraySchema for List/Collection fields.
   */
  private static ArraySchema createArraySchema(Field field) {
    ArraySchema arraySchema = new ArraySchema();
    
    Schema schemaAnnotation = field.getAnnotation(Schema.class);
    if (schemaAnnotation != null) {
      if (!schemaAnnotation.description().isEmpty()) {
        arraySchema.setDescription(schemaAnnotation.description());
      }
    }
    
    // Try to get the generic type of the List
    Type genericType = field.getGenericType();
    if (genericType instanceof ParameterizedType) {
      ParameterizedType paramType = (ParameterizedType) genericType;
      Type[] typeArgs = paramType.getActualTypeArguments();
      if (typeArgs.length > 0 && typeArgs[0] instanceof Class) {
        Class<?> itemClass = (Class<?>) typeArgs[0];
        
        // For complex types like CurrencyRequest, create a reference
        if (!isSimpleType(itemClass)) {
          // Create a reference to the item schema
          io.swagger.v3.oas.models.media.Schema<?> itemSchema = new io.swagger.v3.oas.models.media.Schema<>();
          itemSchema.set$ref("#/components/schemas/" + itemClass.getSimpleName());
          arraySchema.setItems(itemSchema);
        } else {
          // For simple types, create inline schema
          io.swagger.v3.oas.models.media.Schema<?> itemSchema = createSimpleTypeSchema(itemClass);
          arraySchema.setItems(itemSchema);
        }
      }
    }
    
    return arraySchema;
  }
  
  /**
   * Checks if a field is required based on @NotNull annotation.
   * Note: @Schema(required=true) is deprecated, so we only check @NotNull.
   */
  private static boolean isFieldRequired(Field field) {
    // Check @NotNull annotation
    if (field.getAnnotation(NotNull.class) != null) {
      return true;
    }
    
    return false;
  }
  
  /**
   * Checks if a type is a simple/primitive type.
   */
  private static boolean isSimpleType(Class<?> clazz) {
    return clazz == String.class || 
           clazz == Integer.class || clazz == int.class ||
           clazz == Long.class || clazz == long.class ||
           clazz == Double.class || clazz == double.class ||
           clazz == Float.class || clazz == float.class ||
           clazz == Boolean.class || clazz == boolean.class;
  }
  
  /**
   * Creates a schema for simple/primitive types.
   */
  private static io.swagger.v3.oas.models.media.Schema<?> createSimpleTypeSchema(Class<?> clazz) {
    if (clazz == String.class) {
      return new StringSchema();
    } else if (clazz == Integer.class || clazz == int.class) {
      IntegerSchema schema = new IntegerSchema();
      schema.setFormat("int32");
      return schema;
    } else if (clazz == Long.class || clazz == long.class) {
      IntegerSchema schema = new IntegerSchema();
      schema.setFormat("int64");
      return schema;
    }
    return new io.swagger.v3.oas.models.media.Schema<>();
  }
}
