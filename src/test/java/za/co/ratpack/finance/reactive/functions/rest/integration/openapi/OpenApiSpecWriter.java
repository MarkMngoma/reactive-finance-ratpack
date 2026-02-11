package za.co.ratpack.finance.reactive.functions.rest.integration.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.Components;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Reads an existing OpenAPI YAML spec or creates a new one, then merges
 * all captured interactions into it.
 * 
 * @author Generated
 * @created at 14:13 on 11/02/2026
 */
public class OpenApiSpecWriter {
  
  private static final Logger LOG = LoggerFactory.getLogger(OpenApiSpecWriter.class);
  private static final Pattern PATH_PARAM_PATTERN = Pattern.compile("\\{([^}]+)\\}");
  
  private final ObjectMapper jsonMapper;
  private final String specPath;
  private final String title;
  private final String version;
  
  public OpenApiSpecWriter(String specPath, String title, String version) {
    this.specPath = specPath;
    this.title = title;
    this.version = version;
    this.jsonMapper = new ObjectMapper();
  }
  
  /**
   * Writes the OpenAPI spec from all captured interactions.
   */
  public void writeSpec(List<CapturedInteraction> interactions) {
    try {
      OpenAPI openAPI = loadExistingSpec();
      
      if (openAPI == null) {
        openAPI = createNewSpec();
      }
      
      // Ensure components are initialized
      if (openAPI.getComponents() == null) {
        openAPI.setComponents(new Components());
      }
      if (openAPI.getComponents().getSchemas() == null) {
        openAPI.getComponents().setSchemas(new HashMap<>());
      }
      
      // Group interactions by normalized path + method
      Map<String, List<CapturedInteraction>> groupedInteractions = interactions.stream()
        .collect(Collectors.groupingBy(i -> i.getNormalizedPath() + ":" + i.getMethod()));
      
      // Process each group
      for (Map.Entry<String, List<CapturedInteraction>> entry : groupedInteractions.entrySet()) {
        processInteractionGroup(openAPI, entry.getValue());
      }
      
      // Write the spec
      writeSpecToFile(openAPI);
      
      LOG.info("OpenAPI spec written to: {}", specPath);
      
    } catch (Exception e) {
      LOG.error("Failed to write OpenAPI spec", e);
      throw new RuntimeException("Failed to write OpenAPI spec", e);
    }
  }
  
  /**
   * Loads an existing OpenAPI spec from the file system.
   */
  private OpenAPI loadExistingSpec() {
    try {
      java.nio.file.Path path = java.nio.file.Paths.get(specPath);
      if (Files.exists(path)) {
        String content = Files.readString(path);
        return Yaml.mapper().readValue(content, OpenAPI.class);
      }
    } catch (Exception e) {
      LOG.warn("Could not load existing spec from {}, creating new one", specPath, e);
    }
    return null;
  }
  
  /**
   * Creates a new OpenAPI spec skeleton.
   */
  private OpenAPI createNewSpec() {
    OpenAPI openAPI = new OpenAPI();
    
    Info info = new Info()
      .title(title)
      .version(version)
      .description("Auto-generated from integration tests");
    
    openAPI.setInfo(info);
    openAPI.setOpenapi("3.0.3");
    openAPI.setPaths(new Paths());
    openAPI.setComponents(new Components());
    openAPI.getComponents().setSchemas(new HashMap<>());
    
    return openAPI;
  }
  
  /**
   * Processes a group of interactions for the same path + method.
   */
  private void processInteractionGroup(OpenAPI openAPI, List<CapturedInteraction> interactions) {
    if (interactions.isEmpty()) {
      return;
    }
    
    CapturedInteraction sample = interactions.get(0);
    String path = sample.getNormalizedPath();
    String method = sample.getMethod().toLowerCase();
    
    // Get or create PathItem
    PathItem pathItem = openAPI.getPaths().get(path);
    if (pathItem == null) {
      pathItem = new PathItem();
      openAPI.getPaths().addPathItem(path, pathItem);
    }
    
    // Create Operation
    Operation operation = createOperation(sample, interactions);
    
    // Set operation on the path based on method
    switch (method) {
      case "get" -> pathItem.setGet(operation);
      case "post" -> pathItem.setPost(operation);
      case "put" -> pathItem.setPut(operation);
      case "delete" -> pathItem.setDelete(operation);
      case "patch" -> pathItem.setPatch(operation);
      default -> LOG.warn("Unsupported HTTP method: {}", method);
    }
  }
  
