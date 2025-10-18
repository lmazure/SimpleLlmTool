package fr.mazure.simplellmtool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dev.langchain4j.model.chat.ChatModel;
import fr.mazure.simplellmtool.CommandLine.Attachment;
import fr.mazure.simplellmtool.CommandLine.AttachmentSource;
import fr.mazure.simplellmtool.provider.anthropic.AnthropicChatModelProvider;
import fr.mazure.simplellmtool.provider.anthropic.AnthropicModelParameters;
import fr.mazure.simplellmtool.provider.base.MissingEnvironmentVariable;
import fr.mazure.simplellmtool.provider.custom.CustomChatModelProvider;
import fr.mazure.simplellmtool.provider.custom.CustomModelParameters;
import fr.mazure.simplellmtool.provider.custom.internal.CustomChatModel;
import fr.mazure.simplellmtool.provider.googlegemini.GoogleGeminiChatModelProvider;
import fr.mazure.simplellmtool.provider.googlegemini.GoogleGeminiModelParameters;
import fr.mazure.simplellmtool.provider.mistralai.MistralAiChatModelProvider;
import fr.mazure.simplellmtool.provider.mistralai.MistralAiModelParameters;
import fr.mazure.simplellmtool.provider.mock.MockChatModelProvider;
import fr.mazure.simplellmtool.provider.mock.MockModelParameters;
import fr.mazure.simplellmtool.provider.openai.OpenAiChatModelProvider;
import fr.mazure.simplellmtool.provider.openai.OpenAiModelParameters;

/**
 * Tests for the {@link BatchMode} class.
 */
class BatchModeTest {

    /**
     * Basic test for OpenAI.
     * @throws MissingEnvironmentVariable
     */
    @SuppressWarnings("static-method")
    @Test
    @Tag("e2e")
    @Tag("e2e_openai")
    void testBasicOpenAi() throws MissingEnvironmentVariable {
        // Given
        final ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        final PrintStream output = new PrintStream(outputBuffer);
        final OpenAiModelParameters parameters = new OpenAiModelParameters("gpt-4.1-nano",
                                                                           Optional.empty(),
                                                                           "OPENAI_API_KEY",
                                                                           Optional.empty(),
                                                                           Optional.empty(),
                                                                           Optional.empty(),
                                                                           Optional.empty(),
                                                                           Optional.empty(),
                                                                           Optional.empty());
        final ChatModel model = OpenAiChatModelProvider.createChatModel(parameters);
        final Optional<String> sysPrompt = Optional.of("You must answer in one word.");
        final String userPrompt = "What is the capital of France?";

        // When
        final int exitCode = BatchMode.handleBatch(model, sysPrompt, userPrompt, List.of(), output, System.err, Optional.empty());

        // Then
        Assertions.assertEquals(ExitCode.SUCCESS.getCode(), exitCode);
        Assertions.assertEquals("Paris", outputBuffer.toString().trim());
    }

    /**
     * Basic test for Mistral AI.
     * @throws MissingEnvironmentVariable
     */
    @SuppressWarnings("static-method")
    @Test
    @Tag("e2e")
    @Tag("e2e_mistralai")
    void testBasicMistralAi() throws MissingEnvironmentVariable {
        // Given
        final ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        final PrintStream output = new PrintStream(outputBuffer);
        final MistralAiModelParameters parameters = new MistralAiModelParameters("mistral-small-latest",
                                                                                Optional.empty(),
                                                                                "MISTRALAI_API_KEY",
                                                                                Optional.empty(),
                                                                                Optional.empty(),
                                                                                Optional.empty());
        final ChatModel model = MistralAiChatModelProvider.createChatModel(parameters);
        final Optional<String> sysPrompt = Optional.of("You must answer in one word, with no punctuation.");
        final String userPrompt = "What is the capital of France?";

        // When
        final int exitCode = BatchMode.handleBatch(model, sysPrompt, userPrompt, List.of(), output, System.err, Optional.empty());

        // Then
        Assertions.assertEquals(ExitCode.SUCCESS.getCode(), exitCode);
        Assertions.assertEquals("Paris", outputBuffer.toString().trim());
    }

