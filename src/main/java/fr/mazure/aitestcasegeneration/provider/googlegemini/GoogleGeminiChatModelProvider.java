package fr.mazure.aitestcasegeneration.provider.googlegemini;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel.GoogleAiGeminiChatModelBuilder;
import fr.mazure.aitestcasegeneration.provider.base.MissingEnvironmentVariable;
import fr.mazure.aitestcasegeneration.provider.base.ModelProvider;

public class GoogleGeminiChatModelProvider implements ModelProvider {

    public static ChatModel createChatModel(final GoogleGeminiModelParameters parameters) throws MissingEnvironmentVariable {

        final String apiKey = ModelProvider.getApiKeyFromEnvironmentVariable(parameters.getApiKeyEnvironmentVariableName(), "Google AI Gemini");

        final GoogleAiGeminiChatModelBuilder builder = GoogleAiGeminiChatModel.builder()
                                                                              .apiKey(apiKey)
                                                                              .modelName(parameters.getModelName());

        // Note: baseUrl is not supported in GoogleAiGeminiChatModelBuilder in the current version
        // If baseUrl support is needed, consider using a custom HTTP client or wait for future versions

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

        return builder.build();
    }
}
