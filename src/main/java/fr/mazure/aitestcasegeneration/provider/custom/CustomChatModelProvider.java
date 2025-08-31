package fr.mazure.simplellmtool.provider.custom;

import java.io.PrintStream;

import dev.langchain4j.model.chat.ChatModel;
import fr.mazure.simplellmtool.provider.base.MissingEnvironmentVariable;
import fr.mazure.simplellmtool.provider.base.ModelProvider;
import fr.mazure.simplellmtool.provider.custom.internal.CustomChatModel;
import fr.mazure.simplellmtool.provider.custom.internal.CustomChatModelBuilder;

public class CustomChatModelProvider implements ModelProvider {

    public static ChatModel createChatModel(final CustomModelParameters parameters,
                                            final PrintStream log) throws MissingEnvironmentVariable {

        final String apiKey = ModelProvider.getApiKeyFromEnvironmentVariable(parameters.getApiKeyEnvironmentVariableName(), "custom");

        final CustomChatModelBuilder builder = CustomChatModel.builder()
                                                              .modelName(parameters.getModelName())
                                                              .apiKey(apiKey)
                                                              .baseUrl(parameters.getBaseUrl().get().toString())
                                                              .payloadTemplate(parameters.getPayloadTemplate())
                                                              .answerPath(parameters.getAnswerPath())
                                                              .httpHeaders(parameters.getHttpHeaders())
                                                              .inputTokenPath(parameters.getInputTokenPath())
                                                              .outputTokenPath(parameters.getOutputTokenPath())
                                                              .log(log);

        if (parameters.getLogRequests().isPresent()) {
            builder.logRequests(parameters.getLogRequests().get());
        }

        if (parameters.getLogResponses().isPresent()) {
            builder.logResponses(parameters.getLogResponses().get());
        }

        return builder.build();
    }
}
