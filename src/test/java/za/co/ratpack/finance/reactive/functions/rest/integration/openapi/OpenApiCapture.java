package za.co.ratpack.finance.reactive.functions.rest.integration.openapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe singleton that collects CapturedInteraction instances.
 * 
 * @author Generated
 * @created at 14:13 on 11/02/2026
 */
public class OpenApiCapture {
  
  private static final Logger LOG = LoggerFactory.getLogger(OpenApiCapture.class);
  private static final OpenApiCapture INSTANCE = new OpenApiCapture();
  
  private final List<CapturedInteraction> interactions;
  
  private OpenApiCapture() {
    this.interactions = new CopyOnWriteArrayList<>();
  }
  
  public static OpenApiCapture getInstance() {
    return INSTANCE;
  }
  
  /**
   * Records a captured interaction.
   */
  public void record(CapturedInteraction interaction) {
    try {
      interactions.add(interaction);
      LOG.debug("Recorded interaction: {} {} -> {}", 
        interaction.getMethod(), 
        interaction.getNormalizedPath(), 
        interaction.getResponseStatusCode());
    } catch (Exception e) {
      LOG.warn("Failed to record interaction", e);
    }
  }
  
  /**
   * Gets all captured interactions.
   */
  public List<CapturedInteraction> getInteractions() {
    return new ArrayList<>(interactions);
  }
  
  /**
   * Clears all captured interactions.
   */
  public void clear() {
    interactions.clear();
    LOG.debug("Cleared all captured interactions");
  }
}
