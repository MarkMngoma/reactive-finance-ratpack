package za.co.ratpack.finance.reactive.functions.handlers;

import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.handling.Context;
import ratpack.handling.Handler;

import java.io.IOException;
import java.io.InputStream;

/**
 * Handler to serve API documentation (Scalar UI and OpenAPI spec)
 * 
 * @author markmngoma
 * @created at 15:23 on 11/02/2026
 */
@Singleton
public class ApiDocsHandler implements Handler {

  private static final Logger LOG = LoggerFactory.getLogger(ApiDocsHandler.class);

  @Override
  public void handle(Context ctx) {
    String path = ctx.getRequest().getPath();
    
    // Extract the resource path after /api-docs
    String resourcePath;
    if (path.equals("/api-docs") || path.equals("/api-docs/")) {
      resourcePath = "index.html";
    } else if (path.startsWith("/api-docs/")) {
      resourcePath = path.substring("/api-docs/".length());
    } else {
      // Should not happen given routing, but handle gracefully
      resourcePath = "index.html";
    }
    
    // Construct full resource path
    String fullPath = "api-docs/" + resourcePath;
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("Serving API docs resource: {} (from path: {})", fullPath, path);
    }
    
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fullPath)) {
      if (inputStream == null) {
        if (LOG.isWarnEnabled()) {
          LOG.warn("API docs resource not found: {}", fullPath);
        }
        ctx.getResponse()
          .status(404)
          .send("API documentation resource not found: " + resourcePath);
        return;
      }
      
      // Determine content type based on file extension
      String contentType = determineContentType(resourcePath);
      
      // Read the content
      byte[] content = inputStream.readAllBytes();
      
      // Send the response
      ctx.getResponse()
        .contentType(contentType)
        .send(content);
        
    } catch (IOException e) {
      LOG.error("Error serving API docs resource: {}", fullPath, e);
      ctx.getResponse()
        .status(500)
        .send("Error serving API documentation: " + e.getMessage());
    }
  }
  
  private String determineContentType(String filename) {
    if (filename.endsWith(".html")) {
      return "text/html";
    } else if (filename.endsWith(".yaml") || filename.endsWith(".yml")) {
      return "application/yaml";
    } else if (filename.endsWith(".json")) {
      return "application/json";
    } else if (filename.endsWith(".css")) {
      return "text/css";
    } else if (filename.endsWith(".js")) {
      return "application/javascript";
    } else {
      return "text/plain";
    }
  }
}
