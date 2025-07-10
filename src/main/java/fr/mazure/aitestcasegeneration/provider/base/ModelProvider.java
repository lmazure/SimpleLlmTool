package fr.mazure.aitestcasegeneration.provider.base;

import dev.langchain4j.model.chat.ChatModel;

public interface ModelProvider {
    public static ChatModel createChatModel(final ModelParameters parameters) {
        throw new UnsupportedOperationException("createChatModel must be implemented by each class");
    }
}
