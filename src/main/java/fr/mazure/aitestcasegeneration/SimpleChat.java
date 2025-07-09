package fr.mazure.aitestcasegeneration;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

public class SimpleChat {

    public static void main(String[] args) {

        // Make sure to set the OPENAI_API_KEY environment variable.
        final String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.err.println("Error: OPENAI_API_KEY environment variable is not set.");
            System.err.println("Please set it to your OpenAI API key.");
            System.exit(1);
        }

        final ChatModel model = OpenAiChatModel.builder()
                                               .apiKey(System.getenv("OPENAI_API_KEY"))
                                               .modelName("gpt-4o-mini")
                                               .build();

        final String question = "Why is the sky blue?";
        final String answer = model.chat(question);

        System.out.println("Question: " + question);
        System.out.println("Answer: " + answer);
    }
}
