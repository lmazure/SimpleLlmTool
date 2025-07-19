package fr.mazure.aitestcasegeneration.provider.mock;

import dev.langchain4j.model.chat.ChatModel;
import fr.mazure.aitestcasegeneration.provider.base.MissingEnvironmentVariable;
import fr.mazure.aitestcasegeneration.provider.base.ModelProvider;
import fr.mazure.aitestcasegeneration.provider.mock.internal.MockChatModel;

public class MockChatModelProvider implements ModelProvider {

    public static ChatModel createChatModel(final MockModelParameters parameters) throws MissingEnvironmentVariable {

        return new MockChatModel();
    }
}
