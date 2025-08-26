package fr.mazure.aitestcasegeneration;

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
                                                   final ChatMemory memory) {
        final ChatRequest chatRequest = ChatRequest.builder()
                                                   .parameters(model.defaultRequestParameters()
                                                                    .overrideWith(ChatRequestParameters.builder()
                                                                                                       .toolSpecifications(ToolManager.getSpecifications())
                                                                                                       .build()))
                                                   .messages(memory.messages())
                                                   .build();
        return model.doChat(chatRequest);
    }
}
