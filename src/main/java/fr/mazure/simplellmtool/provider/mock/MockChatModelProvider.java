package fr.mazure.simplellmtool.provider.mock;

import dev.langchain4j.model.chat.ChatModel;
import fr.mazure.simplellmtool.provider.base.MissingEnvironmentVariable;
import fr.mazure.simplellmtool.provider.base.ModelProvider;
import fr.mazure.simplellmtool.provider.mock.internal.MockChatModel;

public class MockChatModelProvider implements ModelProvider {

    public static ChatModel createChatModel(final MockModelParameters parameters) throws MissingEnvironmentVariable {

        return new MockChatModel();
    }
}
