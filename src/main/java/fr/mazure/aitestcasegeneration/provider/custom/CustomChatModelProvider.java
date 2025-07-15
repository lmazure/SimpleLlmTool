package fr.mazure.aitestcasegeneration.provider.custom;

import dev.langchain4j.model.chat.ChatModel;
import fr.mazure.aitestcasegeneration.provider.base.MissingEnvironmentVariable;
import fr.mazure.aitestcasegeneration.provider.base.ModelProvider;

public class CustomChatModelProvider implements ModelProvider {

    public static ChatModel createChatModel(final CustomModelParameters parameters) throws MissingEnvironmentVariable {

        final String apiKey = ModelProvider.getApiKeyFromEnvironmentVariable(parameters.getApiKeyEnvironmentVariableName(), "custom");

        final CustomChatModelBuilder builder = CustomChatModel.builder()
                                                              .apiKey(apiKey)
                                                              .modelName(parameters.getModelName());

        if (parameters.getUrl().isPresent()) {
            builder.baseUrl(parameters.getUrl().get().toString());
        }

        return builder.build();
    }
}
