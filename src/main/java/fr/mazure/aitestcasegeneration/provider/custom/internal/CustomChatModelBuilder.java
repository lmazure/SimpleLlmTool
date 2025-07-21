package fr.mazure.aitestcasegeneration.provider.custom.internal;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class CustomChatModelBuilder {

    private String apiKey;
    private String baseUrl;
    private String modelName;
    private Duration timeout = Duration.ofSeconds(60);
    private Duration connectTimeout = Duration.ofSeconds(30);
    private Map<String, Object> additionalParameters = new HashMap<>();
    private boolean logRequests = false;
    private boolean logResponses = false;

    public CustomChatModelBuilder() {
        // Default constructor
    }

    /**
     * Sets the API key for authentication
     */
    public CustomChatModelBuilder apiKey(final String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    /**
     * Sets the base URL for the API endpoint
     */
    public CustomChatModelBuilder baseUrl(final String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    /**
     * Sets the model name to use
     */
    public CustomChatModelBuilder modelName(final String modelName) {
        this.modelName = modelName;
        return this;
    }

    /**
     * Sets the timeout for API calls
     */
    public CustomChatModelBuilder timeout(final Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Sets the connection timeout
     */
    public CustomChatModelBuilder connectTimeout(final Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * Enables request logging
     */
    public CustomChatModelBuilder logRequests(final boolean logRequests) {
        this.logRequests = logRequests;
        return this;
    }

    /**
     * Enables response logging
     */
    public CustomChatModelBuilder logResponses(final boolean logResponses) {
        this.logResponses = logResponses;
        return this;
    }

    /**
     * Adds additional parameters to be sent with API requests
     */
    public CustomChatModelBuilder additionalParameter(final String key,
                                                      final Object value) {
        this.additionalParameters.put(key, value);
        return this;
    }

    /**
     * Adds multiple additional parameters
     */
    public CustomChatModelBuilder additionalParameters(final Map<String, Object> parameters) {
        this.additionalParameters.putAll(parameters);
        return this;
    }

    /**
     * Builds the CustomChatModel instance
     */
    public CustomChatModel build() {
        validateParameters();
        return new CustomChatModel(this);
    }

    /**
     * Validates required parameters before building
     */
    private void validateParameters() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API key is required");
        }
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Base URL is required");
        }
        if (modelName == null || modelName.trim().isEmpty()) {
            throw new IllegalArgumentException("Model name is required");
        }
        if (timeout != null && timeout.isNegative()) {
            throw new IllegalArgumentException("Timeout must be positive");
        }
        if (connectTimeout != null && connectTimeout.isNegative()) {
            throw new IllegalArgumentException("Connect timeout must be positive");
        }
    }

    // Getters for the CustomChatModel constructor
    String getApiKey() { return apiKey; }
    String getBaseUrl() { return baseUrl; }
    String getModelName() { return modelName; }
    Duration getTimeout() { return timeout; }
    Duration getConnectTimeout() { return connectTimeout; }
    Map<String, Object> getAdditionalParameters() { return new HashMap<>(additionalParameters); }
    boolean isLogRequests() { return logRequests; }
    boolean isLogResponses() { return logResponses; }
}