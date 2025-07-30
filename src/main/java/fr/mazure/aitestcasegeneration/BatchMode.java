package fr.mazure.aitestcasegeneration;

import java.io.PrintStream;
import java.util.Optional;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;

public class BatchMode {

    static void handleBatch(final ChatModel model,
                            final Optional<String> sysPrompt,
                            final String userPrompt,
                            final PrintStream output,
                            final PrintStream log ) {
        final ChatMemory memory = MessageWindowChatMemory.withMaxMessages(2);
    
        if (sysPrompt.isPresent()) {
            final SystemMessage systemPrompt = new SystemMessage(sysPrompt.get());
            memory.add(systemPrompt);
        }
        memory.add(UserMessage.from(userPrompt));
    
        final ChatRequest chatRequest = ChatRequest.builder()
                                                   .parameters(model.defaultRequestParameters())
                                                   .messages(memory.messages())
                                                   .build();
        final ChatResponse response = model.doChat(chatRequest);
        final String answer = response.aiMessage().text();
        output.println(answer);
    }
    
}
