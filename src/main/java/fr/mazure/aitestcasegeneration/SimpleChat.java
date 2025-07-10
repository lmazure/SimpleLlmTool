package fr.mazure.aitestcasegeneration;

import java.util.Optional;

import dev.langchain4j.model.chat.ChatModel;
import fr.mazure.aitestcasegeneration.provider.openai.OpenAiChatModelProvider;
import fr.mazure.aitestcasegeneration.provider.openai.OpenAiModelParameters;

public class SimpleChat {

    public static void main(final String[] args) {

        final OpenAiModelParameters parameters = new OpenAiModelParameters("gpt-4o-mini", Optional.empty(), "OPENAI_API_KEY");
        final ChatModel model = OpenAiChatModelProvider.createChatModel(parameters);

        final String question = "Why is the sky blue?";
        final String answer = model.chat(question);

        System.out.println("Question: " + question);
        System.out.println("Answer: " + answer);
    }
}
