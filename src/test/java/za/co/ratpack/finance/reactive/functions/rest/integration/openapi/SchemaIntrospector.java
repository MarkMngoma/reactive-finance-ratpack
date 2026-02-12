package za.co.ratpack.finance.reactive.functions.rest.integration.openapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.models.media.*;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Utility class for introspecting Java classes and generating OpenAPI Schema objects
 * with full field-level documentation from @Schema annotations.
 * Supports all common datatypes including BigDecimal, LocalDateTime, LocalDate, enums, and more.
 * 
 * @author Generated
 * @created at 12:30 on 12/02/2026
 */
public class SchemaIntrospector {
  
  private static final Logger LOG = LoggerFactory.getLogger(SchemaIntrospector.class);
  
  /**
   * Introspects a Java class and generates an OpenAPI Schema with field descriptions,
   * examples, required fields, and other metadata from @Schema annotations.
   * Includes fields from parent classes (inheritance support).
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
    
    // Get ignored properties from @JsonIgnoreProperties
    Set<String> ignoredProperties = getIgnoredProperties(clazz);
    
    List<String> requiredFields = new ArrayList<>();
    
    // Introspect all fields including inherited fields
    List<Field> allFields = getAllFields(clazz);
    for (Field field : allFields) {
      // Skip static, transient, and ignored fields
      if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) || 
          java.lang.reflect.Modifier.isTransient(field.getModifiers()) ||
          ignoredProperties.contains(field.getName())) {
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
   * Gets all fields from a class including fields from parent classes.
   */
  private static List<Field> getAllFields(Class<?> clazz) {
    List<Field> fields = new ArrayList<>();
    Class<?> currentClass = clazz;
    
    while (currentClass != null && currentClass != Object.class) {
      fields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
      currentClass = currentClass.getSuperclass();
    }
    
    return fields;
  }
  
  /**
   * Gets the set of ignored property names from @JsonIgnoreProperties annotation.
   */
  private static Set<String> getIgnoredProperties(Class<?> clazz) {
    Set<String> ignored = new HashSet<>();
    
    // Check current class and all parent classes
    Class<?> currentClass = clazz;
    while (currentClass != null && currentClass != Object.class) {
      JsonIgnoreProperties ignoreAnnotation = currentClass.getAnnotation(JsonIgnoreProperties.class);
      if (ignoreAnnotation != null) {
        ignored.addAll(Arrays.asList(ignoreAnnotation.value()));
      }
      currentClass = currentClass.getSuperclass();
    }
    
    return ignored;
  }
  
