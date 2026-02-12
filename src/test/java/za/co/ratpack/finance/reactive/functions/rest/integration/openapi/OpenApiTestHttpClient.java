package za.co.ratpack.finance.reactive.functions.rest.integration.openapi;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ratpack.func.Action;
import ratpack.http.client.ReceivedResponse;
import ratpack.http.client.RequestSpec;
import ratpack.test.http.TestHttpClient;

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
  private static final Pattern CURRENCY_CODE_PATTERN = Pattern.compile("^[A-Z]{3}$");
  
  private final TestHttpClient delegate;
  private final OpenApiCapture capture;
  private String testClassName;
  private String testMethodName;
  private String annotationPath;
  private String annotationSummary;
  private String annotationDescription;
  private String[] annotationTags;
  private RequestCaptureData pendingRequestData;
  
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
   * Sets the current test context including annotation data.
   */
  public void setTestContext(String className, String methodName, 
                            String annotationPath, String annotationSummary, 
                            String annotationDescription, String[] annotationTags) {
    this.testClassName = className;
    this.testMethodName = methodName;
    this.annotationPath = annotationPath;
    this.annotationSummary = annotationSummary;
    this.annotationDescription = annotationDescription;
    this.annotationTags = annotationTags;
  }
  
  /**
   * Supports requestSpec() chaining like TestHttpClient.
   * Captures request headers and body automatically.
   */
  public OpenApiTestHttpClient requestSpec(Action<? super RequestSpec> requestSpec) {
    // Initialize pending request data
    if (pendingRequestData == null) {
      pendingRequestData = new RequestCaptureData();
    }
    
    // Capture the reference for use in the lambda
    final RequestCaptureData captureData = pendingRequestData;
    
    // Pass through to delegate and capture headers and body after execution
    delegate.requestSpec(spec -> {
      // Execute the original request spec
      requestSpec.execute(spec);
      
      // Capture headers after configuration
      spec.getHeaders().getNames().forEach(name -> {
        String value = spec.getHeaders().get(name);
        if (value != null && captureData != null) {
          captureData.headers.put(name, value);
        }
      });
      
      // Capture content type
      if (captureData != null) {
        captureData.contentType = spec.getHeaders().get("Content-Type");
        
        // Note: Cannot auto-capture request body from RequestSpec because Body interface
        // is write-only (for setting content) and doesn't provide methods to read back.
        // Use .withBody() method explicitly to capture request bodies.
      }
    });
    
    return this;
  }
  
  /**
   * Helper method to set request body and capture it for OpenAPI spec.
   * Usage: apiClient.withBody(jsonString).requestSpec(...).post(...)
   */
  public OpenApiTestHttpClient withBody(String bodyText) {
    if (pendingRequestData == null) {
      pendingRequestData = new RequestCaptureData();
    }
    pendingRequestData.body = bodyText;
    return this;
  }
  
  /**
   * GET request.
   */
  public ReceivedResponse get(String path) {
    ReceivedResponse response = delegate.get(path);
    captureInteraction("GET", path, response);
    clearPendingRequest();
    return response;
  }
  
  /**
   * POST request.
   */
  public ReceivedResponse post(String path) {
    ReceivedResponse response = delegate.post(path);
    captureInteraction("POST", path, response);
    clearPendingRequest();
    return response;
  }
  
  /**
   * PUT request.
   */
  public ReceivedResponse put(String path) {
    ReceivedResponse response = delegate.put(path);
    captureInteraction("PUT", path, response);
    clearPendingRequest();
    return response;
  }
  
  /**
   * DELETE request.
   */
  public ReceivedResponse delete(String path) {
    ReceivedResponse response = delegate.delete(path);
    captureInteraction("DELETE", path, response);
    clearPendingRequest();
    return response;
  }
  
  /**
   * PATCH request.
   */
  public ReceivedResponse patch(String path) {
    ReceivedResponse response = delegate.patch(path);
    captureInteraction("PATCH", path, response);
    clearPendingRequest();
    return response;
  }
  
  /**
   * Clears pending request data.
   */
  private void clearPendingRequest() {
    pendingRequestData = null;
  }
  
  /**
   * Captures the HTTP interaction.
   */
  private void captureInteraction(String method, String path, ReceivedResponse response) {
    try {
      // Extract request information
      Map<String, String> requestHeaders = new HashMap<>();
      String requestBody = null;
      String requestContentType = null;
      
      if (pendingRequestData != null) {
        requestHeaders.putAll(pendingRequestData.headers);
        requestBody = pendingRequestData.body;
        requestContentType = pendingRequestData.contentType;
      }
      
      // Extract response headers
      Map<String, String> responseHeaders = new HashMap<>();
      response.getHeaders().getNames().forEach(name -> {
        responseHeaders.put(name, response.getHeaders().get(name));
      });
      
      // Normalize the path for OpenAPI
      String normalizedPath = normalizePath(path);
      
      CapturedInteraction interaction = CapturedInteraction.builder()
        .method(method)
        .requestPath(path)
        .normalizedPath(normalizedPath)
        .requestHeaders(requestHeaders)
        .requestContentType(requestContentType)
        .requestBody(requestBody)
        .responseStatusCode(response.getStatusCode())
        .responseHeaders(responseHeaders)
        .responseContentType(response.getHeaders().get("Content-Type"))
        .responseBody(response.getBody().getText())
        .testClassName(testClassName)
        .testMethodName(testMethodName)
        .annotationPath(annotationPath)
        .annotationSummary(annotationSummary)
        .annotationDescription(annotationDescription)
        .annotationTags(annotationTags)
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
   * - Currency codes (3 uppercase letters) → {currencyCode}
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
      // Check for currency code pattern (3 uppercase letters)
      else if (CURRENCY_CODE_PATTERN.matcher(segment).matches()) {
        normalized.append("{currencyCode}");
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
  
  /**
   * Internal class to hold pending request capture data.
   */
  private static class RequestCaptureData {
    Map<String, String> headers = new HashMap<>();
    String body;
    String contentType;
  }
}
