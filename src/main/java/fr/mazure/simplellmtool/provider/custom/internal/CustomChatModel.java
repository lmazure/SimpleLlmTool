package fr.mazure.simplellmtool.provider.custom.internal;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import dev.langchain4j.http.client.log.LoggingHttpClient;
import dev.langchain4j.http.client.jdk.JdkHttpClientBuilder;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.TokenUsage;

public class CustomChatModel implements ChatModel {

    public enum FinishingReason {
        DONE(FinishReason.STOP),
        MAX_TOKENS(FinishReason.LENGTH);
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
        if (jsonError != null) {
            throw new IllegalArgumentException("The generated payload is invalid JSON: " + jsonError + "\n" + addLineNumbers(requestBody));
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
        System.out.println("messages=" + chatRequest.messages());
        System.out.println("converted messages=" + convertMessages(chatRequest.messages()));
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
        for (final Map.Entry<String, String> entry : this.httpHeaders.entrySet()) {
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
            case UserMessage userMessage -> new MessageRound(Role.USER, userMessage.singleText(), List.of());
            case AiMessage aiMessage -> buildModelMessageRound(aiMessage);
            case SystemMessage systemMessage -> new MessageRound(Role.SYSTEM, systemMessage.text(), List.of());
            case ToolExecutionResultMessage toolExecutionResultMessage -> new MessageRound(Role.TOOL, toolExecutionResultMessage.text(), List.of());
            default -> throw new IllegalArgumentException("Unsupported message type: " + message.getClass()); // TODO: we will have to support tools
        };
    }

    private static MessageRound buildModelMessageRound(AiMessage aiMessage) {
        final String text = aiMessage.text();
        final List<MessageRoundToolCall> toolCalls = new ArrayList<>();
        
        ObjectMapper objectMapper = new ObjectMapper();
        
        for (ToolExecutionRequest request : aiMessage.toolExecutionRequests()) {
            final List<MessageRoundToolPamameter> toolParameters = new ArrayList<>();
            
            try {
                // Parse the arguments JSON string into a Map
                String argumentsJson = request.arguments();
                if (argumentsJson != null && !argumentsJson.isEmpty()) {
                    Map<String, Object> argumentsMap = objectMapper.readValue(
                        argumentsJson, 
                        new TypeReference<Map<String, Object>>() {}
                    );
                    
                    // Convert each entry to a MessageRoundToolPamameter
                    for (Map.Entry<String, Object> entry : argumentsMap.entrySet()) {
                        String paramValue = entry.getValue() != null 
                            ? entry.getValue().toString() 
                            : null;
                        toolParameters.add(
                            new MessageRoundToolPamameter(entry.getKey(), paramValue)
                        );
                    }
                }
            } catch (JsonProcessingException e) {
                // Handle parsing error - you might want to log this
                // or rethrow as a runtime exception depending on your needs
                throw new RuntimeException("Failed to parse tool arguments", e);
            }
            
            toolCalls.add(new MessageRoundToolCall(request.name(), toolParameters));
        }
        
        return new MessageRound(Role.MODEL, text, toolCalls);
    }

private ChatResponse parseApiResponse(final String responseBody) throws IOException, JsonPathExtractorException {
    // Parse the JSON response
    ObjectMapper mapper = new ObjectMapper();
    JsonNode root = mapper.readTree(responseBody);
    JsonNode candidate = root.path("candidates").get(0);
    JsonNode content = candidate.path("content");
    JsonNode parts = content.path("parts");
    
    AiMessage aiMessage;
    
    // Check if this is a function call response
    if (parts.has(0) && parts.get(0).has("functionCall")) {
        // Handle function call(s)
        List<ToolExecutionRequest> toolExecutionRequests = new ArrayList<>();
        
        for (JsonNode part : parts) {
            if (part.has("functionCall")) {
                JsonNode functionCall = part.get("functionCall");
                String functionName = functionCall.get("name").asText();
                JsonNode args = functionCall.get("args");
                
                // Convert args to a JSON string
                String arguments = args.toString();
                
                // Create a unique ID for this tool execution request
                String id = UUID.randomUUID().toString();
                
                ToolExecutionRequest toolRequest = ToolExecutionRequest.builder()
                    .id(id)
                    .name(functionName)
                    .arguments(arguments)
                    .build();
                
                toolExecutionRequests.add(toolRequest);
            }
        }
        
        // Create AiMessage with tool execution requests
        aiMessage = AiMessage.from(toolExecutionRequests);
        
    } else {
        // Handle regular text response
        final String generatedText = JsonPathExtractor.extract(responseBody, this.answerPath);
        aiMessage = AiMessage.from(generatedText);
    }
    
    // Extract token usage
    final Integer inputTokens = Integer.valueOf(JsonPathExtractor.extract(responseBody, this.inputTokenPath));
    final Integer outputTokens = Integer.valueOf(JsonPathExtractor.extract(responseBody, this.outputTokenPath));
    final TokenUsage tokenUsage = new TokenUsage(inputTokens, outputTokens);
    
    // Extract finish reason
    final String finishReason = JsonPathExtractor.extract(responseBody, this.finishReasonPath);
    final FinishingReason reason = this.finishReasonMappings.get(finishReason);
    if (reason == null) {
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

    private static String addLineNumbers(final String input) {
        if (input == null) {
            return null;
        }
        
        final String[] lines = input.split("\n", -1);
        final StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < lines.length; i++) {
            result.append(String.format("%03d", Integer.valueOf(i + 1)))
                  .append(" ")
                  .append(lines[i]);
            
            // Add newline except after the last line (if input didn't end with newline)
            if (i < lines.length - 1) {
                result.append("\n");
            }
        }
        
        return result.toString();
    }
}