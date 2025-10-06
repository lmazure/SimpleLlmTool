package fr.mazure.simplellmtool.provider.custom.internal;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
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
        final String requestBody = buildRequestBody(chatRequest);
        final HttpRequest request = buildRequest(chatRequest, requestBody);

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

    private String buildRequestBody(final ChatRequest chatRequest) {
        return RequestPayloadGenerator.generate(this.payloadTemplate,
                                                convertMessages(chatRequest.messages()),
                                                this.modelName,
                                                this.apiKey);
    }

    private HttpRequest buildRequest(final ChatRequest chatRequest,
                                     final String requestBody) {
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
            case UserMessage userMessage -> new MessageRound(Role.USER, userMessage.singleText());
            case AiMessage aiMessage -> new MessageRound(Role.MODEL, aiMessage.text());
            case SystemMessage systemMessage -> new MessageRound(Role.SYSTEM, systemMessage.text());
            default -> throw new IllegalArgumentException("Unsupported message type: " + message.getClass()); // TODO: we will have to support tools
        };
    }

    private ChatResponse parseApiResponse(final String responseBody) throws IOException, JsonPathExtractorException {
        final String generatedText = JsonPathExtractor.extract(responseBody, this.answerPath);

        final AiMessage aiMessage = AiMessage.from(generatedText);

        final Integer inputTokens = Integer.valueOf(JsonPathExtractor.extract(responseBody, this.inputTokenPath));
        final Integer outputTokens = Integer.valueOf(JsonPathExtractor.extract(responseBody, this.outputTokenPath));
        final TokenUsage tokenUsage = new TokenUsage(inputTokens, outputTokens);

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
}