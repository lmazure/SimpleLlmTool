package fr.mazure.simplellmtool;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.SystemMessage;
//import dev.langchain4j.data.message.TextContent;
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

        private static final Logger logger = LoggerFactory.getLogger(ChatMode.class);

    /**
     * Handles interactive chat processing of chat interactions using a specified ChatModel.
     *
     * @param model The ChatModel to use for processing.
     * @param sysPrompt An optional system prompt.
     * @param userPrompt An optional user prompt.
     * @param toolManager The ToolManager to use for tool execution.
     *
     * @throws IOException If an I/O error occurs.
     */
    static void handleChat(final ChatModel model,
                           final Optional<String> sysPrompt,
                           final Optional<String> userPrompt,
                           final Optional<ToolManager> toolManager) throws IOException {

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
                    displayToolList(terminal, toolManager, false);
                    continue;
                }
                if (input.equals("/tools details")) {
                    displayToolList(terminal, toolManager, true);
                    continue;
                }
                memory.add(UserMessage.from(input));
                //memory.add(UserMessage.from(TextContent.from(input),
                //                            ImageContent.from("https://example.com/cat.jpg")));
                final ChatResponse chatResponse = generateResponse(model, memory, toolManager);
                displayAnswer(terminal, chatResponse.aiMessage().text());
                logTokenUsage(chatResponse.tokenUsage());
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
                Type '/tools list' to display the list of available tools
                Type '/tools details' to display the details of available tools
                """;;
        displayMessage(terminal, helpMessage);
    }

    private static void displayToolList(final Terminal terminal,
                                        final Optional<ToolManager> toolManager,
                                        final boolean includeParameters) {
        if (toolManager.isPresent()) {
            final String toolList = getToolListAsString(toolManager.get(), includeParameters);
            displayMessage(terminal, toolList);
        } else {
            displayMessage(terminal, "No tools available");
        }
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

    private static void logTokenUsage(final TokenUsage tokenUsage) {
        if (tokenUsage != null) {
            final Integer inputTokens = tokenUsage.inputTokenCount();
            final Integer outputTokens = tokenUsage.outputTokenCount();
            final Integer totalTokens = tokenUsage.totalTokenCount();

            logger.info("Input tokens: " + inputTokens);
            logger.info("Output tokens: " + outputTokens);
            logger.info("Total tokens: " + totalTokens);
        }
    }

    private static String getToolListAsString(final ToolManager toolManager,
                                              final boolean includeParameters) {
        final List<ToolManager.Tool> toolList = toolManager.getToolList();
        final StringBuilder str = new StringBuilder();
        for (final ToolManager.Tool tool: toolList) {
            str.append(tool.name())
               .append(": ")
               .append(tool.description())
               .append("\n");
            if (includeParameters) {
                for (final ToolManager.ToolParameter param: tool.parameters()) {
                    str.append("  ")
                       .append(param.name())
                       .append(": ")
                       .append(param.description())
                       .append("\n");
                }
            }
        }

        return str.toString();
    }
}
