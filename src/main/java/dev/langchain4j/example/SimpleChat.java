package dev.langchain4j.example;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

public class SimpleChat {

    public static void main(String[] args) {

        // Make sure to set the OPENAI_API_KEY environment variable.
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.err.println("Error: OPENAI_API_KEY environment variable is not set.");
            System.err.println("Please set it to your OpenAI API key.");
            System.exit(1);
        }

        ChatLanguageModel model = OpenAiChatModel.withApiKey(apiKey);

        String question = "Why is the sky blue?";
        String answer = model.generate(question);

        System.out.println("Question: " + question);
        System.out.println("Answer: " + answer);
    }
}
