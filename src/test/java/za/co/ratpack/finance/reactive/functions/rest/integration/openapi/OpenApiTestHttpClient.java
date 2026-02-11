package za.co.ratpack.finance.reactive.functions.rest.integration.openapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.http.client.ReceivedResponse;
import ratpack.http.client.RequestSpec;
import ratpack.test.http.TestHttpClient;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Decorator/wrapper around Ratpack's TestHttpClient that intercepts HTTP calls
 * and captures interactions for OpenAPI spec generation.
 * 
 * @author Generated
 * @created at 14:13 on 11/02/2026
 */
public class OpenApiTestHttpClient {
  
  private static final Logger LOG = LoggerFactory.getLogger(OpenApiTestHttpClient.class);
  private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
  private static final Pattern NUMERIC_PATTERN = Pattern.compile("^\\d+$");
  
  private final TestHttpClient delegate;
  private final OpenApiCapture capture;
  private String testClassName;
  private String testMethodName;
  private RequestSpec currentRequestSpec;
  
  public OpenApiTestHttpClient(TestHttpClient delegate) {
    this.delegate = delegate;
    this.capture = OpenApiCapture.getInstance();
  }
  
  /**
   * Sets the current test context for operationId generation.
   */
  public void setTestContext(String className, String methodName) {
    this.testClassName = className;
    this.testMethodName = methodName;
  }
  
  /**
   * Supports requestSpec() chaining like TestHttpClient.
   */
  public OpenApiTestHttpClient requestSpec(Consumer<RequestSpec> requestSpec) {
    delegate.requestSpec(requestSpec);
    return this;
  }
  
  /**
   * GET request.
   */
  public ReceivedResponse get(String path) {
    ReceivedResponse response = delegate.get(path);
    captureInteraction("GET", path, null, null, response);
    return response;
  }
  
  /**
   * POST request.
   */
  public ReceivedResponse post(String path) {
    ReceivedResponse response = delegate.post(path);
    captureInteraction("POST", path, null, null, response);
    return response;
  }
  
  /**
   * PUT request.
   */
  public ReceivedResponse put(String path) {
    ReceivedResponse response = delegate.put(path);
    captureInteraction("PUT", path, null, null, response);
    return response;
  }
  
  /**
   * DELETE request.
   */
  public ReceivedResponse delete(String path) {
    ReceivedResponse response = delegate.delete(path);
    captureInteraction("DELETE", path, null, null, response);
    return response;
  }
  
  /**
   * PATCH request.
   */
  public ReceivedResponse patch(String path) {
    ReceivedResponse response = delegate.patch(path);
    captureInteraction("PATCH", path, null, null, response);
    return response;
  }
  
  /**
   * Captures the HTTP interaction.
   */
  private void captureInteraction(String method, String path, String requestBody, 
                                   String requestContentType, ReceivedResponse response) {
    try {
      // Extract request information from delegate if available
      Map<String, String> requestHeaders = new HashMap<>();
      Map<String, String> responseHeaders = new HashMap<>();
      
      // Extract response headers
      response.getHeaders().getNames().forEach(name -> {
        responseHeaders.put(name, response.getHeaders().get(name));
      });
      
      // Normalize the path for OpenAPI
      String normalizedPath = normalizePath(path);
      
      // Try to get request body if the method supports it
      String actualRequestBody = requestBody;
      String actualRequestContentType = requestContentType;
      
      // For POST, PUT, PATCH we should capture the request body
      // The delegate's internal state might have this, but we'll capture what we can
      if (method.equals("POST") || method.equals("PUT") || method.equals("PATCH")) {
        // Request body would need to be captured before the call
        // For now, we'll leave it as is and enhance later if needed
      }
      
      CapturedInteraction interaction = CapturedInteraction.builder()
        .method(method)
        .requestPath(path)
        .normalizedPath(normalizedPath)
        .requestHeaders(requestHeaders)
        .requestContentType(actualRequestContentType)
        .requestBody(actualRequestBody)
        .responseStatusCode(response.getStatusCode())
        .responseHeaders(responseHeaders)
        .responseContentType(response.getHeaders().get("Content-Type"))
        .responseBody(response.getBody().getText())
        .testClassName(testClassName)
        .testMethodName(testMethodName)
        .build();
      
      capture.record(interaction);
      
    } catch (Exception e) {
      LOG.warn("Failed to capture interaction for {} {}", method, path, e);
    }
  }
  
  /**
   * Normalizes a concrete path to an OpenAPI path template.
   * Handles:
   * - UUID segments → {id}
   * - Numeric segments (not first) → {id}
   * - Known Ratpack tokens like :currencyCode → {currencyCode}
   */
  private String normalizePath(String path) {
    String[] segments = path.split("/");
    StringBuilder normalized = new StringBuilder();
    
    for (int i = 0; i < segments.length; i++) {
      String segment = segments[i];
      
      if (segment.isEmpty()) {
        continue;
      }
      
      normalized.append("/");
      
      // Check for Ratpack-style path parameters (e.g., :currencyCode)
      if (segment.startsWith(":")) {
        normalized.append("{").append(segment.substring(1)).append("}");
      }
      // Check for UUID
      else if (UUID_PATTERN.matcher(segment).matches()) {
        normalized.append("{id}");
      }
      // Check for numeric ID (but not in first position after host)
      else if (i > 0 && NUMERIC_PATTERN.matcher(segment).matches()) {
        normalized.append("{id}");
      }
      // Otherwise keep as-is
      else {
        normalized.append(segment);
      }
    }
    
    return normalized.toString();
  }
  
  /**
   * Returns the underlying TestHttpClient for direct access if needed.
   */
  public TestHttpClient getDelegate() {
    return delegate;
  }
}
