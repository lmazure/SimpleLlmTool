package fr.mazure.simplellmtool.provider.custom.internal;

import java.io.IOException;
import java.io.PrintStream;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.TokenUsage;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;

public class CustomChatModel implements ChatModel {

    private final String modelName;
    private final String apiKey;
    private final String url;
    private static final Duration timeout = Duration.ofSeconds(60);
    private static final Duration connectTimeout = Duration.ofSeconds(30);
    private final Map<String, String> httpHeaders;
    private final String payloadTemplate;
    private final String answerPath;
    private final String inputTokenPath;
    private final String outputTokenPath;
    private final boolean logRequests;
    private final boolean logResponses;
    private final PrintStream log;

    private final OkHttpClient httpClient;

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
        this.logRequests = builder.isLogRequests();
        this.logResponses = builder.isLogResponses();
        this.log = builder.getLog();

        this.httpClient = new OkHttpClient.Builder()
                                          .connectTimeout(connectTimeout.toMillis(), TimeUnit.MILLISECONDS)
                                          .readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                                          .build();
    }

    /**
     * Create a new builder instance
     */
    public static CustomChatModelBuilder builder() {
        return new CustomChatModelBuilder();
    }

    @Override
    public ChatResponse doChat(final ChatRequest chatRequest) {
        RequestBody requestBody;
        try {
            requestBody = buildRequestBody(chatRequest);
            if (logRequests) {
                this.log.println("URL: " + url);
                this.log.println("Request: " + bodyToString(requestBody));
            }
        } catch (final IOException e) {
            throw new RuntimeException("Failed to build request body: " + e.getMessage());
        }
        final Request request = buildRequest(chatRequest, requestBody);

        try (final okhttp3.Response response = httpClient.newCall(request).execute()) {
            if (logResponses) {
                this.log.println("Response: " + response.code() + " " + response.message());
            }
            if (!response.isSuccessful()) {
                throw new RuntimeException("API call failed: " + response.code() + " " + response.message());
            }

            final String responseBody = response.body().string();
            if (logResponses) {
                this.log.println("Response body: " + responseBody);
            }
            return parseApiResponse(responseBody);
        } catch (final IOException e) {
            throw new RuntimeException("Failed to parse answer: " + e.getMessage());
        }
    }

    private RequestBody buildRequestBody(final ChatRequest chatRequest) throws IOException {
        final String json = RequestPayloadGenerator.generate(this.payloadTemplate, convertMessages(chatRequest.messages()), modelName, apiKey);
        return RequestBody.create(json, MediaType.get("application/json"));
    }

    private Request buildRequest(final ChatRequest chatRequest,
                                 final RequestBody requestBody) {
        final Request.Builder builder = new Request.Builder()
                                                   .url(url)
                                                   .header("Content-Type", "application/json")
                                                   .post(requestBody);
        for (final Map.Entry<String, String> entry : httpHeaders.entrySet()) {
            final String valueTemplate = entry.getValue();
            final String value = RequestPayloadGenerator.generate(valueTemplate, convertMessages(chatRequest.messages()), modelName, apiKey);
            if (logRequests) {
                this.log.println("Header: " + entry.getKey() + ": " + value);
            }
            builder.header(entry.getKey(), value);
        }
        return builder.build();
    }

    private List<MessageRound> convertMessages(final List<ChatMessage> messages) {
        return messages.stream()
                       .map(this::convertMessage)
                       .collect(Collectors.toList());
    }

    private MessageRound convertMessage(final ChatMessage message) {
        return switch (message) {
            case UserMessage userMessage -> new MessageRound(Role.USER, userMessage.singleText());
            case AiMessage aiMessage -> new MessageRound(Role.MODEL, aiMessage.text());
            case SystemMessage systemMessage -> new MessageRound(Role.SYSTEM, systemMessage.text());
            default -> throw new IllegalArgumentException("Unsupported message type: " + message.getClass()); // TODO: we will have to support tools
        };
    }

    private ChatResponse parseApiResponse(final String responseBody) throws IOException {
        final String generatedText = JsonPathExtractor.extract(responseBody, answerPath);

        final AiMessage aiMessage = AiMessage.from(generatedText);

        final Integer inputTokens = Integer.parseInt(JsonPathExtractor.extract(responseBody, inputTokenPath));
        final Integer outputTokens = Integer.parseInt(JsonPathExtractor.extract(responseBody, outputTokenPath));
        final TokenUsage tokenUsage = new TokenUsage(inputTokens, outputTokens);

        return ChatResponse.builder()
                           .aiMessage(aiMessage)
                           .tokenUsage(tokenUsage)
                           .build();
    }

    private String bodyToString(final RequestBody body) {
        try {
            final Buffer buffer = new Buffer();
            body.writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            // this should never happen
            throw new RuntimeException("Failed to read request body: " + e.getMessage());
        }
    }
}