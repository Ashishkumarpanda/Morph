package dev.morph.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Morph.
 */
@ConfigurationProperties(prefix = "morph")
public class MorphProperties {

    /**
     * Enables debug logging for mapping operations.
     */
    private boolean debug;

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
