package fr.mazure.simplellmtool.provider.custom.internal;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.exception.HttpException;
import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.HttpMethod;
import dev.langchain4j.http.client.HttpRequest;
import dev.langchain4j.http.client.HttpRequest.Builder;
import dev.langchain4j.http.client.SuccessfulHttpResponse;
import dev.langchain4j.http.client.jdk.JdkHttpClientBuilder;
import dev.langchain4j.http.client.log.LoggingHttpClient;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.TokenUsage;
import fr.mazure.simplellmtool.tools.ToolParameterValue;

/**
 * CustomChatModel represents a fully customizable chat model that can be used in the LangChain4j framework
 */
public class CustomChatModel implements ChatModel {

    /**
     * Represents the reason why the model stopped text generation
     */
    public enum FinishingReason {
        DONE(FinishReason.STOP),
        MAX_TOKENS(FinishReason.LENGTH),
        TOOL_CALL(FinishReason.TOOL_EXECUTION);
        public final FinishReason reason;
        private FinishingReason(final FinishReason reason) {
            this.reason = reason;
        }
    }

    private final String modelName;
    private final String apiKey;
    private final String url;
    private static final Duration readTimeout = Duration.ofSeconds(60);
    private static final Duration connectTimeout = Duration.ofSeconds(30);
    private final Map<String, String> httpHeaders;
    private final String payloadTemplate;
    private final String answerPath;
    private final String inputTokenPath;
    private final String outputTokenPath;
    private final String finishReasonPath;
    private final Map<String, FinishingReason> finishReasonMappings;
    private final String toolCallsPath;
    private final String toolNamePath;
    private final Optional<String> toolCallIdPath;
    private final Optional<String> toolArgumentsDictPath;
    private final Optional<String> toolArgumentsStringPath;
    private final Boolean logRequests;
    private final Boolean logResponses;

    private final HttpClient httpClient;

    // Constructor that takes the builder
    public CustomChatModel(final CustomChatModelBuilder builder) {
        this.modelName = builder.getModelName();
        this.apiKey = builder.getApiKey();
        this.url = builder.getBaseUrl();
        this.payloadTemplate = builder.getPayloadTemplate();
        this.httpHeaders = builder.getHttpHeaders();
        this.answerPath = builder.getAnswerPath();
        this.inputTokenPath = builder.getInputTokenPath();
        this.outputTokenPath = builder.getOutputTokenPath();
        this.finishReasonPath = builder.getFinishReasonPath();
        this.finishReasonMappings = builder.getFinishReasonMappings();
        this.toolCallsPath = builder.getToolCallsPath();
        this.toolNamePath = builder.getToolNamePath();
        this.toolCallIdPath = builder.getToolCallIdPath();
        this.toolArgumentsDictPath = builder.getToolArgumentsDictPath();
        this.toolArgumentsStringPath = builder.getToolArgumentsStringPath();
        this.logRequests = builder.isLogRequests();
        this.logResponses = builder.isLogResponses();

        this.httpClient = buildHttpClient();
    }

    private HttpClient buildHttpClient() {
        final HttpClientBuilder httpClientBuilder = new JdkHttpClientBuilder();
        final HttpClient client = httpClientBuilder.connectTimeout(connectTimeout).readTimeout(readTimeout).build();

        if (this.logRequests.booleanValue() || this.logResponses.booleanValue()) {
            return new LoggingHttpClient(client, this.logRequests, this.logResponses);
        }
        return client;
    }

    /**
     * Create a new builder instance
     */
    public static CustomChatModelBuilder builder() {
        return new CustomChatModelBuilder();
    }

    @Override
    public ChatResponse doChat(final ChatRequest chatRequest) {
        final List<ToolSpecification> toolSpecifications = chatRequest.toolSpecifications();
        final String requestBody = buildRequestBody(chatRequest, toolSpecifications);
        final String jsonError = isValidJson(requestBody);
        if (Objects.nonNull(jsonError)) {
            throw new IllegalArgumentException("The generated payload is invalid JSON: " + jsonError + "\n" + StringUtils.addLineNumbers(requestBody));
        }
        final HttpRequest request = buildRequest(chatRequest, requestBody, toolSpecifications);

        try {
            final SuccessfulHttpResponse response = this.httpClient.execute(request);
            return parseApiResponse(response.body());
        } catch (final HttpException e) {
            throw new RuntimeException("API call failed: " + e.statusCode() + " " + e.getMessage());
        } catch (final IOException e) {
            throw new RuntimeException("Failed to parse answer: " + e.getMessage());
        } catch (final JsonPathExtractorException e) {
            throw new RuntimeException("Failed to extract generated text from answer: " + e.getMessage());
        }
    }

