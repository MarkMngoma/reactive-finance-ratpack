package za.co.ratpack.finance.reactive.functions.rest.integration.openapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Collects captured HTTP interactions for OpenAPI spec generation.
 * Acts as a thread-local storage for test interactions.
 *
 * @author markmngoma
 */
public class OpenApiCapture {
    private static final ThreadLocal<List<CapturedInteraction>> interactions = 
        ThreadLocal.withInitial(ArrayList::new);
    
    private static final ThreadLocal<Boolean> enabled = ThreadLocal.withInitial(() -> false);
    
    /**
     * Enables capture for the current thread.
     */
    public static void enable() {
        enabled.set(true);
        interactions.get().clear();
    }
    
    /**
     * Disables capture for the current thread.
     */
    public static void disable() {
        enabled.set(false);
    }
    
    /**
     * Checks if capture is enabled for the current thread.
     */
    public static boolean isEnabled() {
        return enabled.get();
    }
    
    /**
     * Captures an HTTP interaction.
     */
    public static void capture(CapturedInteraction interaction) {
        if (isEnabled()) {
            interactions.get().add(interaction);
        }
    }
    
    /**
     * Gets all captured interactions for the current thread.
     */
    public static List<CapturedInteraction> getInteractions() {
        return Collections.unmodifiableList(interactions.get());
    }
    
    /**
     * Clears all captured interactions for the current thread.
     */
    public static void clear() {
        interactions.get().clear();
    }
    
    /**
     * Clears thread-local state (should be called in test cleanup).
     */
    public static void cleanup() {
        interactions.remove();
        enabled.remove();
    }
}
