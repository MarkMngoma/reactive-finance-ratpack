package za.co.ratpack.finance.reactive.functions.rest.integration.openapi;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;

/**
 * JUnit 5 extension that captures OpenAPI interactions during tests and writes
 * the spec after all tests complete.
 * 
 * @author Generated
 * @created at 14:13 on 11/02/2026
 */
public class OpenApiSpecExtension implements AfterAllCallback, BeforeEachCallback {
  
  private static final Logger LOG = LoggerFactory.getLogger(OpenApiSpecExtension.class);
  
  private static final String DEFAULT_SPEC_PATH = "src/test/resources/spec/openapi.yaml";
  private static final String DEFAULT_TITLE = "Reactive Finance Ratpack API";
  private static final String DEFAULT_VERSION = "1.0.0";
  
  @Override
  public void beforeEach(ExtensionContext context) {
    // Store current test class/method name for operationId generation
    String testClassName = context.getTestClass()
      .map(Class::getSimpleName)
      .orElse("UnknownTest");
    
    String testMethodName = context.getTestMethod()
      .map(method -> method.getName())
      .orElse("unknownMethod");
    
    // Check for @DocumentApi annotation
    String annotationPath = "";
    String annotationSummary = "";
    String annotationDescription = "";
    String[] annotationTags = new String[0];
    
    if (context.getTestMethod().isPresent()) {
      Method method = context.getTestMethod().get();
      DocumentApi documentApi = method.getAnnotation(DocumentApi.class);
      
      if (documentApi != null) {
        annotationPath = documentApi.path();
        annotationSummary = documentApi.summary();
        annotationDescription = documentApi.description();
        annotationTags = documentApi.tags();
        
        LOG.debug("Found @DocumentApi on {}.{}: path={}, summary={}, description={}, tags={}", 
                 testClassName, testMethodName, annotationPath, annotationSummary, 
                 annotationDescription, String.join(",", annotationTags));
      }
    }
    
    // Store in extension context for use by test
    ExtensionContext.Store store = context.getStore(ExtensionContext.Namespace.create(getClass()));
    store.put("testClassName", testClassName);
    store.put("testMethodName", testMethodName);
    store.put("annotationPath", annotationPath);
    store.put("annotationSummary", annotationSummary);
    store.put("annotationDescription", annotationDescription);
    store.put("annotationTags", annotationTags);
    
    LOG.debug("BeforeEach: {} - {}", testClassName, testMethodName);
  }
  
  @Override
  public void afterAll(ExtensionContext context) {
    try {
      OpenApiCapture capture = OpenApiCapture.getInstance();
      List<CapturedInteraction> interactions = capture.getInteractions();
      
      if (interactions.isEmpty()) {
        LOG.info("No interactions captured, skipping OpenAPI spec generation");
        return;
      }
      
      LOG.info("Generating OpenAPI spec from {} captured interactions", interactions.size());
      
      // Get configuration from system properties
      String specPath = System.getProperty("openapi.spec.path", DEFAULT_SPEC_PATH);
      String title = System.getProperty("openapi.spec.title", DEFAULT_TITLE);
      String version = System.getProperty("openapi.spec.version", DEFAULT_VERSION);
      
      // Write the spec
      OpenApiSpecWriter writer = new OpenApiSpecWriter(specPath, title, version);
      writer.writeSpec(interactions);
      
      // Clear captured interactions
      capture.clear();
      
      LOG.info("OpenAPI spec generation completed");
      
    } catch (Exception e) {
      LOG.error("Failed to generate OpenAPI spec", e);
      // Don't fail the tests if spec generation fails
    }
  }
}
