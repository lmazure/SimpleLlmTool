package fr.mazure.simplellmtool.provider.base;

import dev.langchain4j.model.chat.ChatModel;

public interface ModelProvider {

    /**
     * Creates a chat model instance based on the provided parameters.
     *
     * @param parameters The parameters for creating the chat model.
     * @return A chat model instance.
     */
    public static ChatModel createChatModel(final ModelParameters parameters) {
        throw new UnsupportedOperationException("createChatModel must be implemented by each class");
    }

    /**
     * Retrieves an API key from the environment variable.
     *
     * @param apiKeyEnvironmentVariableName The name of the environment variable containing the API key.
     * @param providerName The name of the provider.
     * @return The API key.
     * @throws MissingEnvironmentVariable If the environment variable is not defined.
     */
    static String getApiKeyFromEnvironmentVariable(final String apiKeyEnvironmentVariableName,
                                                   final String providerName) throws MissingEnvironmentVariable {
        final String apiKey = System.getenv(apiKeyEnvironmentVariableName);
        if (apiKey == null) {
            throw new MissingEnvironmentVariable(apiKeyEnvironmentVariableName, "Please set it to your " + providerName + " API key.");
        }

        return apiKey;
    }
}
