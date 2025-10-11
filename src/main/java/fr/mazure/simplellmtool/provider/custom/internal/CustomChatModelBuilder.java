package fr.mazure.simplellmtool.provider.custom.internal;


import java.util.Map;

import fr.mazure.simplellmtool.provider.custom.internal.CustomChatModel.FinishingReason;

/*
 * Represents a builder for the CustomChatModel class
 */
public class CustomChatModelBuilder {

    private String modelName;
    private String apiKey;
    private String baseUrl;
    private String payloadTemplate;
    private Map<String, String> httpHeaders;
    private String answerPath;
    private String inputTokenPath;
    private String outputTokenPath;
    private String finishReasonPath;
    private Map<String, FinishingReason> finishReasonMappings;
    private Boolean logRequests = Boolean.FALSE;
    private Boolean logResponses = Boolean.FALSE;

    public CustomChatModelBuilder() {
        // Default constructor
    }

    /**
     * Sets the model name
     */
    public CustomChatModelBuilder modelName(final String modelName) {
        this.modelName = modelName;
        return this;
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
     * Sets the JSON path to the field containing the finish reason
     */
    public CustomChatModelBuilder finishReasonPath(final String finishReasonPath) {
        this.finishReasonPath = finishReasonPath;
        return this;
    }

    /**
     * Sets the map of finish reason values to their corresponding FinishReason enum values
     */
    public CustomChatModelBuilder finishReasonMappings(final Map<String, FinishingReason> finishReasonMappings) {
        this.finishReasonMappings = finishReasonMappings;
        return this;
    }

    /**
     * Enables request logging
     */
    public CustomChatModelBuilder logRequests(final Boolean logRequests) {
        this.logRequests = logRequests;
        return this;
    }

    /**
     * Enables response logging
     */
    public CustomChatModelBuilder logResponses(final Boolean logResponses) {
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
        if (this.modelName == null || this.modelName.trim().isEmpty()) {
            throw new IllegalArgumentException("Model name is required");
        }
        if (this.apiKey == null || this.apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API key is required");
        }
        if (this.baseUrl == null || this.baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Base URL is required");
        }
        if (this.payloadTemplate == null || this.payloadTemplate.trim().isEmpty()) {
            throw new IllegalArgumentException("Payload template is required");
        }
        if (this.httpHeaders == null || this.httpHeaders.isEmpty()) {
            throw new IllegalArgumentException("HTTP headers are required");
        }
        if (this.answerPath == null || this.answerPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Answer path is required");
        }
        if (this.inputTokenPath == null || this.inputTokenPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Input token path is required");
        }
        if (this.outputTokenPath == null || this.outputTokenPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Output token path is required");
        }
        if (this.finishReasonPath == null || this.finishReasonPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Finish reason path is required");
        }
        if (this.finishReasonMappings == null || this.finishReasonMappings.isEmpty()) {
            throw new IllegalArgumentException("Finish reason mappings are required");
        }
    }

    // Getters for the CustomChatModel constructor
    String getModelName() { return this.modelName; }
    String getApiKey() { return this.apiKey; }
    String getBaseUrl() { return this.baseUrl; }
    String getPayloadTemplate() { return this.payloadTemplate; }
    String getAnswerPath() { return this.answerPath; }
    Map<String, String> getHttpHeaders() { return this.httpHeaders; }
    String getInputTokenPath() { return this.inputTokenPath; }
    String getOutputTokenPath() { return this.outputTokenPath; }
    String getFinishReasonPath() { return this.finishReasonPath; }
    Map<String, FinishingReason> getFinishReasonMappings() { return this.finishReasonMappings;}
    Boolean isLogRequests() { return this.logRequests; }
    Boolean isLogResponses() { return this.logResponses; }
}