    /**
     * Basic test for Anthropic.
     * @throws MissingEnvironmentVariable
     */
    @SuppressWarnings("static-method")
    @Test
    @Tag("e2e")
    @Tag("e2e_anthropic")
    void testBasicAnthropic() throws MissingEnvironmentVariable {
        // Given
        final ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        final PrintStream output = new PrintStream(outputBuffer);
        final AnthropicModelParameters parameters = new AnthropicModelParameters("claude-sonnet-4-20250514",
                                                                                 Optional.empty(),
                                                                                 "ANTHROPIC_API_KEY",
                                                                                 Optional.empty(),
                                                                                 Optional.empty(),
                                                                                 Optional.empty(),
                                                                                 Optional.empty());
        final ChatModel model = AnthropicChatModelProvider.createChatModel(parameters);
        final Optional<String> sysPrompt = Optional.of("You must answer in one word with no punctuation.");
        final String userPrompt = "What is the capital of France?";

        // When
        final int exitCode = BatchMode.handleBatch(model, sysPrompt, userPrompt, List.of(), output, System.err, Optional.empty());

        // Then
        Assertions.assertEquals(ExitCode.SUCCESS.getCode(), exitCode);
        Assertions.assertEquals("Paris", outputBuffer.toString().trim());
    }

    /**
     * Basic test for Google Gemini.
     * @throws MissingEnvironmentVariable
     */
    @SuppressWarnings("static-method")
    @Test
    @Tag("e2e")
    @Tag("e2e_google_gemini")
    void testBasicGemini() throws MissingEnvironmentVariable {
        // Given
        final ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        final PrintStream output = new PrintStream(outputBuffer);
        final GoogleGeminiModelParameters parameters = new GoogleGeminiModelParameters("gemini-2.5-flash",
                                                                                       Optional.empty(),
                                                                                       "GOOGLE_GEMINI_API_KEY",
                                                                                       Optional.empty(),
                                                                                       Optional.empty(),
                                                                                       Optional.empty(),
                                                                                       Optional.empty());
        final ChatModel model = GoogleGeminiChatModelProvider.createChatModel(parameters);
        final Optional<String> sysPrompt = Optional.of("You must answer in one word with no punctuation, but using title case.");
        final String userPrompt = "What is the capital of France?";

        // When
        final int exitCode = BatchMode.handleBatch(model, sysPrompt, userPrompt, List.of(), output, System.err, Optional.empty());

        // Then
        Assertions.assertEquals(ExitCode.SUCCESS.getCode(), exitCode);
        Assertions.assertEquals("Paris", outputBuffer.toString().trim());
    }

    /**
     * Basic test for custom model.
     * @throws URISyntaxException
     * @throws MalformedURLException
     * @throws MissingEnvironmentVariable
     */
    @SuppressWarnings("static-method")
    @Test
    @Tag("e2e")
    @Tag("e2e_openai")
    void testBasicCustom() throws MalformedURLException, URISyntaxException, MissingEnvironmentVariable {
        // Given
        final ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        final PrintStream output = new PrintStream(outputBuffer);
        final String payloadTemplate = """
                            {
                              "model": "gpt-4.1-nano",
                              "messages": [
                                {{#each messages}}{
                                  "role": "{{#if (isSystem role)}}system{{/if}}{{#if (isUser role)}}user{{/if}}{{#if (isModel role)}}assistant{{/if}}",
                                  "content": {{convertToJsonString content}}
                                }{{#unless @last}},
                                {{/unless}}{{/each}}
                              ],
                              "temperature": 0.7,
                              "seed": 42
                            }
                """;
        final CustomModelParameters parameters = new CustomModelParameters("model_name_unused_in_template",
                                                                           (new URI("https://api.openai.com/v1/chat/completions")).toURL(),
                                                                           "OPENAI_API_KEY",
                                                                           payloadTemplate,
                                                                           Map.of("Authorization", "Bearer {{apiKey}}"),
                                                                           "choices[0].message.content",
                                                                           "usage.prompt_tokens",
                                                                           "usage.completion_tokens",
                                                                           "choices[0].finish_reason",
                                                                           Map.of("stop", CustomChatModel.FinishingReason.DONE),
                                                                           "choices[0].message.tool_calls",
                                                                           "function.name",
                                                                           "function.arguments");
        final ChatModel model = CustomChatModelProvider.createChatModel(parameters);
        final Optional<String> sysPrompt = Optional.of("You must answer in one word.");
        final String userPrompt = "What is the capital of France?";

        // When
        final int exitCode = BatchMode.handleBatch(model, sysPrompt, userPrompt, List.of(), output, System.err, Optional.empty());

        // Then
        Assertions.assertEquals(ExitCode.SUCCESS.getCode(), exitCode);
        Assertions.assertEquals("Paris", outputBuffer.toString().trim());
    }

