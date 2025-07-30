package fr.mazure.aitestcasegeneration;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Optional;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.TokenUsage;

public class ChatMode {

    static void handleChat(final ChatModel model,
                           final Optional<String> sysPrompt,
                           final Optional<String> userPrompt,
                           final PrintStream log) throws IOException {
    
        // setup terminal
        final Terminal terminal = TerminalBuilder.builder()
                                                 .system(true)
                                                 .build();
        final LineReader reader = LineReaderBuilder.builder()
                                                   .terminal(terminal)
                                                   .build();
        final AttributedString prompt = new AttributedString("Enter text: ",
                                                             AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
    
        // print help message
        final String helpMessage = "Type '/exit' to exit";
        final AttributedString help = new AttributedString(helpMessage,
                                                           AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
        terminal.writer().println(help.toAnsi());
    
        // handle chat
        final ChatMemory memory = MessageWindowChatMemory.withMaxMessages(10);
    
        if (sysPrompt.isPresent()) {
            final SystemMessage systemPrompt = new SystemMessage(sysPrompt.get());
            memory.add(systemPrompt);
            final AttributedString systemPromptString = new AttributedString("System prompt: ",
                                                                             AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
            terminal.writer().print(systemPromptString.toAnsi());
            terminal.writer().println(sysPrompt.get());
        }
    
        String prefilledText = userPrompt.orElse("");
        while (true) {
            final String input = reader.readLine(prompt.toAnsi(), null, prefilledText);
            if (input.isEmpty()) {
                continue;
            }
            if (input.equals("/exit")) {
                terminal.close();
                return;
            }
    
            memory.add(UserMessage.from(input));
            final ChatRequest chatRequest = ChatRequest.builder()
                                                       .parameters(model.defaultRequestParameters())
                                                       .messages(memory.messages())
                                                       .build();
            final ChatResponse response = model.doChat(chatRequest);
            final String answer = response.aiMessage().text();
            memory.add(AiMessage.from(answer));
            final AttributedString displayedAnswer = new AttributedString(answer, AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE));
            terminal.writer().println(displayedAnswer.toAnsi());
            final TokenUsage tokenUsage = response.tokenUsage();
            if (tokenUsage != null) {
                final Integer inputTokens = tokenUsage.inputTokenCount();
                final Integer outputTokens = tokenUsage.outputTokenCount();
                final Integer totalTokens = tokenUsage.totalTokenCount();

                log.println("Input tokens: " + inputTokens);
                log.println("Output tokens: " + outputTokens);
                log.println("Total tokens: " + totalTokens);
            }
            prefilledText = "";
        }
    }
    
}
