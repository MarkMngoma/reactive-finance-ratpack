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
  private String lastRequestBody;
  private String lastRequestContentType;
  
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
   * Wraps the consumer to capture request body and headers.
   */
  public OpenApiTestHttpClient requestSpec(Consumer<RequestSpec> requestSpec) {
    delegate.requestSpec(spec -> {
      // Wrap to capture request body
      requestSpec.accept(new RequestSpecCapture(spec, this));
    });
    return this;
  }
  
  /**
   * Sets the captured request body for the next request.
   */
  void setLastRequestBody(String body, String contentType) {
    this.lastRequestBody = body;
    this.lastRequestContentType = contentType;
  }
  
  /**
   * GET request.
   */
  public ReceivedResponse get(String path) {
    ReceivedResponse response = delegate.get(path);
    captureInteraction("GET", path, response);
    clearRequestState();
    return response;
  }
  
  /**
   * POST request.
   */
  public ReceivedResponse post(String path) {
    ReceivedResponse response = delegate.post(path);
    captureInteraction("POST", path, response);
    clearRequestState();
    return response;
  }
  
  /**
   * PUT request.
   */
  public ReceivedResponse put(String path) {
    ReceivedResponse response = delegate.put(path);
    captureInteraction("PUT", path, response);
    clearRequestState();
    return response;
  }
  
  /**
   * DELETE request.
   */
  public ReceivedResponse delete(String path) {
    ReceivedResponse response = delegate.delete(path);
    captureInteraction("DELETE", path, response);
    clearRequestState();
    return response;
  }
  
  /**
   * PATCH request.
   */
  public ReceivedResponse patch(String path) {
    ReceivedResponse response = delegate.patch(path);
    captureInteraction("PATCH", path, response);
    clearRequestState();
    return response;
  }
  
  /**
   * Clears request state after a request.
   */
  private void clearRequestState() {
    lastRequestBody = null;
    lastRequestContentType = null;
  }
  
  /**
   * Captures the HTTP interaction.
   */
  private void captureInteraction(String method, String path, ReceivedResponse response) {
    try {
      // Extract request information
      Map<String, String> requestHeaders = new HashMap<>();
      Map<String, String> responseHeaders = new HashMap<>();
      
      // Extract response headers
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
        .requestContentType(lastRequestContentType)
        .requestBody(lastRequestBody)
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

/**
 * Wrapper for RequestSpec that captures request body and content type.
 */
class RequestSpecCapture implements RequestSpec {
  
  private final RequestSpec delegate;
  private final OpenApiTestHttpClient client;
  
  RequestSpecCapture(RequestSpec delegate, OpenApiTestHttpClient client) {
    this.delegate = delegate;
    this.client = client;
  }
  
  @Override
  public ratpack.http.MutableHeaders getHeaders() {
    return delegate.getHeaders();
  }
  
  @Override
  public RequestSpec headers(ratpack.func.Action<? super ratpack.http.MutableHeaders> action) throws Exception {
    return delegate.headers(action);
  }
  
  @Override
  public ratpack.http.client.RequestSpec method(String method) {
    return delegate.method(method);
  }
  
  @Override
  public ratpack.http.client.RequestSpec method(io.netty.handler.codec.http.HttpMethod method) {
    return delegate.method(method);
  }
  
  @Override
  public ratpack.http.client.RequestSpec decompressResponse(boolean shouldDecompress) {
    return delegate.decompressResponse(shouldDecompress);
  }
  
  @Override
  public java.net.URI getUri() {
    return delegate.getUri();
  }
  
  @Override
  public ratpack.http.client.RequestSpec redirects(int maxRedirects) {
    return delegate.redirects(maxRedirects);
  }
  
  @Override
  public ratpack.http.client.RequestSpec onRedirect(ratpack.func.Function<? super ratpack.http.client.ReceivedResponse, ratpack.func.Action<? super RequestSpec>> function) {
    return delegate.onRedirect(function);
  }
  
  @Override
  public ratpack.http.client.RequestSpec sslContext(javax.net.ssl.SSLContext sslContext) {
    return delegate.sslContext(sslContext);
  }
  
  @Override
  public ratpack.http.Body getBody() {
    return delegate.getBody();
  }
  
  @Override
  public ratpack.http.client.RequestSpec body(ratpack.func.Action<? super ratpack.http.client.RequestSpec.Body> action) throws Exception {
    // Capture the body
    RequestSpec result = delegate.body(action);
    
    // Try to extract body content
    try {
      ratpack.http.Body body = delegate.getBody();
      if (body != null) {
        String bodyText = body.getText();
        String contentType = delegate.getHeaders().get("Content-Type");
        client.setLastRequestBody(bodyText, contentType);
      }
    } catch (Exception e) {
      // Ignore - body capture is best-effort
    }
    
    return result;
  }
  
  @Override
  public ratpack.http.client.RequestSpec basicAuth(String username, String password) {
    return delegate.basicAuth(username, password);
  }
  
  @Override
  public ratpack.http.client.RequestSpec connectTimeout(java.time.Duration duration) {
    return delegate.connectTimeout(duration);
  }
  
  @Override
  public ratpack.http.client.RequestSpec readTimeout(java.time.Duration duration) {
    return delegate.readTimeout(duration);
  }
  
  @Override
  public java.time.Duration getReadTimeout() {
    return delegate.getReadTimeout();
  }
  
  @Override
  public java.time.Duration getConnectTimeout() {
    return delegate.getConnectTimeout();
  }
}
