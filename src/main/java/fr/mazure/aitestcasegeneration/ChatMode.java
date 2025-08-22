package fr.mazure.aitestcasegeneration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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

/**
 * The ChatMode class handles interactive chat processing.
 */
public class ChatMode {

    private record ChatAnswer(String answer, TokenUsage tokenUsage) {}

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
            final ChatMemory memory = MessageWindowChatMemory.withMaxMessages(10);

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
                final ChatAnswer chatAnswer = generateAnswer(model, memory);
                memory.add(AiMessage.from(chatAnswer.answer()));
                displayAnswer(terminal, chatAnswer.answer());
                logTokenUsage(log, chatAnswer.tokenUsage());
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
        final String toolList = getToolList();
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

    private static ChatAnswer generateAnswer(final ChatModel model,
                                             final ChatMemory memory) {
        final ChatRequest chatRequest = ChatRequest.builder()
                                                   .parameters(model.defaultRequestParameters())
                                                   .messages(memory.messages())
                                                   .build();
        final ChatResponse response = model.doChat(chatRequest);
        return new ChatAnswer(response.aiMessage().text(), response.tokenUsage());
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

    private static String getToolList() {
        final File toolsDir = new File("tools");
        if (!toolsDir.exists() || !toolsDir.isDirectory()) {
            throw new RuntimeException("No tools directory found.");
        }

        final File[] pythonFiles = toolsDir.listFiles((_, name) -> name.endsWith(".py"));
        if (pythonFiles == null || pythonFiles.length == 0) {
            return "";
        }

        final StringBuilder toolList = new StringBuilder();
        for (final File pythonFile : pythonFiles) {
            final String scriptName = pythonFile.getName().replace(".py", "");
            final String description = getToolDescription(pythonFile);
            toolList.append(scriptName)
                    .append(": ")
                    .append(description)
                    .append("\n");
        }

        return toolList.toString();
    }

    private static String getToolDescription(final File pythonFile) {
        try {
            final ProcessBuilder pb = new ProcessBuilder("python", pythonFile.getAbsolutePath(), "--description");
            pb.redirectErrorStream(true);
            final Process process = pb.start();

            final StringBuilder output = new StringBuilder();
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                output.append(reader.readLine());
            }

            final int exitCode = process.waitFor();
            if (exitCode != 0 || output.length() == 0) {
                throw new RuntimeException("description not available for " + pythonFile);
            }

            return output.toString().trim();
        } catch (final IOException | InterruptedException e) {
            throw new RuntimeException("failed to get description of " + pythonFile, e);
        }
    }
}
