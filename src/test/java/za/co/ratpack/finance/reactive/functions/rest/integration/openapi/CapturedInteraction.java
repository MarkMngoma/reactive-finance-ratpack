package za.co.ratpack.finance.reactive.functions.rest.integration.openapi;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * POJO that records a single HTTP request/response interaction.
 * 
 * @author Generated
 * @created at 14:13 on 11/02/2026
 */
@Data
@Builder
public class CapturedInteraction {
  
  /**
   * HTTP method (GET, POST, PUT, DELETE, PATCH).
   */
  private String method;
  
  /**
   * Concrete request path (e.g., /v1/QueryCurrencyResource/ZAR).
   */
  private String requestPath;
  
  /**
   * Normalized path pattern for OpenAPI (e.g., /v1/QueryCurrencyResource/{currencyCode}).
   */
  private String normalizedPath;
  
  /**
   * Request headers.
   */
  private Map<String, String> requestHeaders;
  
  /**
   * Request content type.
   */
  private String requestContentType;
  
  /**
   * Request body.
   */
  private String requestBody;
  
  /**
   * Response status code.
   */
  private int responseStatusCode;
  
  /**
   * Response headers.
   */
  private Map<String, String> responseHeaders;
  
  /**
   * Response content type.
   */
  private String responseContentType;
  
  /**
   * Response body.
   */
  private String responseBody;
  
  /**
   * Test class name (for operationId generation).
   */
  private String testClassName;
  
  /**
   * Test method name (for operationId generation).
   */
  private String testMethodName;
}