  /**
   * Creates an Operation from a sample interaction and all interactions in the group.
   */
  private Operation createOperation(CapturedInteraction sample, List<CapturedInteraction> allInteractions) {
    Operation operation = new Operation();
    
    // Generate operationId
    String operationId = generateOperationId(sample);
    operation.setOperationId(operationId);
    
    // Set summary and description if available
    String summary = generateSummary(sample);
    operation.setSummary(summary);
    
    // Extract path parameters
    List<Parameter> parameters = extractPathParameters(sample.getNormalizedPath());
    
    // Add common request headers as parameters
    if (sample.getRequestHeaders() != null && !sample.getRequestHeaders().isEmpty()) {
      parameters.addAll(extractCommonHeaders(sample.getRequestHeaders()));
    }
    
    if (!parameters.isEmpty()) {
      operation.setParameters(parameters);
    }
    
    // Add request body if applicable
    if (sample.getRequestBody() != null && !sample.getRequestBody().isEmpty()) {
      RequestBody requestBody = createRequestBody(sample);
      operation.setRequestBody(requestBody);
    }
    
    // Add responses
    ApiResponses responses = createResponses(allInteractions);
    operation.setResponses(responses);
    
    return operation;
  }
  
  /**
   * Generates an operationId from the interaction.
   */
  private String generateOperationId(CapturedInteraction interaction) {
    if (interaction.getTestMethodName() != null) {
      return interaction.getTestMethodName();
    }
    
    // Fallback: generate from path and method
    String path = interaction.getNormalizedPath()
      .replaceAll("[^a-zA-Z0-9]", "_")
      .replaceAll("_+", "_")
      .replaceAll("^_|_$", "");
    
    return interaction.getMethod().toLowerCase() + "_" + path;
  }
  
  /**
   * Generates a summary from the interaction.
   */
  private String generateSummary(CapturedInteraction interaction) {
    String method = interaction.getMethod();
    String path = interaction.getNormalizedPath();
    
    return method + " " + path;
  }
  
  /**
   * Extracts path parameters from the normalized path.
   */
  private List<Parameter> extractPathParameters(String normalizedPath) {
    List<Parameter> parameters = new ArrayList<>();
    
    Matcher matcher = PATH_PARAM_PATTERN.matcher(normalizedPath);
    while (matcher.find()) {
      String paramName = matcher.group(1);
      
      PathParameter parameter = new PathParameter();
      parameter.setName(paramName);
      parameter.setRequired(true);
      parameter.setSchema(new StringSchema());
      
      parameters.add(parameter);
    }
    
    return parameters;
  }
  
  /**
   * Extracts common request headers as parameters.
   */
  private List<Parameter> extractCommonHeaders(Map<String, String> headers) {
    List<Parameter> parameters = new ArrayList<>();
    
    // Only document meaningful headers (Content-Type, Accept, etc.)
    for (Map.Entry<String, String> entry : headers.entrySet()) {
      String headerName = entry.getKey();
      String headerValue = entry.getValue();
      
      // Filter out common/standard headers that are usually auto-managed
      if (headerName.equalsIgnoreCase("host") || 
          headerName.equalsIgnoreCase("connection") ||
          headerName.equalsIgnoreCase("content-length")) {
        continue;
      }
      
      io.swagger.v3.oas.models.parameters.HeaderParameter parameter = 
        new io.swagger.v3.oas.models.parameters.HeaderParameter();
      parameter.setName(headerName);
      parameter.setRequired(false);
      parameter.setSchema(new StringSchema());
      parameter.setExample(headerValue);
      
      parameters.add(parameter);
    }
    
    return parameters;
  }
  
  /**
   * Creates a RequestBody from the interaction.
   */
  private RequestBody createRequestBody(CapturedInteraction interaction) {
    RequestBody requestBody = new RequestBody();
    requestBody.setRequired(true);
    
    Content content = new Content();
    MediaType mediaType = new MediaType();
    
    // Try to infer schema from JSON body
    if (interaction.getRequestBody() != null && isJson(interaction.getRequestContentType())) {
      try {
        Schema<?> schema = inferSchemaFromJson(interaction.getRequestBody());
        mediaType.setSchema(schema);
        // Add example from actual request body
        mediaType.setExample(parseJsonExample(interaction.getRequestBody()));
      } catch (Exception e) {
        LOG.warn("Could not infer schema from request body", e);
        mediaType.setSchema(new ObjectSchema());
      }
    } else {
      mediaType.setSchema(new ObjectSchema());
    }
    
    String contentType = interaction.getRequestContentType() != null 
      ? interaction.getRequestContentType() 
      : "application/json";
    
    content.addMediaType(contentType, mediaType);
    requestBody.setContent(content);
    
    return requestBody;
  }
  