    private String buildRequestBody(final ChatRequest chatRequest,
                                    final List<ToolSpecification> toolSpecifications) {
        return RequestPayloadGenerator.generate(this.payloadTemplate,
                                                convertMessages(chatRequest.messages()),
                                                this.modelName,
                                                toolSpecifications,
                                                this.apiKey);
    }

    private HttpRequest buildRequest(final ChatRequest chatRequest,
                                     final String requestBody,
                                     final List<ToolSpecification> toolSpecifications) {
        final Builder httpRequestBuilder = HttpRequest.builder()
                                                      .method(HttpMethod.POST)
                                                      .url(this.url)
                                                      .addHeader("Content-Type", "application/json")
                                                      .body(requestBody);
        for (final Map.Entry<String, String> entry: this.httpHeaders.entrySet()) {
            final String valueTemplate = entry.getValue();
            final String value = RequestPayloadGenerator.generate(valueTemplate,
                                                                  convertMessages(chatRequest.messages()),
                                                                  this.modelName,
                                                                  toolSpecifications,
                                                                  this.apiKey);
            httpRequestBuilder.addHeader(entry.getKey(), value);
        }
        return httpRequestBuilder.build();
    }

    private List<MessageRound> convertMessages(final List<ChatMessage> messages) {
        return messages.stream()
                       .map(this::convertMessage)
                       .toList();
    }

    private MessageRound convertMessage(final ChatMessage message) {
        return switch (message) {
            case UserMessage userMessage -> new MessageRound(MessageRound.Role.USER,
                                                             userMessage.singleText());
            case AiMessage aiMessage -> buildModelMessageRound(aiMessage);
            case SystemMessage systemMessage -> new MessageRound(MessageRound.Role.SYSTEM,
                                                                 systemMessage.text());
            case ToolExecutionResultMessage toolExecutionResultMessage -> new MessageRound(MessageRound.Role.TOOL,
                                                                                           toolExecutionResultMessage.text(),
                                                                                           toolExecutionResultMessage.toolName(),
                                                                                           toolExecutionResultMessage.id());
            default -> throw new IllegalArgumentException("Unsupported message type: " + message.getClass());
        };
    }

    private static MessageRound buildModelMessageRound(AiMessage aiMessage) {
        final String text = aiMessage.text();
        final List<MessageRound.ToolCall> toolCalls = new ArrayList<>();

        final ObjectMapper objectMapper = new ObjectMapper();

        for (final ToolExecutionRequest request: aiMessage.toolExecutionRequests()) {
            final List<MessageRound.ToolParameter> toolParameters = new ArrayList<>();

            try {
                // Parse the arguments JSON string into a Map
                final String argumentsJson = request.arguments();
                if (Objects.nonNull(argumentsJson) && !argumentsJson.isEmpty()) {
                    final Map<String, Object> argumentsMap = objectMapper.readValue(
                        argumentsJson,
                        new TypeReference<Map<String, Object>>() { /* empty */}
                    );

                    // Convert each entry to a MessageRoundToolPamameter
                    for (Map.Entry<String, Object> entry: argumentsMap.entrySet()) {
                        final ToolParameterValue paramValue = (entry.getValue() != null) ? ToolParameterValue.convert(entry.getValue())
                                                                                         : null;
                        toolParameters.add(new MessageRound.ToolParameter(entry.getKey(), paramValue));
                    }
                }
            } catch (final JsonProcessingException e) {
                // Handle parsing error - you might want to log this
                // or rethrow as a runtime exception depending on your needs
                throw new RuntimeException("Failed to parse tool arguments", e);
            }

            toolCalls.add(new MessageRound.ToolCall(request.name(), request.id(), toolParameters));
        }

        return new MessageRound(MessageRound.Role.MODEL, text, toolCalls);
    }

