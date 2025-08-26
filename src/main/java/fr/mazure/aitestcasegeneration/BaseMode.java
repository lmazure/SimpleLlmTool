package fr.mazure.aitestcasegeneration;

import java.util.Optional;

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
        final ChatRequestParameters parameters = model.defaultRequestParameters();
        if (toolManager.isPresent()) {
            parameters.overrideWith(ChatRequestParameters.builder()
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
