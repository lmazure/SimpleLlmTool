package fr.mazure.aitestcasegeneration;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import dev.langchain4j.model.chat.ChatModel;

import fr.mazure.aitestcasegeneration.provider.anthropic.AnthropicChatModelProvider;
import fr.mazure.aitestcasegeneration.provider.anthropic.AnthropicModelParameters;
import fr.mazure.aitestcasegeneration.provider.base.MissingEnvironmentVariable;
import fr.mazure.aitestcasegeneration.provider.custom.CustomChatModelProvider;
import fr.mazure.aitestcasegeneration.provider.custom.CustomModelParameters;
import fr.mazure.aitestcasegeneration.provider.googlegemini.GoogleGeminiChatModelProvider;
import fr.mazure.aitestcasegeneration.provider.googlegemini.GoogleGeminiModelParameters;
import fr.mazure.aitestcasegeneration.provider.mistralai.MistralAiChatModelProvider;
import fr.mazure.aitestcasegeneration.provider.mistralai.MistralAiModelParameters;
import fr.mazure.aitestcasegeneration.provider.openai.OpenAiChatModelProvider;
import fr.mazure.aitestcasegeneration.provider.openai.OpenAiModelParameters;

/**
 * Tests for the {@link BatchMode} class.
 */
//@Disabled
public class BatchModeTest {

    /**
     * Basic test for OpenAI.
     * @throws MissingEnvironmentVariable
     */
    @Test
    @Tag("e2e")
    public void testBasicOpenAi() throws MissingEnvironmentVariable {
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
        BatchMode.handleBatch(model, sysPrompt, userPrompt, output, Optional.empty());

        // Then
        Assertions.assertEquals("Paris", outputBuffer.toString().trim());
    }

    /**
     * Basic test for Mistral AI.
     * @throws MissingEnvironmentVariable
     */
    @Test
    @Tag("e2e")
    public void testBasicMistralAi() throws MissingEnvironmentVariable {
        // Given
        final ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        final PrintStream output = new PrintStream(outputBuffer);
        final MistralAiModelParameters parameters = new MistralAiModelParameters("mistral-large-latest",
                                                                                Optional.empty(),
                                                                                "MISTRALAI_API_KEY",
                                                                                Optional.empty(),
                                                                                Optional.empty(),
                                                                                Optional.empty());
        final ChatModel model = MistralAiChatModelProvider.createChatModel(parameters);
        final Optional<String> sysPrompt = Optional.of("You must answer in one word, with no punctuation.");
        final String userPrompt = "What is the capital of France?";

        // When
        BatchMode.handleBatch(model, sysPrompt, userPrompt, output, Optional.empty());

        // Then
        Assertions.assertEquals("Paris", outputBuffer.toString().trim());
    }

    /**
     * Basic test for Anthropic.
     * @throws MissingEnvironmentVariable
     */
    @Test
    @Tag("e2e")
    public void testBasicAnthropic() throws MissingEnvironmentVariable {
        // Given
        final ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        final PrintStream output = new PrintStream(outputBuffer);
        final AnthropicModelParameters parameters = new AnthropicModelParameters("claude-3-haiku-20240307",
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
        BatchMode.handleBatch(model, sysPrompt, userPrompt, output, Optional.empty());

        // Then
        Assertions.assertEquals("Paris", outputBuffer.toString().trim());
    }

    /**
     * Basic test for Google AI Gemini.
     * @throws MissingEnvironmentVariable
     */
    @Test
    @Tag("e2e")
    public void testBasicGemini() throws MissingEnvironmentVariable {
        // Given
        final ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        final PrintStream output = new PrintStream(outputBuffer);
        final GoogleGeminiModelParameters parameters = new GoogleGeminiModelParameters("gemini-1.5-flash",
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
        BatchMode.handleBatch(model, sysPrompt, userPrompt, output, Optional.empty());

        // Then
        Assertions.assertEquals("Paris", outputBuffer.toString().trim());
    }

    /**
     * Basic test for custom model.
     * @throws URISyntaxException
     * @throws MalformedURLException
     * @throws MissingEnvironmentVariable
     */
    @Test
    @Tag("e2e")
    public void testBasicCustom() throws MalformedURLException, URISyntaxException, MissingEnvironmentVariable {
        // Given
        final ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        final PrintStream output = new PrintStream(outputBuffer);
        final ByteArrayOutputStream logBuffer = new ByteArrayOutputStream();
        final PrintStream log = new PrintStream(logBuffer);
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
                                                                           Optional.empty(),
                                                                           Optional.empty());
        final ChatModel model = CustomChatModelProvider.createChatModel(parameters, log);
        final Optional<String> sysPrompt = Optional.of("You must answer in one word.");
        final String userPrompt = "What is the capital of France?";

        // When
        BatchMode.handleBatch(model, sysPrompt, userPrompt, output, Optional.empty());

        // Then
        Assertions.assertEquals("Paris", outputBuffer.toString().trim());
    }
}