    /**
     * Tool test for OpenAI.
     *
     * @throws MissingEnvironmentVariable
     * @throws ToolManagerException In case of a tool execution error
     */
    @SuppressWarnings("static-method")
    @Test
    @Tag("e2e")
    @Tag("e2e_openai")
    void testToolOpenAi(@TempDir final Path tempDir) throws IOException, MissingEnvironmentVariable, ToolManagerException {
        // Given
        final String pythonScript = """
                import sys
                from datetime import datetime

                def parse_date(date_string):
                    try:
                        return datetime.strptime(date_string, "%Y-%m-%d")
                    except ValueError:
                        print(f"Error: Invalid date format '{date_string}'. Use YYYY-MM-DD format (e.g., 2023-01-17)")
                        sys.exit(1)

                def main():
                    if len(sys.argv) == 2 and sys.argv[1] == "--description":
                        print("Calculate the number of days between start_date and end_date")
                        print("start_date\tstart date formatted as YYYY-MM-DD")
                        print("end_date\tend date formatted as YYYY-MM-DD")
                        sys.exit(0)

                    if len(sys.argv) != 3:
                        print("Usage: python date_diff.py <start_date> <end_date>")
                        sys.exit(1)

                    start_date_str = sys.argv[1]
                    end_date_str = sys.argv[2]

                    start_date = parse_date(start_date_str)
                    end_date = parse_date(end_date_str)

                    days_difference = (end_date - start_date).days
                    print(days_difference)

                if __name__ == "__main__":
                    main()
                """;
        final Path pythonScriptPath = tempDir.resolve("compute_dates_difference.py");
        Files.writeString(pythonScriptPath, pythonScript);
        final ToolManager toolManager = new ToolManager(tempDir);
        final ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        final PrintStream output = new PrintStream(outputBuffer);
        final OpenAiModelParameters parameters = new OpenAiModelParameters("gpt-4.1-nano",
                                                                           Optional.empty(),
                                                                           "OPENAI_API_KEY",
                                                                           Optional.empty(),
                                                                           Optional.empty(),
                                                                           Optional.empty(),
                                                                           Optional.empty(),
                                                                           Optional.empty(),
                                                                           Optional.empty());
        final ChatModel model = OpenAiChatModelProvider.createChatModel(parameters);
        final Optional<String> sysPrompt = Optional.of("You must answer in one number. Do not add any other text.");
        final String userPrompt = "How many days are there between 2021, January 23rd and 2027, September 3rd?";

        // When
        final int exitCode = BatchMode.handleBatch(model, sysPrompt, userPrompt, List.of(), output, System.err, Optional.of(toolManager));

        // Then
        Assertions.assertEquals(ExitCode.SUCCESS.getCode(), exitCode);
        Assertions.assertEquals("2414", outputBuffer.toString().trim());
    }

    /**
     * Management of invalid file attachment.
     * @throws MissingEnvironmentVariable
     */
    @SuppressWarnings("static-method")
    @Test
    void testInvalidFileAttachment() throws MissingEnvironmentVariable {
        // Given
        final ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        final PrintStream output = new PrintStream(outputBuffer);
        final ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();
        final PrintStream error = new PrintStream(errorBuffer);

        final ChatModel model = MockChatModelProvider.createChatModel(new MockModelParameters());
        final Optional<String> sysPrompt = Optional.of("You are a helpful assistant.");
        final String userPrompt = "What is the capital of France?";
        final List<Attachment> attachments = List.of(new Attachment(AttachmentSource.FILE, "invalid_file.jpg"));

        // When
        final int exitCode = BatchMode.handleBatch(model, sysPrompt, userPrompt, attachments, output, error, Optional.empty());

        // Then
        Assertions.assertEquals(ExitCode.ATTACHMENT_ERROR.getCode(), exitCode);
        Assertions.assertEquals("Invalid attachment: Error reading file: invalid_file.jpg", errorBuffer.toString().trim());
    }

