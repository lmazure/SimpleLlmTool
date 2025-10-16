package fr.mazure.simplellmtool.provider.mock;

import dev.langchain4j.model.chat.ChatModel;
import fr.mazure.simplellmtool.provider.base.ModelProvider;
import fr.mazure.simplellmtool.provider.mock.internal.MockChatModel;

/**
 * The mock model provider.
 */
public class MockChatModelProvider implements ModelProvider {

    public static ChatModel createChatModel(@SuppressWarnings("unused") final MockModelParameters parameters) {

        return new MockChatModel();
    }
}
