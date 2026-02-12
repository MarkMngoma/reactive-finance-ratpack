package za.co.ratpack.finance.reactive.functions.rest.integration.openapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Test HTTP client that captures interactions for OpenAPI spec generation.
 *
 * @author markmngoma
 */
public class OpenApiTestHttpClient {
    private static final Logger logger = LoggerFactory.getLogger(OpenApiTestHttpClient.class);
    
    private final HttpClient httpClient;
    private final String baseUrl;
    
    public OpenApiTestHttpClient(String baseUrl) {
        this.httpClient = HttpClient.newHttpClient();
        this.baseUrl = baseUrl;
    }
    
    /**
     * Sends a GET request.
     */
    public HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        captureInteraction("GET", path, null, response);
        
        return response;
    }
    
    /**
     * Sends a POST request.
     */
    public HttpResponse<String> post(String path, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        captureInteraction("POST", path, body, response);
        
        return response;
    }
    
    /**
     * Sends a PUT request.
     */
    public HttpResponse<String> put(String path, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        captureInteraction("PUT", path, body, response);
        
        return response;
    }
    
    /**
     * Sends a DELETE request.
     */
    public HttpResponse<String> delete(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .DELETE()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        captureInteraction("DELETE", path, null, response);
        
        return response;
    }
    
    /**
     * Captures the interaction if capture is enabled.
     */
    private void captureInteraction(String method, String path, String requestBody, HttpResponse<String> response) {
        if (!OpenApiCapture.isEnabled()) {
            return;
        }
        
        Map<String, String> requestHeaders = new LinkedHashMap<>();
        if (requestBody != null) {
            requestHeaders.put("Content-Type", "application/json");
        }
        
        Map<String, String> responseHeaders = new LinkedHashMap<>();
        response.headers().map().forEach((key, values) -> {
            if (!values.isEmpty()) {
                responseHeaders.put(key, values.get(0));
            }
        });
        
        CapturedInteraction interaction = CapturedInteraction.builder()
                .method(method)
                .path(path)
                .requestBody(requestBody)
                .statusCode(response.statusCode())
                .responseBody(response.body())
                .requestHeaders(requestHeaders)
                .responseHeaders(responseHeaders)
                .build();
        
        OpenApiCapture.capture(interaction);
        
        logger.debug("Captured {} {} - status {}", method, path, response.statusCode());
    }
}
