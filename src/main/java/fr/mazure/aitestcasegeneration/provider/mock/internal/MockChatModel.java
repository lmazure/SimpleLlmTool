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
        final int numberOfReturnedMessages = Math.min(messages.size(), 5);
        for (int i = messages.size() - numberOfReturnedMessages; i < messages.size(); i++) {
            final ChatMessage message = messages.get(i);
            mockAnswerBuilder.append(String.format("%03d ", i));
            mockAnswerBuilder.append(
                switch (message) {
                    case UserMessage userMessage -> "USER " + userMessage.singleText().replaceAll("\n", "↵");
                    case AiMessage aiMessage -> "MODEL " + aiMessage.text().replaceAll("\n", "↵");
                    case SystemMessage systemMessage -> "SYSTEM " + systemMessage.text().replaceAll("\n", "↵");
                    default -> throw new IllegalArgumentException("Unsupported message type: " + message.getClass());
                });
            if (i < messages.size() - 1) {
                mockAnswerBuilder.append("\n");
            }
        }

        return ChatResponse.builder()
                           .aiMessage(AiMessage.from(mockAnswerBuilder.toString()))
                           .build();
    }
}