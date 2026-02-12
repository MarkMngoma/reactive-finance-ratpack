package za.co.ratpack.finance.reactive.functions.rest.integration.openapi;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a captured HTTP request/response interaction for OpenAPI spec generation.
 *
 * @author markmngoma
 */
public class CapturedInteraction {
    private final String method;
    private final String path;
    private final int statusCode;
    private final String requestBody;
    private final String responseBody;
    private final Map<String, String> requestHeaders;
    private final Map<String, String> responseHeaders;
    
    private CapturedInteraction(Builder builder) {
        this.method = builder.method;
        this.path = builder.path;
        this.statusCode = builder.statusCode;
        this.requestBody = builder.requestBody;
        this.responseBody = builder.responseBody;
        this.requestHeaders = builder.requestHeaders;
        this.responseHeaders = builder.responseHeaders;
    }
    
    public String getMethod() {
        return method;
    }
    
    public String getPath() {
        return path;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public String getRequestBody() {
        return requestBody;
    }
    
    public String getResponseBody() {
        return responseBody;
    }
    
    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }
    
    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String method;
        private String path;
        private int statusCode;
        private String requestBody;
        private String responseBody;
        private Map<String, String> requestHeaders = new LinkedHashMap<>();
        private Map<String, String> responseHeaders = new LinkedHashMap<>();
        
        public Builder method(String method) {
            this.method = method;
            return this;
        }
        
        public Builder path(String path) {
            this.path = path;
            return this;
        }
        
        public Builder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }
        
        public Builder requestBody(String requestBody) {
            this.requestBody = requestBody;
            return this;
        }
        
        public Builder responseBody(String responseBody) {
            this.responseBody = responseBody;
            return this;
        }
        
        public Builder requestHeaders(Map<String, String> requestHeaders) {
            this.requestHeaders = new LinkedHashMap<>(requestHeaders);
            return this;
        }
        
        public Builder responseHeaders(Map<String, String> responseHeaders) {
            this.responseHeaders = new LinkedHashMap<>(responseHeaders);
            return this;
        }
        
        public CapturedInteraction build() {
            return new CapturedInteraction(this);
        }
    }
}