    /**
     * Management of invalid URL attachment.
     * @throws MissingEnvironmentVariable
     */
    @SuppressWarnings("static-method")
    @Test
    void testInvalidUrlAttachment() throws MissingEnvironmentVariable {
        // Given
        final ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        final PrintStream output = new PrintStream(outputBuffer);
        final ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();
        final PrintStream error = new PrintStream(errorBuffer);

        final ChatModel model = MockChatModelProvider.createChatModel(new MockModelParameters());
        final Optional<String> sysPrompt = Optional.of("You are a helpful assistant.");
        final String userPrompt = "What is the capital of France?";
        final List<Attachment> attachments = List.of(new Attachment(AttachmentSource.URL, "http://example.com/path with spaces/file.jpg"));

        // When
        final int exitCode = BatchMode.handleBatch(model, sysPrompt, userPrompt, attachments, output, error, Optional.empty());

        // Then
        Assertions.assertEquals(ExitCode.ATTACHMENT_ERROR.getCode(), exitCode);
        Assertions.assertEquals("Invalid attachment: Invalid URL: Illegal character in path at index 23: http://example.com/path with spaces/file.jpg", errorBuffer.toString().trim());
    }

    /**
     * Test of image file attachment for Anthropic.
     * @throws MissingEnvironmentVariable
     */
    @SuppressWarnings("static-method")
    @Test
    @Tag("e2e")
    @Tag("e2e_anthropic")
    void testImageFileAttachmentAnthropic() throws MissingEnvironmentVariable {
        // Given
        final ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        final PrintStream output = new PrintStream(outputBuffer);
        final ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();
        final PrintStream error = new PrintStream(errorBuffer);

        final AnthropicModelParameters parameters = new AnthropicModelParameters("claude-sonnet-4-20250514",
                                                                                 Optional.empty(),
                                                                                 "ANTHROPIC_API_KEY",
                                                                                 Optional.empty(),
                                                                                 Optional.empty(),
                                                                                 Optional.empty(),
                                                                                 Optional.empty());
        final ChatModel model = AnthropicChatModelProvider.createChatModel(parameters);
        final String userPrompt = "Describe the image";
        final List<Attachment> attachments = List.of(new Attachment(AttachmentSource.FILE, "src/test/data/whiteCrossInRedDisk.jpg"));

        // When
        final int exitCode = BatchMode.handleBatch(model, Optional.empty(), userPrompt, attachments, output, error, Optional.empty());

        // Then
        Assertions.assertEquals(ExitCode.SUCCESS.getCode(), exitCode);
        Assertions.assertTrue(outputBuffer.toString().contains("plus sign"));
    }

    /**
     * Test of image URL attachment for Anthropic.
     * @throws MissingEnvironmentVariable
     */
    @SuppressWarnings("static-method")
    @Test
    @Tag("e2e")
    @Tag("e2e_anthropic")
    void testImageUrlAttachmentAnthropic() throws MissingEnvironmentVariable {
        // Given
        final ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        final PrintStream output = new PrintStream(outputBuffer);
        final ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();
        final PrintStream error = new PrintStream(errorBuffer);

        final AnthropicModelParameters parameters = new AnthropicModelParameters("claude-sonnet-4-20250514",
                                                                                 Optional.empty(),
                                                                                 "ANTHROPIC_API_KEY",
                                                                                 Optional.empty(),
                                                                                 Optional.empty(),
                                                                                 Optional.empty(),
                                                                                 Optional.empty());
        final ChatModel model = AnthropicChatModelProvider.createChatModel(parameters);
        final String userPrompt = "Describe the image";
        final List<Attachment> attachments = List.of(new Attachment(AttachmentSource.URL, "https://raw.githubusercontent.com/lmazure/SimpleLlmTool/main/src/test/data/whiteCrossInRedDisk.jpg"));

        // When
        final int exitCode = BatchMode.handleBatch(model, Optional.empty(), userPrompt, attachments, output, error, Optional.empty());

        // Then
        Assertions.assertEquals(ExitCode.SUCCESS.getCode(), exitCode);
        Assertions.assertTrue(outputBuffer.toString().contains("plus sign"));
    }

