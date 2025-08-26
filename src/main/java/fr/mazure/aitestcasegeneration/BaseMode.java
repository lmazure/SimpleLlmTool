package fr.mazure.aitestcasegeneration;

import java.util.List;
import java.util.Optional;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;

/**
 * Base class for BatchMode and ChatMode.
 */
public class BaseMode {

    protected static ChatResponse generateResponse(final ChatModel model,
                                                   final ChatMemory memory,
                                                   final Optional<ToolManager> toolManager) {
        ChatResponse chatResponse = callChatModel(model, memory, toolManager);
        AiMessage aiMessage = chatResponse.aiMessage();
        memory.add(aiMessage);
        while (aiMessage.hasToolExecutionRequests()) {
            assert toolManager.isPresent();
            final List<ToolExecutionResultMessage> toolExecutionResultMessages = toolManager.get().handleToolExecutionRequests(aiMessage.toolExecutionRequests());
            for (final ToolExecutionResultMessage m: toolExecutionResultMessages) {
                memory.add(m);
            }
            chatResponse = callChatModel(model, memory, toolManager);
            aiMessage = chatResponse.aiMessage();
            memory.add(aiMessage);
        }
        return chatResponse;
    }

    private static ChatResponse callChatModel(final ChatModel model,
                                              final ChatMemory memory,
                                              final Optional<ToolManager> toolManager) {
        ChatRequestParameters parameters = model.defaultRequestParameters();
        if (toolManager.isPresent()) {
            parameters = parameters.overrideWith(ChatRequestParameters.builder()
                                                                      .toolSpecifications(toolManager.get().getSpecifications())
                                                                      .build());
        }
        final ChatRequest chatRequest = ChatRequest.builder()
                                                   .parameters(parameters)
                                                   .messages(memory.messages())
                                                   .build();
        return model.doChat(chatRequest);
    }
}
