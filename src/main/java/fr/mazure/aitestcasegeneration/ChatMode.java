package fr.mazure.aitestcasegeneration;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Optional;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.TokenUsage;

/**
 * The ChatMode class handles interactive chat processing.
 */
public class ChatMode extends BaseMode {

    /**
     * Handles interactive chat processing of chat interactions using a specified ChatModel.
     *
     * @param model The ChatModel to use for processing.
     * @param sysPrompt An optional system prompt.
     * @param userPrompt An optional user prompt.
     * @param log The PrintStream to use for logging.
     * @throws IOException If an I/O error occurs.
     */
    static void handleChat(final ChatModel model,
                           final Optional<String> sysPrompt,
                           final Optional<String> userPrompt,
                           final PrintStream log) throws IOException {

        // setup terminal
        try (final Terminal terminal = TerminalBuilder.builder()
                                                      .system(true)
                                                      .build()) {
            final LineReader reader = LineReaderBuilder.builder()
                                                       .terminal(terminal)
                                                       .build();

            // setup memory
            final ChatMemory memory = MessageWindowChatMemory.withMaxMessages(25);

            // print help message
            displayHelpMessage(terminal);

            // handle system prompt if it is defined
            if (sysPrompt.isPresent()) {
                final SystemMessage systemPrompt = new SystemMessage(sysPrompt.get());
                memory.add(systemPrompt);
                displaySystemPrompt(terminal, sysPrompt);
            }

            // handle chat
            String prefilledText = userPrompt.orElse("");
            while (true) {
                final String input = getUserInput(reader, prefilledText);
                if (input.isEmpty()) {
                    continue;
                }
                if (input.equals("/exit")) {
                    return;
                }
                if (input.equals("/tools list")) {
                    displayToolList(terminal);
                    continue;
                }
                memory.add(UserMessage.from(input));
                ChatResponse chatResponse = generateResponse(model, memory);
                AiMessage aiMessage = chatResponse.aiMessage();
                memory.add(aiMessage);
                while (aiMessage.hasToolExecutionRequests()) {
                    final List<ToolExecutionResultMessage> toolExecutionResultMessages = ToolManager.handleToolExecutionRequests(aiMessage.toolExecutionRequests());
                    for (final ToolExecutionResultMessage m: toolExecutionResultMessages) {
                        memory.add(m);
                    }
                    chatResponse = generateResponse(model, memory);
                    aiMessage = chatResponse.aiMessage();
                    memory.add(aiMessage);
                }
                displayAnswer(terminal, aiMessage.text());
                logTokenUsage(log, chatResponse.tokenUsage());
                prefilledText = "";
            }
        }
    }

    private static String getUserInput(final LineReader reader,
                                       final String prefilledText) {
        final String prompt = "Enter text: ";
        final AttributedString promptAttributedString = new AttributedString(prompt,
                                                                             AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
        final String input = reader.readLine(promptAttributedString.toAnsi(), null, prefilledText);
        return input;
    }

    private static void displayHelpMessage(final Terminal terminal) {
        final String helpMessage = """
                Type '/exit' to exit
                Type '/tools list' to display the list of availables tools
                """;;
        displayMessage(terminal, helpMessage);
    }

    private static void displayToolList(final Terminal terminal) {
        final String toolList = getToolListAsString();
        displayMessage(terminal, toolList);
    }

    private static void displayMessage(final Terminal terminal,
                                       final String message) {
        final AttributedString help = new AttributedString(message,
                                                           AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
        terminal.writer().println(help.toAnsi());
    }

    private static void displaySystemPrompt(final Terminal terminal,
                                            final Optional<String> sysPrompt) {
        final AttributedString systemPromptString = new AttributedString("System prompt: ",
                                                                         AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
        terminal.writer().print(systemPromptString.toAnsi());
        terminal.writer().println(sysPrompt.get());
    }

    private static void displayAnswer(final Terminal terminal,
                                      final String answer) {
        final AttributedString displayedAnswer = new AttributedString(answer,
                                                                      AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE));
        terminal.writer().println(displayedAnswer.toAnsi());
    }

    private static void logTokenUsage(final PrintStream log,
                                      final TokenUsage tokenUsage) {
        if (tokenUsage != null) {
            final Integer inputTokens = tokenUsage.inputTokenCount();
            final Integer outputTokens = tokenUsage.outputTokenCount();
            final Integer totalTokens = tokenUsage.totalTokenCount();

            log.println("Input tokens: " + inputTokens);
            log.println("Output tokens: " + outputTokens);
            log.println("Total tokens: " + totalTokens);
        }
    }

    private static String getToolListAsString() {
        final List<ToolManager.Tool> toolList = ToolManager.getToolList();
        final StringBuilder str = new StringBuilder();
        for (final ToolManager.Tool tool: toolList) {
            str.append(tool.name())
               .append(": ")
               .append(tool.description())
               .append("\n");
        }

        return str.toString();
    }

}
