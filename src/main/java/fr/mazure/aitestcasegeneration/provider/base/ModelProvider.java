package fr.mazure.aitestcasegeneration.provider.base;

import dev.langchain4j.model.chat.ChatModel;

public interface ModelProvider {
    public static ChatModel createChatModel(final ModelParameters parameters) {
        throw new UnsupportedOperationException("createChatModel must be implemented by each class");
    }
    static String getApiKeyFromEnvironmentVariable(final String apiKeyEnvironmentVariableName,
                                                   final String providerName) throws MissingEnvironmentVariable {
        final String apiKey = System.getenv(apiKeyEnvironmentVariableName);
        if (apiKey == null) {
            throw new MissingEnvironmentVariable(apiKeyEnvironmentVariableName, "Please set it to your " + providerName + " API key.");
        }

        return apiKey;
    }
}