  /**
   * Creates ApiResponses from all interactions in the group.
   */
  private ApiResponses createResponses(List<CapturedInteraction> interactions) {
    ApiResponses responses = new ApiResponses();
    
    // Group by status code
    Map<Integer, List<CapturedInteraction>> byStatus = interactions.stream()
      .collect(Collectors.groupingBy(CapturedInteraction::getResponseStatusCode));
    
    for (Map.Entry<Integer, List<CapturedInteraction>> entry : byStatus.entrySet()) {
      int statusCode = entry.getKey();
      CapturedInteraction sample = entry.getValue().get(0);
      
      ApiResponse apiResponse = new ApiResponse();
      apiResponse.setDescription(getStatusDescription(statusCode));
      
      // Add response headers
      if (sample.getResponseHeaders() != null && !sample.getResponseHeaders().isEmpty()) {
        Map<String, io.swagger.v3.oas.models.headers.Header> headerMap = new HashMap<>();
        sample.getResponseHeaders().forEach((name, value) -> {
          io.swagger.v3.oas.models.headers.Header header = new io.swagger.v3.oas.models.headers.Header();
          header.setSchema(new StringSchema());
          header.setExample(value);
          headerMap.put(name, header);
        });
        apiResponse.setHeaders(headerMap);
      }
      
      if (sample.getResponseBody() != null && !sample.getResponseBody().isEmpty()) {
        Content content = new Content();
        MediaType mediaType = new MediaType();
        
        // Try to infer schema from JSON body
        if (isJson(sample.getResponseContentType())) {
          try {
            Schema<?> schema = inferSchemaFromJson(sample.getResponseBody());
            mediaType.setSchema(schema);
            // Add example from actual response body
            mediaType.setExample(parseJsonExample(sample.getResponseBody()));
          } catch (Exception e) {
            LOG.warn("Could not infer schema from response body", e);
            mediaType.setSchema(new ObjectSchema());
          }
        } else {
          mediaType.setSchema(new StringSchema());
        }
        
        String contentType = sample.getResponseContentType() != null 
          ? sample.getResponseContentType() 
          : "application/json";
        
        content.addMediaType(contentType, mediaType);
        apiResponse.setContent(content);
      }
      
      responses.addApiResponse(String.valueOf(statusCode), apiResponse);
    }
    
    return responses;
  }
  
  /**
   * Infers a schema from JSON content.
   */
  private Schema<?> inferSchemaFromJson(String jsonContent) throws IOException {
    JsonNode node = jsonMapper.readTree(jsonContent);
    return inferSchemaFromJsonNode(node);
  }
  
  /**
   * Infers a schema from a JsonNode.
   */
  private Schema<?> inferSchemaFromJsonNode(JsonNode node) {
    if (node.isObject()) {
      ObjectSchema schema = new ObjectSchema();
      
      Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
      while (fields.hasNext()) {
        Map.Entry<String, JsonNode> field = fields.next();
        String fieldName = field.getKey();
        JsonNode fieldValue = field.getValue();
        
        Schema<?> fieldSchema = inferSchemaFromJsonNode(fieldValue);
        schema.addProperty(fieldName, fieldSchema);
      }
      
      return schema;
    } else if (node.isArray()) {
      io.swagger.v3.oas.models.media.ArraySchema schema = new io.swagger.v3.oas.models.media.ArraySchema();
      if (node.size() > 0) {
        schema.setItems(inferSchemaFromJsonNode(node.get(0)));
      }
      return schema;
    } else if (node.isNumber()) {
      if (node.isInt() || node.isLong()) {
        return new io.swagger.v3.oas.models.media.IntegerSchema();
      } else {
        return new io.swagger.v3.oas.models.media.NumberSchema();
      }
    } else if (node.isBoolean()) {
      return new io.swagger.v3.oas.models.media.BooleanSchema();
    } else {
      return new StringSchema();
    }
  }
  
  /**
   * Checks if a content type is JSON.
   */
  private boolean isJson(String contentType) {
    return contentType != null && contentType.toLowerCase().contains("json");
  }
  
  /**
   * Gets a description for a status code.
   */
  private String getStatusDescription(int statusCode) {
    return switch (statusCode) {
      case 200 -> "OK";
      case 201 -> "Created";
      case 204 -> "No Content";
      case 400 -> "Bad Request";
      case 401 -> "Unauthorized";
      case 403 -> "Forbidden";
      case 404 -> "Not Found";
      case 500 -> "Internal Server Error";
      default -> "Response";
    };
  }
  
  /**
   * Parses JSON string into an example object for OpenAPI.
   */
  private Object parseJsonExample(String jsonContent) {
    try {
      JsonNode node = jsonMapper.readTree(jsonContent);
      return jsonMapper.convertValue(node, Object.class);
    } catch (Exception e) {
      LOG.warn("Could not parse JSON example", e);
      return jsonContent;
    }
  }
  
  /**
   * Writes the OpenAPI spec to file.
   */
  private void writeSpecToFile(OpenAPI openAPI) throws IOException {
    java.nio.file.Path path = java.nio.file.Paths.get(specPath);
    
    // Ensure parent directory exists
    Files.createDirectories(path.getParent());
    
    // Write YAML
    String yaml = Yaml.pretty(openAPI);
    Files.writeString(path, yaml);
  }
}
