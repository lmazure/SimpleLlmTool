package fr.mazure.aitestcasegeneration.provider.mock.internal;

import java.util.List;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;

public class MockChatModel implements ChatModel {

    // Constructor that takes the builder
    public MockChatModel() {

    }

    @Override
    public ChatResponse doChat(final ChatRequest chatRequest) {
        final List<ChatMessage> messages = chatRequest.messages();

        final StringBuilder mockAnswerBuilder = new StringBuilder();
        mockAnswerBuilder.append(String.format("%03d ", messages.size()));
        final ChatMessage lastMessage = messages.get(messages.size() - 1);
        mockAnswerBuilder.append(
            switch (lastMessage) {
                case UserMessage userMessage -> "USER " + userMessage.singleText();
                case AiMessage aiMessage -> "MODEL " + aiMessage.text();
                case SystemMessage systemMessage -> "SYSTEM " + systemMessage.text();
                default -> throw new IllegalArgumentException("Unsupported message type: " + lastMessage.getClass()); // TODO: we will have to support tools and 
            });

        return ChatResponse.builder()
                           .aiMessage(AiMessage.from(mockAnswerBuilder.toString()))
                           .build();
    }
}