    /**
     * Test of PDF file attachment for Anthropic.
     * @throws MissingEnvironmentVariable
     */
    @SuppressWarnings("static-method")
    @Test
    @Tag("e2e")
    @Tag("e2e_anthropic")
    void testPdfFileAttachmentAnthropic() throws MissingEnvironmentVariable {
        // Given
        final ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        final PrintStream output = new PrintStream(outputBuffer);
        final ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();
        final PrintStream error = new PrintStream(errorBuffer);

        final AnthropicModelParameters parameters = new AnthropicModelParameters("claude-sonnet-4-20250514",
                                                                                 Optional.empty(),
                                                                                 "ANTHROPIC_API_KEY",
                                                                                 Optional.empty(),
                                                                                 Optional.empty(),
                                                                                 Optional.empty(),
                                                                                 Optional.empty());
        final ChatModel model = AnthropicChatModelProvider.createChatModel(parameters);
        final String userPrompt = "What is John birthday? Write only the date formatted as YYYY-MM-DD.";
        final List<Attachment> attachments = List.of(new Attachment(AttachmentSource.FILE, "src/test/data/john.pdf"));

        // When
        final int exitCode = BatchMode.handleBatch(model, Optional.empty(), userPrompt, attachments, output, error, Optional.empty());

        // Then
        Assertions.assertEquals(ExitCode.SUCCESS.getCode(), exitCode);
        Assertions.assertEquals("1941-03-07", outputBuffer.toString().trim());
    }

    /**
     * Test of PDF URL attachment for Anthropic.
     * @throws MissingEnvironmentVariable
     */
    @Disabled("Bug in LangChain4j?")
    @SuppressWarnings("static-method")
    @Test
    @Tag("e2e")
    @Tag("e2e_anthropic")
    void testPdfUrlAttachmentAnthropic() throws MissingEnvironmentVariable {
        // Given
        final ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        final PrintStream output = new PrintStream(outputBuffer);
        final ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();
        final PrintStream error = new PrintStream(errorBuffer);

        final AnthropicModelParameters parameters = new AnthropicModelParameters("claude-sonnet-4-20250514",
                                                                                 Optional.empty(),
                                                                                 "ANTHROPIC_API_KEY",
                                                                                 Optional.empty(),
                                                                                 Optional.empty(),
                                                                                 Optional.empty(),
                                                                                 Optional.empty());
        final ChatModel model = AnthropicChatModelProvider.createChatModel(parameters);
        final String userPrompt = "What is John birthday? Write only the date formatted as YYYY-MM-DD.";
        final List<Attachment> attachments = List.of(new Attachment(AttachmentSource.URL, "https://raw.githubusercontent.com/lmazure/SimpleLlmTool/main/src/test/data/john.pdf"));

        // When
        final int exitCode = BatchMode.handleBatch(model, Optional.empty(), userPrompt, attachments, output, error, Optional.empty());

        // Then
        Assertions.assertEquals(ExitCode.SUCCESS.getCode(), exitCode);
        Assertions.assertEquals("1941-03-07", outputBuffer.toString().trim());
    }

    /**
     * Test of PDF URL attachment for OpenAI.
     * @throws MissingEnvironmentVariable
     */
    @Disabled("Bug in LangChain4j?")
    @SuppressWarnings("static-method")
    @Test
    @Tag("e2e")
    @Tag("e2e_openai")
    void testPdfUrlAttachmentOpenAI() throws MissingEnvironmentVariable {
        // Given
        final ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        final PrintStream output = new PrintStream(outputBuffer);
        final ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();
        final PrintStream error = new PrintStream(errorBuffer);

        final OpenAiModelParameters parameters = new OpenAiModelParameters("gpt-5-mini-2025-08-07",
                                                                           Optional.empty(),
                                                                           "OPENAI_API_KEY",
                                                                           Optional.empty(),
                                                                           Optional.empty(),
                                                                           Optional.empty(),
                                                                           Optional.empty(),
                                                                           Optional.empty(),
                                                                           Optional.empty());
        final ChatModel model = OpenAiChatModelProvider.createChatModel(parameters);
        final String userPrompt = "What is John birthday? Write only the date formatted as YYYY-MM-DD.";
        final List<Attachment> attachments = List.of(new Attachment(AttachmentSource.URL, "https://raw.githubusercontent.com/lmazure/SimpleLlmTool/main/src/test/data/john.pdf"));

        // When
        final int exitCode = BatchMode.handleBatch(model, Optional.empty(), userPrompt, attachments, output, error, Optional.empty());

        // Then
        Assertions.assertEquals(ExitCode.SUCCESS.getCode(), exitCode);
        Assertions.assertEquals("1941-03-07", outputBuffer.toString().trim());
    }
}
