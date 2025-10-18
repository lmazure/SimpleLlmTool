package fr.mazure.simplellmtool;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
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

import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.TokenUsage;
import fr.mazure.simplellmtool.CommandLine.Attachment;

/**
 * The ChatMode class handles interactive chat processing.
 */
public class ChatMode extends BaseMode {

        private static final Logger logger = LoggerFactory.getLogger(ChatMode.class);
        private static final String COMMAND_EXIT = "/exit";
        private static final String COMMAND_TOOLS_LIST = "/tools list";
        private static final String COMMAND_TOOLS_DETAILS = "/tools details";
        private static final String COMMAND_ATTACH_FILE = "/attach file";
        private static final String COMMAND_ATTACH_URL = "/attach url";

    /**
     * Handles interactive chat processing of chat interactions using a specified ChatModel.
     *
     * @param model The ChatModel to use for processing.
     * @param sysPrompt An optional system prompt.
     * @param userPrompt An optional user prompt.
     * @param attachments The attachments to send to the model.
     * @param toolManager The ToolManager to use for tool execution.
     *
     * @throws IOException If an I/O error occurs.
     * @throws ToolManagerException In case of a tool execution error
     */
    static void handleChat(final ChatModel model,
                           final Optional<String> sysPrompt,
                           final Optional<String> userPrompt,
                           final List<Attachment> initialAttachments,
                           final Optional<ToolManager> toolManager) throws IOException, ToolManagerException {

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
            List<Content> attachments = new ArrayList<>();
            try {
                attachments = AttachmentManager.getAttachmentsContent(initialAttachments);
            } catch (final AttachmentManagerException e) {
                displayError(terminal, e.getMessage() + "\nAll attachments will be ignored");
            }
            while (true) {
                final String input = getUserInput(reader, prefilledText);
                if (input.isEmpty()) {
                    continue;
                }
                if (input.equals(COMMAND_EXIT)) {
                    return;
                }
                if (input.equals(COMMAND_TOOLS_LIST)) {
                    displayToolList(terminal, toolManager, false);
                    continue;
                }
                if (input.equals(COMMAND_TOOLS_DETAILS)) {
                    displayToolList(terminal, toolManager, true);
                    continue;
                }
                if (input.startsWith(COMMAND_ATTACH_FILE + " ")) {
                    final String filePath = input.substring(COMMAND_ATTACH_FILE.length() + 1).trim();
                    try {
                        attachments.add(AttachmentManager.getFileContent(Paths.get(filePath).toAbsolutePath()));
                    } catch (final AttachmentManagerException e) {
                        displayError(terminal, e.getMessage());
                    }
                    continue;
                }
                if (input.startsWith(COMMAND_ATTACH_URL + " ")) {
                    final String url = input.substring(COMMAND_ATTACH_URL.length() + 1).trim();
                    try {
                        attachments.add(AttachmentManager.getUriContent(new URI(url)));
                    } catch (final URISyntaxException e) {
                        displayError(terminal, "Invalid URL:" + e.getMessage());
                    } catch (final AttachmentManagerException e) {
                        displayError(terminal, e.getMessage());
                    }
                    continue;
                }
                if (input.startsWith("/")) {
                    displayError(terminal, "Invalid command: " + input);
                    continue;
                }
                final List<Content> contents = new ArrayList<>();
                contents.add(TextContent.from(input));
                contents.addAll(attachments);
                memory.add(UserMessage.from(contents));
                final ChatResponse chatResponse = generateResponse(model, memory, toolManager);
                displayAnswer(terminal, chatResponse.aiMessage().text());
                logUsage(chatResponse.finishReason(), chatResponse.tokenUsage());
                prefilledText = "";
                attachments.clear();
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
                Type '%s' to exit
                Type '%s' to display the list of available tools
                Type '%s' to display the details of available tools
                Type '%s' to attach a file
                Type '%s' to attach a URL
                """.formatted(COMMAND_EXIT,
                              COMMAND_TOOLS_LIST,
                              COMMAND_TOOLS_DETAILS,
                              COMMAND_ATTACH_FILE,
                              COMMAND_ATTACH_URL);
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

    private static void displayError(final Terminal terminal,
                                     final String message) {
    final AttributedString help = new AttributedString(message,
                                                       AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
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

    private static void logUsage(final FinishReason finishReason,
                                 final TokenUsage tokenUsage) {
        if (finishReason != null) {
            logger.info("Finish reason: " + finishReason.toString());
        }

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
