package fr.mazure.aitestcasegeneration.provider.custom.internal;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.TokenUsage;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;

public class CustomChatModel implements ChatModel {

    private final String apiKey;
    private final String url;
    private final String modelName;
    private final Duration timeout;
    private final Duration connectTimeout;
    private final Map<String, Object> additionalParameters;
    private final boolean logRequests;
    private final boolean logResponses;

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    // Constructor that takes the builder
    public CustomChatModel(final CustomChatModelBuilder builder) {
        this.apiKey = builder.getApiKey();
        this.url = builder.getBaseUrl();
        this.modelName = builder.getModelName();
        this.timeout = builder.getTimeout();
        this.connectTimeout = builder.getConnectTimeout();
        this.additionalParameters = builder.getAdditionalParameters();
        this.logRequests = builder.isLogRequests();
        this.logResponses = builder.isLogResponses();

        this.httpClient = createHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Create a new builder instance
     */
    public static CustomChatModelBuilder builder() {
        return new CustomChatModelBuilder();
    }

    private OkHttpClient createHttpClient() {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder()
                                                             .connectTimeout(connectTimeout.toMillis(), TimeUnit.MILLISECONDS)
                                                             .readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);

        // Add interceptors for logging if enabled
        if (logRequests || logResponses) {
            builder.addInterceptor(new LoggingInterceptor());
        }

        return builder.build();
    }

    @Override
    public ChatResponse doChat(final ChatRequest chatRequest) {
        RequestBody requestBody;
        try {
            requestBody = buildRequestBody(chatRequest);
        } catch (final IOException e) {
            throw new RuntimeException("Failed to build request body: " + e.getMessage());
        }
        final Request request = buildRequest(requestBody);

        try (final okhttp3.Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("API call failed: " + response.code() + " " + response.message());
            }

            final String responseBody = response.body().string();
            return parseApiResponse(responseBody);
        } catch (final IOException e) {
            throw new RuntimeException("Failed to parse answer: " + e.getMessage());
        }
    }

    private RequestBody buildRequestBody(final ChatRequest chatRequest) throws IOException {
        final Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("model", modelName);
        requestMap.put("messages", convertMessages(chatRequest.messages()));

        // Add any additional parameters
        requestMap.putAll(additionalParameters);

        final String json = objectMapper.writeValueAsString(requestMap);
        return RequestBody.create(json, MediaType.get("application/json"));
    }

    private Request buildRequest(final RequestBody requestBody) {
        final Request.Builder builder = new Request.Builder()
                                                   .url(url)
                                                   .header("Authorization", "Bearer " + apiKey)
                                                   .header("Content-Type", "application/json")
                                                   .post(requestBody);

        return builder.build();
    }

    /*
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
            default -> throw new IllegalArgumentException("Unsupported message type: " + message.getClass()); // TODO: we will have to support tools and 
        };
    }
    */

    private List<Map<String, String>> convertMessages(final List<ChatMessage> messages) {
        return messages.stream()
            .map(this::convertMessage)
            .collect(Collectors.toList());
    }

    private Map<String, String> convertMessage(final ChatMessage message) {
        final Map<String, String> messageMap = new HashMap<>();

        if (message instanceof UserMessage) {
            messageMap.put("role", "user");
            messageMap.put("content", ((UserMessage) message).singleText());
        } else if (message instanceof AiMessage) {
            messageMap.put("role", "assistant");
            messageMap.put("content", ((AiMessage) message).text());
        } else if (message instanceof SystemMessage) {
            messageMap.put("role", "system");
            messageMap.put("content", ((SystemMessage) message).text());
        }

        return messageMap;
    }

    private ChatResponse parseApiResponse(final String responseBody) throws IOException {
        final JsonNode jsonResponse = objectMapper.readTree(responseBody);

        final String generatedText = jsonResponse.path("choices")
                                                 .path(0)
                                                 .path("message")
                                                 .path("content")
                                                 .asText();

        final AiMessage aiMessage = AiMessage.from(generatedText);

        /* TODO this is specific to OpenAI, I'll have to define two JsonPathes to retrive these two values
        TokenUsage tokenUsage = null;
        if (jsonResponse.has("usage")) {
            JsonNode usage = jsonResponse.path("usage");
            tokenUsage = new TokenUsage(
                usage.path("prompt_tokens").asInt(),
                usage.path("completion_tokens").asInt()
            );
        }
        */

        return ChatResponse.builder()
                           .aiMessage(aiMessage)
                           //.tokenUsage(tokenUsage) → see previous TODO
                           .build();
    }

    // Simple logging interceptor
    private class LoggingInterceptor implements Interceptor {
        @Override
        public okhttp3.Response intercept(final Chain chain) throws IOException {
            final Request request = chain.request();

            if (logRequests) {
                System.out.println("Request: " + request.method() + " " + request.url());
                if (request.body() != null) {
                    System.out.println("Request Body: " + bodyToString(request.body()));
                }
            }

            okhttp3.Response response = chain.proceed(request);

            if (logResponses) {
                System.out.println("Response: " + response.code() + " " + response.message());
            }

            return response;
        }

        private String bodyToString(final RequestBody body) {
            try {
                final Buffer buffer = new Buffer();
                body.writeTo(buffer);
                return buffer.readUtf8();
            } catch (IOException e) {
                return "Error reading body: " + e.getMessage();
            }
        }
    }
}