    public ChatResponse parseApiResponse(final String responseBody) throws IOException,
                                                                           JsonPathExtractorException {
        AiMessage aiMessage;

        final ObjectMapper objectMapper = new ObjectMapper();
        final String jsonError = isValidJson(responseBody);
        if (Objects.nonNull(jsonError)) {
            throw new IllegalArgumentException("The received payload is invalid JSON: " + jsonError + "\n" + StringUtils.addLineNumbers(responseBody));
        }
        final JsonNode rootNode = objectMapper.readTree(responseBody);

        // Try to extract tool calls using the configured path
        try {
            final List<JsonNode> toolCallNodes = JsonPathExtractor.extractArray(rootNode, this.toolCallsPath);

            if (!toolCallNodes.isEmpty()) {
                // Handle function call(s)
                final List<ToolExecutionRequest> toolExecutionRequests = new ArrayList<>();

                for (final JsonNode toolCallNode: toolCallNodes) {
                    final String functionName = JsonPathExtractor.extractString(toolCallNode, this.toolNamePath);
                    final JsonNode argsNode;
                    if (this.toolArgumentsDictPath.isPresent()) {
                        argsNode = JsonPathExtractor.extractNode(toolCallNode, this.toolArgumentsDictPath.get());
                    } else {
                        final String argsString = JsonPathExtractor.extractString(toolCallNode, this.toolArgumentsStringPath.get());
                        argsNode = objectMapper.readTree(argsString);
                    }

                    // Convert args to a JSON string
                    final String arguments = argsNode.toString();

                    // get the call ID if there is one
                    // otherwise create a unique ID for this tool execution request
                    final String id = this.toolCallIdPath.isPresent() ? JsonPathExtractor.extractString(toolCallNode, this.toolCallIdPath.get())
                                                                      : UUID.randomUUID().toString();
                    final ToolExecutionRequest toolRequest = ToolExecutionRequest.builder()
                                                                                 .id(id)
                                                                                 .name(functionName)
                                                                                 .arguments(arguments)
                                                                                 .build();

                    toolExecutionRequests.add(toolRequest);
                }

                // Create AiMessage with tool execution requests
                aiMessage = AiMessage.from(toolExecutionRequests);
            } else {
                // Handle regular text response (empty tool calls array)
                final String generatedText = JsonPathExtractor.extractString(rootNode, this.answerPath);
                aiMessage = AiMessage.from(generatedText);
            }
        } catch (final JsonPathExtractorException _) {
            // If tool calls path doesn't exist or is invalid, fall back to regular text response
            final String generatedText = JsonPathExtractor.extractString(rootNode, this.answerPath);
            aiMessage = AiMessage.from(generatedText);
        }

        // Extract token usage
        final Integer inputTokens = Integer.valueOf(JsonPathExtractor.extractString(rootNode, this.inputTokenPath));
        final Integer outputTokens = Integer.valueOf(JsonPathExtractor.extractString(rootNode, this.outputTokenPath));
        final TokenUsage tokenUsage = new TokenUsage(inputTokens, outputTokens);

        // Extract finish reason
        final String finishReason = JsonPathExtractor.extractString(rootNode, this.finishReasonPath);
        final FinishingReason reason = this.finishReasonMappings.get(finishReason);
        if (Objects.isNull(reason)) {
            throw new IllegalArgumentException("Unexpected finish reason: " + finishReason + " (it should be present in the finishReasonMappings property)");
        }
        final FinishReason finishReasonEnum = reason.reason;

        return ChatResponse.builder()
                           .aiMessage(aiMessage)
                           .tokenUsage(tokenUsage)
                           .finishReason(finishReasonEnum)
                           .build();
    }

    /**
     * Checks if the provided JSON string is valid.
     *
     * @param json the JSON string to validate
     * @return null if the JSON is valid, or an error message if it is invalid
     */
    private static String isValidJson(final String json) {
        try {
            new ObjectMapper().readTree(json);
            return null;
        } catch (final JsonProcessingException e) {
            return e.getMessage();
        }
    }
}