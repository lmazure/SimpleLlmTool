package fr.mazure.simplellmtool.provider.googlegemini;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel.GoogleAiGeminiChatModelBuilder;
import fr.mazure.simplellmtool.provider.base.MissingEnvironmentVariable;
import fr.mazure.simplellmtool.provider.base.ModelProvider;

/**
 * The Google Gemini model provider.
 */
public class GoogleGeminiChatModelProvider implements ModelProvider {

    public static ChatModel createChatModel(final GoogleGeminiModelParameters parameters) throws MissingEnvironmentVariable {

        final String apiKey = ModelProvider.getApiKeyFromEnvironmentVariable(parameters.getApiKeyEnvironmentVariableName(), "Google Gemini");

        final GoogleAiGeminiChatModelBuilder builder = GoogleAiGeminiChatModel.builder()
                                                                              .apiKey(apiKey)
                                                                              .modelName(parameters.getModelName());

        if (parameters.getBaseUrl().isPresent()) {
            builder.baseUrl(parameters.getBaseUrl().get().toString());
        }

        if (parameters.getTemperature().isPresent()) {
            builder.temperature(parameters.getTemperature().get());
        }

        if (parameters.getTopP().isPresent()) {
            builder.topP(parameters.getTopP().get());
        }

        if (parameters.getTopK().isPresent()) {
            builder.topK(parameters.getTopK().get());
        }

        if (parameters.getMaxTokens().isPresent()) {
            builder.maxOutputTokens(parameters.getMaxTokens().get());
        }

        return builder.logRequests(Boolean.TRUE)
                      .logResponses(Boolean.TRUE)
                      .build();
    }
}
