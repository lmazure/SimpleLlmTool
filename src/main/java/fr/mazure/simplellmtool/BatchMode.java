package fr.mazure.simplellmtool;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import fr.mazure.simplellmtool.attachments.Attachment;
import fr.mazure.simplellmtool.attachments.AttachmentManager;
import fr.mazure.simplellmtool.attachments.AttachmentManagerException;
import fr.mazure.simplellmtool.tools.ToolManager;
import fr.mazure.simplellmtool.tools.ToolManagerException;

/**
 * The BatchMode class handles batch processing.
 */
public class BatchMode extends BaseMode {

    /**
     * Handles batch processing of chat interactions using a specified ChatModel.
     *
     * @param model The ChatModel to use for processing.
     * @param sysPrompt An optional system prompt.
     * @param userPrompt The user prompt to process.
     * @param initialAttachments The attachments to send to the model.
     * @param output The PrintStream to use for output.
     * @param error The PrintStream to use for error output.
     * @param toolManager The ToolManager to use for tool execution.
     *
     * @return The exit code.
     */
    static int handleBatch(final ChatModel model,
                           final Optional<String> sysPrompt,
                           final String userPrompt,
                           final List<Attachment> initialAttachments,
                           final PrintStream output,
                           final PrintStream error,
                           final Optional<ToolManager> toolManager) {
        final ChatMemory memory = MessageWindowChatMemory.withMaxMessages(15);

        if (sysPrompt.isPresent()) {
            final SystemMessage systemPrompt = new SystemMessage(sysPrompt.get());
            memory.add(systemPrompt);
        }

        List<Content> attachments = new ArrayList<>();
        try {
            attachments = AttachmentManager.getAttachmentsContent(initialAttachments);
        } catch (final AttachmentManagerException e) {
            error.println("Invalid attachment: " + e.getMessage());
            return ExitCode.ATTACHMENT_ERROR.getCode();
        }

        final List<Content> contents = new ArrayList<>();
        contents.add(TextContent.from(userPrompt));
        contents.addAll(attachments);
        memory.add(UserMessage.from(contents));

        ChatResponse response;
        try {
            response = generateResponse(model, memory, toolManager);
        } catch (final ToolManagerException e) {
            error.println("Tool error: " + e.getMessage());
            return ExitCode.TOOL_ERROR.getCode();
        }
        final String answer = response.aiMessage().text();
        output.println(answer);
        return ExitCode.SUCCESS.getCode();
    }
}
