package fr.mazure.simplellmtool.provider.openai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel.OpenAiChatModelBuilder;
import fr.mazure.simplellmtool.provider.base.MissingEnvironmentVariable;
import fr.mazure.simplellmtool.provider.base.ModelProvider;

/**
 * The OpenAI model provider.
 */
public class OpenAiChatModelProvider implements ModelProvider {

    public static ChatModel createChatModel(final OpenAiModelParameters parameters) throws MissingEnvironmentVariable {

        final String apiKey = ModelProvider.getApiKeyFromEnvironmentVariable(parameters.getApiKeyEnvironmentVariableName(), "OpenAI");

        final OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                                                              .apiKey(apiKey)
                                                              .modelName(parameters.getModelName());

        if (parameters.getBaseUrl().isPresent()) {
            builder.baseUrl(parameters.getBaseUrl().get().toString());
        }

        if (parameters.getOrganizationId().isPresent()) {
            builder.organizationId(parameters.getOrganizationId().get());
        }

        if (parameters.getProjectId().isPresent()) {
            builder.projectId(parameters.getProjectId().get());
        }

        if (parameters.getTemperature().isPresent()) {
            builder.temperature(parameters.getTemperature().get());
        }

        if (parameters.getSeed().isPresent()) {
            builder.seed(parameters.getSeed().get());
        }

        if (parameters.getTopP().isPresent()) {
            builder.topP(parameters.getTopP().get());
        }

        if (parameters.getMaxCompletionTokens().isPresent()) {
            builder.maxCompletionTokens(parameters.getMaxCompletionTokens().get());
        }

        return builder.logRequests(Boolean.TRUE)
                      .logResponses(Boolean.TRUE)
                      .build();
    }
}
