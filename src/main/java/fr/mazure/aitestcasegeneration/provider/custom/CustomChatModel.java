package fr.mazure.aitestcasegeneration.provider.custom;

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
    private final String baseUrl;
    private final String modelName;
    private final Duration timeout;
    private final Duration connectTimeout;
    private final Integer maxRetries;
    private final Double temperature;
    private final Integer maxTokens;
    private final String organizationId;
    private final Map<String, Object> additionalParameters;
    private final boolean logRequests;
    private final boolean logResponses;

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    // Constructor that takes the builder
    public CustomChatModel(final CustomChatModelBuilder builder) {
        this.apiKey = builder.getApiKey();
        this.baseUrl = builder.getBaseUrl();
        this.modelName = builder.getModelName();
        this.timeout = builder.getTimeout();
        this.connectTimeout = builder.getConnectTimeout();
        this.maxRetries = builder.getMaxRetries();
        this.temperature = builder.getTemperature();
        this.maxTokens = builder.getMaxTokens();
        this.organizationId = builder.getOrganizationId();
        this.additionalParameters = builder.getAdditionalParameters();
        this.logRequests = builder.isLogRequests();
        this.logResponses = builder.isLogResponses();

        this.httpClient = createHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Creates a new builder instance
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
    public ChatResponse doChat(ChatRequest chatRequest) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return attemptGenerate(chatRequest);
            } catch (final Exception e) {
                lastException = e;
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(calculateBackoffDelay(attempt));
                    } catch (final InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during retry", ie);
                    }
                }
            }
        }

        throw new RuntimeException("Failed to generate response after " + maxRetries + " attempts", lastException);
    }

    private ChatResponse attemptGenerate(ChatRequest chatRequest) throws IOException {
        RequestBody requestBody = buildRequestBody(chatRequest);
        Request request = buildRequest(requestBody);

        try (okhttp3.Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("API call failed: " + response.code() + " " + response.message());
            }

            String responseBody = response.body().string();
            return parseApiResponse(responseBody);
        }
    }

    private RequestBody buildRequestBody(final ChatRequest chatRequest) throws IOException {
        final Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("model", modelName);
        requestMap.put("messages", convertMessages(chatRequest.messages()));

        // Add optional parameters
        if (temperature != null) {
            requestMap.put("temperature", temperature);
        }
        if (maxTokens != null) {
            requestMap.put("max_tokens", maxTokens);
        }

        // Add any additional parameters
        requestMap.putAll(additionalParameters);

        final String json = objectMapper.writeValueAsString(requestMap);
        return RequestBody.create(json, MediaType.get("application/json"));
    }

    private Request buildRequest(final RequestBody requestBody) {
        final Request.Builder builder = new Request.Builder()
                                                   .url(baseUrl + "/chat/completions")
                                                   .header("Authorization", "Bearer " + apiKey)
                                                   .header("Content-Type", "application/json")
                                                   .post(requestBody);

        if (organizationId != null) {
            builder.header("OpenAI-Organization", organizationId);
        }

        return builder.build();
    }

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

        TokenUsage tokenUsage = null;
        if (jsonResponse.has("usage")) {
            JsonNode usage = jsonResponse.path("usage");
            tokenUsage = new TokenUsage(
                usage.path("prompt_tokens").asInt(),
                usage.path("completion_tokens").asInt()
            );
        }

        return ChatResponse.builder()
                           .aiMessage(aiMessage)
                           .tokenUsage(tokenUsage)
                           .build();
    }

    private long calculateBackoffDelay(int attempt) {
        return Math.min(1000 * (1L << attempt), 30000); // Exponential backoff with max 30 seconds
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