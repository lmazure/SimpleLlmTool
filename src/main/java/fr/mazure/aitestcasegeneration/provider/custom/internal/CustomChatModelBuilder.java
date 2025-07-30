package fr.mazure.aitestcasegeneration.provider.custom.internal;

import java.io.PrintStream;
import java.util.Map;

public class CustomChatModelBuilder {

    private String apiKey;
    private String baseUrl;
    private String payloadTemplate;
    private Map<String, String> httpHeaders;
    private String answerPath;
    private String inputTokenPath;
    private String outputTokenPath;
    private boolean logRequests = false;
    private boolean logResponses = false;
    private PrintStream log;

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
     * Sets the payload template for the API calls
     */
    public CustomChatModelBuilder payloadTemplate(final String payloadTemplate) {
        this.payloadTemplate = payloadTemplate;
        return this;
    }

    public CustomChatModelBuilder httpHeaders(final Map<String, String> httpHeaders) {
        this.httpHeaders = httpHeaders;
        return this;
    }
    /**
     * Sets the JSON path to the field containing the answer
     */
    public CustomChatModelBuilder answerPath(final String answerPath) {
        this.answerPath = answerPath;
        return this;
    }

    /**
     * Sets the JSON path to the field containing the number of input tokens
     */
    public CustomChatModelBuilder inputTokenPath(final String inputTokenPath) {
        this.inputTokenPath = inputTokenPath;
        return this;
    }

    /**
     * Sets the JSON path to the field containing the number of output tokens
     */
    public CustomChatModelBuilder outputTokenPath(final String outputTokenPath) {
        this.outputTokenPath = outputTokenPath;
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
                                                      final String value) {
        this.httpHeaders.put(key, value);
        return this;
    }

    /**
     * Adds multiple additional parameters
     */
    public CustomChatModelBuilder additionalParameters(final Map<String, String> parameters) {
        this.httpHeaders.putAll(parameters);
        return this;
    }

    /**
     * Sets the log stream
     */
    public CustomChatModelBuilder log(final PrintStream log) {
        this.log = log;
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
        if (payloadTemplate == null || payloadTemplate.trim().isEmpty()) {
            throw new IllegalArgumentException("Payload template is required");
        }
        if (httpHeaders == null || httpHeaders.isEmpty()) {
            throw new IllegalArgumentException("HTTP headers are required");
        }
        if (answerPath == null || answerPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Answer path is required");
        }
        if (inputTokenPath == null || inputTokenPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Input token path is required");
        }
        if (outputTokenPath == null || outputTokenPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Output token path is required");
        }
        if (log == null) {
            throw new IllegalArgumentException("Log stream is required");
        }
    }

    // Getters for the CustomChatModel constructor
    String getApiKey() { return apiKey; }
    String getBaseUrl() { return baseUrl; }
    String getPayloadTemplate() { return payloadTemplate; }
    String getAnswerPath() { return answerPath; }
    Map<String, String> getHttpHeaders() { return httpHeaders; }
    String getInputTokenPath() { return inputTokenPath; }
    String getOutputTokenPath() { return outputTokenPath; }
    boolean isLogRequests() { return logRequests; }
    boolean isLogResponses() { return logResponses; }
    PrintStream getLog() { return log; }
}