  /**
   * Creates a Schema for an individual field based on its type and annotations.
   * Supports: String, Integer, Long, Boolean, Double, Float, BigDecimal,
   * LocalDateTime, LocalDate, Enums, Lists, and complex objects.
   */
  private static io.swagger.v3.oas.models.media.Schema<?> createFieldSchema(Field field) {
    Class<?> fieldType = field.getType();
    Schema schemaAnnotation = field.getAnnotation(Schema.class);
    
    io.swagger.v3.oas.models.media.Schema<?> fieldSchema;
    
    // Handle List/Collection types
    if (List.class.isAssignableFrom(fieldType) || Collection.class.isAssignableFrom(fieldType)) {
      fieldSchema = createArraySchema(field);
    }
    // Handle Enum types
    else if (fieldType.isEnum()) {
      StringSchema enumSchema = new StringSchema();
      // Add enum values
      Object[] enumConstants = fieldType.getEnumConstants();
      List<String> enumValues = new ArrayList<>();
      for (Object enumConstant : enumConstants) {
        enumValues.add(enumConstant.toString());
      }
      enumSchema.setEnum(enumValues);
      fieldSchema = enumSchema;
    }
    // Handle String
    else if (fieldType == String.class) {
      fieldSchema = new StringSchema();
    }
    // Handle Integer/int
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
    // Handle Long/long
    else if (fieldType == Long.class || fieldType == long.class) {
      IntegerSchema longSchema = new IntegerSchema();
      longSchema.setFormat("int64");
      fieldSchema = longSchema;
    }
    // Handle Boolean/boolean
    else if (fieldType == Boolean.class || fieldType == boolean.class) {
      fieldSchema = new BooleanSchema();
    }
    // Handle Double/double
    else if (fieldType == Double.class || fieldType == double.class) {
      NumberSchema doubleSchema = new NumberSchema();
      doubleSchema.setFormat("double");
      fieldSchema = doubleSchema;
    }
    // Handle Float/float
    else if (fieldType == Float.class || fieldType == float.class) {
      NumberSchema floatSchema = new NumberSchema();
      floatSchema.setFormat("float");
      fieldSchema = floatSchema;
    }
    // Handle BigDecimal
    else if (fieldType == BigDecimal.class) {
      NumberSchema decimalSchema = new NumberSchema();
      // Use format from annotation, default to decimal
      if (schemaAnnotation != null && !schemaAnnotation.format().isEmpty()) {
        decimalSchema.setFormat(schemaAnnotation.format());
      } else {
        decimalSchema.setFormat("decimal");
      }
      fieldSchema = decimalSchema;
    }
    // Handle LocalDateTime
    else if (fieldType == LocalDateTime.class) {
      DateTimeSchema dateTimeSchema = new DateTimeSchema();
      dateTimeSchema.setFormat("date-time");
      fieldSchema = dateTimeSchema;
    }
    // Handle LocalDate
    else if (fieldType == LocalDate.class) {
      DateSchema dateSchema = new DateSchema();
      dateSchema.setFormat("date");
      fieldSchema = dateSchema;
    }
    // Handle Joda Money types (if available)
    else if (fieldType.getName().equals("org.joda.money.Money")) {
      // Money is typically represented as a string in APIs
      StringSchema moneySchema = new StringSchema();
      moneySchema.setFormat("money");
      if (schemaAnnotation == null || schemaAnnotation.description().isEmpty()) {
        moneySchema.setDescription("Monetary amount with currency (e.g., 'USD 100.00')");
      }
      fieldSchema = moneySchema;
    }
    else if (fieldType.getName().equals("org.joda.money.CurrencyUnit")) {
      // CurrencyUnit is a string currency code
      StringSchema currencySchema = new StringSchema();
      currencySchema.setFormat("currency");
      if (schemaAnnotation == null || schemaAnnotation.description().isEmpty()) {
        currencySchema.setDescription("ISO 4217 currency code (e.g., 'USD', 'EUR')");
      }
      fieldSchema = currencySchema;
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
   * Handles both @NotNull and @NotNull.List annotations.
   */
  private static boolean isFieldRequired(Field field) {
    // Check @NotNull annotation
    if (field.getAnnotation(NotNull.class) != null) {
      return true;
    }
    
    // Check @NotNull.List annotation (used on collections)
    if (field.getAnnotation(NotNull.List.class) != null) {
      return true;
    }
    
    return false;
  }
  
  /**
   * Checks if a type is a simple/primitive type.
   * Includes all basic Java types plus common date/time and numeric types.
   */
  private static boolean isSimpleType(Class<?> clazz) {
    return clazz == String.class || 
           clazz == Integer.class || clazz == int.class ||
           clazz == Long.class || clazz == long.class ||
           clazz == Double.class || clazz == double.class ||
           clazz == Float.class || clazz == float.class ||
           clazz == Boolean.class || clazz == boolean.class ||
           clazz == BigDecimal.class ||
           clazz == LocalDateTime.class ||
           clazz == LocalDate.class ||
           clazz.isEnum() ||
           clazz.getName().equals("org.joda.money.Money") ||
           clazz.getName().equals("org.joda.money.CurrencyUnit");
  }
  
  /**
   * Creates a schema for simple/primitive types.
   * Handles all basic types including dates, decimals, and enums.
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
    } else if (clazz == Boolean.class || clazz == boolean.class) {
      return new BooleanSchema();
    } else if (clazz == Double.class || clazz == double.class) {
      NumberSchema schema = new NumberSchema();
      schema.setFormat("double");
      return schema;
    } else if (clazz == Float.class || clazz == float.class) {
      NumberSchema schema = new NumberSchema();
      schema.setFormat("float");
      return schema;
    } else if (clazz == BigDecimal.class) {
      NumberSchema schema = new NumberSchema();
      schema.setFormat("decimal");
      return schema;
    } else if (clazz == LocalDateTime.class) {
      DateTimeSchema schema = new DateTimeSchema();
      schema.setFormat("date-time");
      return schema;
    } else if (clazz == LocalDate.class) {
      DateSchema schema = new DateSchema();
      schema.setFormat("date");
      return schema;
    } else if (clazz.isEnum()) {
      StringSchema schema = new StringSchema();
      Object[] enumConstants = clazz.getEnumConstants();
      List<String> enumValues = new ArrayList<>();
      for (Object enumConstant : enumConstants) {
        enumValues.add(enumConstant.toString());
      }
      schema.setEnum(enumValues);
      return schema;
    } else if (clazz.getName().equals("org.joda.money.Money")) {
      StringSchema schema = new StringSchema();
      schema.setFormat("money");
      return schema;
    } else if (clazz.getName().equals("org.joda.money.CurrencyUnit")) {
      StringSchema schema = new StringSchema();
      schema.setFormat("currency");
      return schema;
    }
    return new io.swagger.v3.oas.models.media.Schema<>();
  }
}
