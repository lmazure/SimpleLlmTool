package fr.mazure.simplellmtool.provider.anthropic;

import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.AnthropicChatModel.AnthropicChatModelBuilder;
import dev.langchain4j.model.chat.ChatModel;
import fr.mazure.simplellmtool.provider.base.MissingEnvironmentVariable;
import fr.mazure.simplellmtool.provider.base.ModelProvider;

/**
 * The Anthropic model provider.
 */
public class AnthropicChatModelProvider implements ModelProvider {

    public static ChatModel createChatModel(final AnthropicModelParameters parameters) throws MissingEnvironmentVariable {

        final String apiKey = ModelProvider.getApiKeyFromEnvironmentVariable(parameters.getApiKeyEnvironmentVariableName(), "Anthropic");

        final AnthropicChatModelBuilder builder = AnthropicChatModel.builder()
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
            builder.maxTokens(parameters.getMaxTokens().get());
        }

        return builder.logRequests(Boolean.TRUE)
                      .logResponses(Boolean.TRUE)
                      .build();
    }
}
