package fr.mazure.simplellmtool.provider.custom;

import dev.langchain4j.model.chat.ChatModel;
import fr.mazure.simplellmtool.provider.base.MissingEnvironmentVariable;
import fr.mazure.simplellmtool.provider.base.ModelProvider;
import fr.mazure.simplellmtool.provider.custom.internal.CustomChatModel;

/**
 * The custom model provider.
 */
public class CustomChatModelProvider implements ModelProvider {

    public static ChatModel createChatModel(final CustomModelParameters parameters) throws MissingEnvironmentVariable {

        final String apiKey = ModelProvider.getApiKeyFromEnvironmentVariable(parameters.getApiKeyEnvironmentVariableName(), "custom");

        return CustomChatModel.builder()
                              .modelName(parameters.getModelName())
                              .apiKey(apiKey)
                              .baseUrl(parameters.getBaseUrl().get().toString())
                              .payloadTemplate(parameters.getPayloadTemplate())
                              .answerPath(parameters.getAnswerPath())
                              .httpHeaders(parameters.getHttpHeaders())
                              .inputTokenPath(parameters.getInputTokenPath())
                              .outputTokenPath(parameters.getOutputTokenPath())
                              .finishReasonPath(parameters.getFinishReasonPath())
                              .finishReasonMappings(parameters.getFinishReasonMappings())
                              .toolCallsPath(parameters.getToolCallsPath())
                              .toolNamePath(parameters.getToolNamePath())
                              .toolArgumentsDictPath(parameters.getToolArgumentsDictPath())
                              .toolArgumentsStringPath(parameters.getToolArgumentsStringPath())
                              .logRequests(Boolean.TRUE)
                              .logResponses(Boolean.TRUE)
                              .build();
    }
}
