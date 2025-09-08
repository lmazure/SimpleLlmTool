package fr.mazure.simplellmtool.provider.mistralai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.model.mistralai.MistralAiChatModel.MistralAiChatModelBuilder;
import fr.mazure.simplellmtool.provider.base.MissingEnvironmentVariable;
import fr.mazure.simplellmtool.provider.base.ModelProvider;

public class MistralAiChatModelProvider implements ModelProvider {

    public static ChatModel createChatModel(final MistralAiModelParameters parameters) throws MissingEnvironmentVariable {

        final String apiKey = ModelProvider.getApiKeyFromEnvironmentVariable(parameters.getApiKeyEnvironmentVariableName(), "Mistral AI");

        final MistralAiChatModelBuilder builder = MistralAiChatModel.builder()
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

        if (parameters.getMaxTokens().isPresent()) {
            builder.maxTokens(parameters.getMaxTokens().get());
        }

        return builder.logRequests(true)
                      .logResponses(true)
                      .build();    }
}
