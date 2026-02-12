package za.co.ratpack.finance.reactive.functions.rest.integration.openapi;

import org.junit.jupiter.api.extension.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JUnit 5 extension that enables OpenAPI spec generation from test interactions.
 * Automatically captures HTTP interactions during tests and generates an OpenAPI spec.
 *
 * Usage:
 * <pre>
 * &#64;ExtendWith(OpenApiSpecExtension.class)
 * class MyIntegrationTest {
 *     // test methods
 * }
 * </pre>
 *
 * @author markmngoma
 */
public class OpenApiSpecExtension implements BeforeEachCallback, AfterEachCallback, AfterAllCallback {
    private static final Logger logger = LoggerFactory.getLogger(OpenApiSpecExtension.class);
    
    private static final ThreadLocal<OpenApiSpecWriter> writerThreadLocal = new ThreadLocal<>();
    
    @Override
    public void beforeEach(ExtensionContext context) {
        // Enable capture for this test
        OpenApiCapture.enable();
        
        // Create a writer for this thread if not exists
        if (writerThreadLocal.get() == null) {
            OpenApiSpecWriter writer = new OpenApiSpecWriter();
            writerThreadLocal.set(writer);
        }
        
        logger.debug("OpenAPI capture enabled for test: {}", context.getDisplayName());
    }
    
    @Override
    public void afterEach(ExtensionContext context) {
        // Process captured interactions
        List<CapturedInteraction> interactions = OpenApiCapture.getInteractions();
        OpenApiSpecWriter writer = writerThreadLocal.get();
        
        if (writer != null && !interactions.isEmpty()) {
            logger.debug("Processing {} captured interactions from test: {}", 
                        interactions.size(), context.getDisplayName());
            
            for (CapturedInteraction interaction : interactions) {
                processInteraction(writer, interaction);
            }
        }
        
        // Clear interactions for this test
        OpenApiCapture.clear();
    }
    
    @Override
    public void afterAll(ExtensionContext context) {
        // Generate and write OpenAPI spec
        OpenApiSpecWriter writer = writerThreadLocal.get();
        
        if (writer != null) {
            try {
                Map<String, Object> spec = generateSpec(writer);
                writeSpec(spec, context);
                logger.info("OpenAPI specification generated successfully");
            } catch (Exception e) {
                logger.error("Failed to generate OpenAPI specification", e);
            }
        }
        
        // Cleanup
        OpenApiCapture.cleanup();
        writerThreadLocal.remove();
    }
    
    /**
     * Processes a single interaction.
     */
    private void processInteraction(OpenApiSpecWriter writer, CapturedInteraction interaction) {
        // Try to introspect request body
        if (interaction.getRequestBody() != null && !interaction.getRequestBody().isEmpty()) {
            writer.tryIntrospectKnownClass(interaction.getRequestBody());
        }
        
        // Try to introspect response body
        if (interaction.getResponseBody() != null && !interaction.getResponseBody().isEmpty()) {
            writer.tryIntrospectKnownClass(interaction.getResponseBody());
        }
    }
    
    /**
     * Generates the OpenAPI specification structure.
     */
    private Map<String, Object> generateSpec(OpenApiSpecWriter writer) {
        Map<String, Object> spec = new LinkedHashMap<>();
        spec.put("openapi", "3.0.3");
        
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("title", "Reactive Finance API");
        info.put("version", "1.0.0");
        spec.put("info", info);
        
        Map<String, Object> components = new LinkedHashMap<>();
        components.put("schemas", writer.getComponentSchemas());
        spec.put("components", components);
        
        return spec;
    }
    
    /**
     * Writes the spec to a file.
     */
    private void writeSpec(Map<String, Object> spec, ExtensionContext context) throws IOException {
        Path outputPath = Paths.get("build", "openapi-spec.json");
        Files.createDirectories(outputPath.getParent());
        
        // Simple JSON serialization (in a real implementation, use Jackson or similar)
        String json = simpleJsonSerialize(spec);
        Files.writeString(outputPath, json);
        
        logger.info("OpenAPI spec written to: {}", outputPath.toAbsolutePath());
    }
    
    /**
     * Simple JSON serialization for the spec.
     */
    private String simpleJsonSerialize(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        serializeMap(map, sb, 1);
        sb.append("\n}");
        return sb.toString();
    }
    
    private void serializeMap(Map<String, Object> map, StringBuilder sb, int indent) {
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                sb.append(",\n");
            }
            first = false;
            
            appendIndent(sb, indent);
            sb.append("\"").append(entry.getKey()).append("\": ");
            serializeValue(entry.getValue(), sb, indent);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void serializeValue(Object value, StringBuilder sb, int indent) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof String) {
            sb.append("\"").append(value).append("\"");
        } else if (value instanceof Number || value instanceof Boolean) {
            sb.append(value);
        } else if (value instanceof Map) {
            sb.append("{\n");
            serializeMap((Map<String, Object>) value, sb, indent + 1);
            sb.append("\n");
            appendIndent(sb, indent);
            sb.append("}");
        } else if (value instanceof List) {
            sb.append("[\n");
            serializeList((List<?>) value, sb, indent + 1);
            sb.append("\n");
            appendIndent(sb, indent);
            sb.append("]");
        } else {
            sb.append("\"").append(value.toString()).append("\"");
        }
    }
    
    private void serializeList(List<?> list, StringBuilder sb, int indent) {
        boolean first = true;
        for (Object item : list) {
            if (!first) {
                sb.append(",\n");
            }
            first = false;
            appendIndent(sb, indent);
            serializeValue(item, sb, indent);
        }
    }
    
    private void appendIndent(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append("  ");
        }
    }
}
