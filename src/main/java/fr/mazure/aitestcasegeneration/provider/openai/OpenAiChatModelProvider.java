package fr.mazure.aitestcasegeneration.provider.openai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel.OpenAiChatModelBuilder;
import fr.mazure.aitestcasegeneration.provider.base.ModelProvider;

public class OpenAiChatModelProvider implements ModelProvider {
    
    public static ChatModel createChatModel(final OpenAiModelParameters parameters) {
        // Make sure to set the OPENAI_API_KEY environment variable.
        final String apiKey = System.getenv(parameters.getApiKeyEnvironmentVariableName());
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.err.println("Error: " + parameters.getApiKeyEnvironmentVariableName() + " environment variable is not set.");
            System.err.println("Please set it to your OpenAI API key.");
            System.exit(1);
        }

        final OpenAiChatModelBuilder model = OpenAiChatModel.builder()
                                                            .apiKey(apiKey)
                                                            .modelName(parameters.getModelName());

        if (parameters.getUrl().isPresent()) {
            model.baseUrl(parameters.getUrl().get().toString());
        }

        return model.build();
    }
}
