package fr.mazure.simplellmtool.provider.custom.internal;


import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
    private String toolCallsPath;
    private String toolNamePath;
    private Optional<String> toolArgumentsDictPath = Optional.empty();
    private Optional<String> toolArgumentsStringPath = Optional.empty();
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
        if (!JsonPathExtractor.isPathValid(answerPath)) {
             throw new IllegalArgumentException("Invalid answer path: " + answerPath);
        }
        this.answerPath = answerPath;
        return this;
    }

    /**
     * Sets the JSON path to the field containing the number of input tokens
     */
    public CustomChatModelBuilder inputTokenPath(final String inputTokenPath) {
        if (!JsonPathExtractor.isPathValid(inputTokenPath)) {
             throw new IllegalArgumentException("Invalid input token path: " + inputTokenPath);
        }
        this.inputTokenPath = inputTokenPath;
        return this;
    }

    /**
     * Sets the JSON path to the field containing the number of output tokens
     */
    public CustomChatModelBuilder outputTokenPath(final String outputTokenPath) {
        if (!JsonPathExtractor.isPathValid(outputTokenPath)) {
             throw new IllegalArgumentException("Invalid output token path: " + outputTokenPath);
        }
        this.outputTokenPath = outputTokenPath;
        return this;
    }

    /**
     * Sets the JSON path to the field containing the finish reason
     */
    public CustomChatModelBuilder finishReasonPath(final String finishReasonPath) {
        if (!JsonPathExtractor.isPathValid(finishReasonPath)) {
             throw new IllegalArgumentException("Invalid finish reason path: " + finishReasonPath);
        }
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
     * Sets the JSON path to the array of tool calls
     */
    public CustomChatModelBuilder toolCallsPath(final String toolCallsPath) {
        if (!JsonPathExtractor.isPathValid(toolCallsPath)) {
             throw new IllegalArgumentException("Invalid tool calls path: " + toolCallsPath);
        }
        this.toolCallsPath = toolCallsPath;
        return this;
    }

    /**
     * Sets the JSON path to the tool name within a tool call element
     */
    public CustomChatModelBuilder toolNamePath(final String toolNamePath) {
        if (!JsonPathExtractor.isPathValid(toolNamePath)) {
             throw new IllegalArgumentException("Invalid tool name path: " + toolNamePath);
        }
        this.toolNamePath = toolNamePath;
        return this;
    }

    /**
     * Sets the JSON path to the tool arguments dictionary within a tool call element
     */
    public CustomChatModelBuilder toolArgumentsDictPath(final Optional<String> toolArgumentsDictPath) {
        if (toolArgumentsDictPath.isPresent() && !JsonPathExtractor.isPathValid(toolArgumentsDictPath.get())) {
             throw new IllegalArgumentException("Invalid tool arguments path: " + toolArgumentsDictPath.get());
        }
        this.toolArgumentsDictPath = toolArgumentsDictPath;
        return this;
    }

    /**
     * Sets the JSON path to the tool arguments string within a tool call element
     */
    public CustomChatModelBuilder toolArgumentsStringPath(final Optional<String> toolArgumentsStringPath) {
        if (toolArgumentsStringPath.isPresent() && !JsonPathExtractor.isPathValid(toolArgumentsStringPath.get())) {
             throw new IllegalArgumentException("Invalid tool arguments path: " + toolArgumentsStringPath.get());
        }
        this.toolArgumentsStringPath = toolArgumentsStringPath;
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
        if (Objects.isNull(this.modelName) || this.modelName.trim().isEmpty()) {
            throw new IllegalArgumentException("Model name is required");
        }
        if (Objects.isNull(this.apiKey) || this.apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API key is required");
        }
        if (Objects.isNull(this.baseUrl) || this.baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Base URL is required");
        }
        if (Objects.isNull(this.payloadTemplate) || this.payloadTemplate.trim().isEmpty()) {
            throw new IllegalArgumentException("Payload template is required");
        }
        if (Objects.isNull(this.httpHeaders)) {
            throw new IllegalArgumentException("HTTP headers are required");
        }
        if (Objects.isNull(this.answerPath) || this.answerPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Answer path is required");
        }
        if (Objects.isNull(this.inputTokenPath) || this.inputTokenPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Input token path is required");
        }
        if (Objects.isNull(this.outputTokenPath) || this.outputTokenPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Output token path is required");
        }
        if (Objects.isNull(this.finishReasonPath) || this.finishReasonPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Finish reason path is required");
        }
        if (Objects.isNull(this.finishReasonMappings) || this.finishReasonMappings.isEmpty()) {
            throw new IllegalArgumentException("Finish reason mappings are required");
        }
        if (Objects.isNull(this.toolCallsPath) || this.toolCallsPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Tool calls path is required");
        }
        if (Objects.isNull(this.toolNamePath) || this.toolNamePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Tool name path is required");
        }
        final boolean toolArgumentsDictPathIsSet = this.toolArgumentsDictPath.isPresent() && !this.toolArgumentsDictPath.get().trim().isEmpty();
        final boolean toolArgumentsStringPathIsSet = this.toolArgumentsStringPath.isPresent() && !this.toolArgumentsStringPath.get().trim().isEmpty();
        if (!(toolArgumentsDictPathIsSet || toolArgumentsStringPathIsSet)) {
            throw new IllegalArgumentException("Either tool arguments dictonary path or tool arguments dictonary string is required");
        }
        if (toolArgumentsDictPathIsSet && toolArgumentsStringPathIsSet) {
            throw new IllegalArgumentException("Tool arguments dictonary path and tool arguments dictonary string cannot both be set");
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
    String getToolCallsPath() { return this.toolCallsPath; }
    String getToolNamePath() { return this.toolNamePath; }
    Optional<String> getToolArgumentsDictPath() { return this.toolArgumentsDictPath; }
    Optional<String> getToolArgumentsStringPath() { return this.toolArgumentsStringPath; }
    Boolean isLogRequests() { return this.logRequests; }
    Boolean isLogResponses() { return this.logResponses; }
}