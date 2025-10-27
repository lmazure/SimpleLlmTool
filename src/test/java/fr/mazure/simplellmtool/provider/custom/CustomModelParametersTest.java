package fr.mazure.simplellmtool.provider.custom;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import fr.mazure.simplellmtool.provider.base.InvalidModelParameter;
import fr.mazure.simplellmtool.provider.base.MissingModelParameter;
import fr.mazure.simplellmtool.provider.custom.internal.CustomChatModel;

/**
 * Tests for the {@link CustomModelParameters} class.
 */
class CustomModelParametersTest {

    /**
     * Test loading a valid configuration file with all parameters.
     *
     * @param tempDir temporary directory where to write the configuration file
     *
     * @throws IOException if there is an error reading the file
     * @throws URISyntaxException if the expected URL is invalid (i.e. you screwed up the test itself)
     * @throws MissingModelParameter if a compulsory parameter is missing
     * @throws InvalidModelParameter if a parameter has an incorrect value
     */
    @SuppressWarnings("static-method")
    @Test
    void testLoadFromFileWithAllParameters(@TempDir final Path tempDir) throws IOException,
                                                                               MissingModelParameter,
                                                                               InvalidModelParameter,
                                                                               URISyntaxException {
        // Given
        final String configContent = """
                modelName: custom-large-latest
                apiKeyEnvVar: CUSTOM_API_KEY
                url: https://api.custom.ai/v1
                payloadTemplate: the_template
                httpHeaders:
                  Authorization: Bearer {{apiKey}}
                answerPath: path_to_answer
                inputTokenPath: path_to_number_of_input_tokens
                outputTokenPath: path_to_number_of_output_tokens
                finishReasonPath: path_to_finish_reason
                finishReasonMappings:
                  string_for_stop: DONE
                  string_for_max_tokens: MAX_TOKENS
                toolCallsPath: path_to_tool_calls
                toolNamePath: path_to_tool_name
                toolArgumentsDictPath: path_to_tool_arguments
                """;
        final Path tempConfigPath = tempDir.resolve("valid-custom-config.yaml");
        Files.writeString(tempConfigPath, configContent);

        // When
        final CustomModelParameters parameters = CustomModelParameters.loadFromFile(tempConfigPath, Optional.empty());

        // Then
        Assertions.assertEquals("custom-large-latest", parameters.getModelName());
        Assertions.assertEquals("CUSTOM_API_KEY", parameters.getApiKeyEnvironmentVariableName());
        Assertions.assertEquals(new URI("https://api.custom.ai/v1").toURL(), parameters.getBaseUrl().get());
        Assertions.assertEquals("the_template", parameters.getPayloadTemplate());
        Assertions.assertEquals(Map.of("Authorization", "Bearer {{apiKey}}"), parameters.getHttpHeaders());
        Assertions.assertEquals("path_to_answer", parameters.getAnswerPath());
        Assertions.assertEquals("path_to_number_of_input_tokens", parameters.getInputTokenPath());
        Assertions.assertEquals("path_to_number_of_output_tokens", parameters.getOutputTokenPath());
        Assertions.assertEquals("path_to_finish_reason", parameters.getFinishReasonPath());
        Assertions.assertEquals(Map.of("string_for_stop", CustomChatModel.FinishingReason.DONE, "string_for_max_tokens", CustomChatModel.FinishingReason.MAX_TOKENS), parameters.getFinishReasonMappings());
        Assertions.assertEquals("path_to_tool_calls", parameters.getToolCallsPath());
        Assertions.assertEquals("path_to_tool_name", parameters.getToolNamePath());
        Assertions.assertTrue(parameters.getToolArgumentsDictPath().isPresent());
        Assertions.assertEquals("path_to_tool_arguments", parameters.getToolArgumentsDictPath().get());
        Assertions.assertFalse(parameters.getToolArgumentsStringPath().isPresent());
    }

    /**
     * Test loading a minimal configuration file with only required parameters.
     *
     * @param tempDir temporary directory where to write the configuration file
     *
     * @throws IOException if there is an error reading the file
     * @throws MissingModelParameter if a compulsory parameter is missing
     * @throws InvalidModelParameter if a parameter has an incorrect value
     */
    @SuppressWarnings("static-method")
    @Test
    void testLoadFromFileWithMinimalParameters(@TempDir final Path tempDir) throws IOException,
                                                                                   MissingModelParameter,
                                                                                   InvalidModelParameter,
                                                                                   URISyntaxException {
        // Given
        final String configContent = """
                modelName: custom-small-latest
                apiKeyEnvVar: CUSTOM_API_KEY
                url: https://api.custom.ai/v1
                payloadTemplate: the_template
                httpHeaders:
                  Authorization: Bearer {{apiKey}}
                answerPath: path_to_answer
                inputTokenPath: path_to_number_of_input_tokens
                outputTokenPath: path_to_number_of_output_tokens
                finishReasonPath: path_to_finish_reason
                finishReasonMappings:
                  string_for_stop: DONE
                  string_for_max_tokens: MAX_TOKENS
                toolCallsPath: path_to_tool_calls
                toolNamePath: path_to_tool_name
                toolArgumentsDictPath: path_to_tool_arguments
                """;
        final Path tempConfigPath = tempDir.resolve(("minimal-custom-config.yaml"));
        Files.writeString(tempConfigPath, configContent);

        // When
        final CustomModelParameters parameters = CustomModelParameters.loadFromFile(tempConfigPath, Optional.empty());

        // Then
        Assertions.assertEquals("custom-small-latest", parameters.getModelName());
        Assertions.assertEquals("CUSTOM_API_KEY", parameters.getApiKeyEnvironmentVariableName());
        Assertions.assertEquals(new URI("https://api.custom.ai/v1").toURL(), parameters.getBaseUrl().get());
        Assertions.assertEquals("the_template", parameters.getPayloadTemplate());
        Assertions.assertEquals(Map.of("Authorization", "Bearer {{apiKey}}"), parameters.getHttpHeaders());
        Assertions.assertEquals("path_to_answer", parameters.getAnswerPath());
        Assertions.assertEquals("path_to_number_of_input_tokens", parameters.getInputTokenPath());
        Assertions.assertEquals("path_to_number_of_output_tokens", parameters.getOutputTokenPath());
        Assertions.assertEquals("path_to_finish_reason", parameters.getFinishReasonPath());
        Assertions.assertEquals(Map.of("string_for_stop", CustomChatModel.FinishingReason.DONE, "string_for_max_tokens", CustomChatModel.FinishingReason.MAX_TOKENS), parameters.getFinishReasonMappings());
        Assertions.assertEquals("path_to_tool_calls", parameters.getToolCallsPath());
        Assertions.assertEquals("path_to_tool_name", parameters.getToolNamePath());
        Assertions.assertTrue(parameters.getToolArgumentsDictPath().isPresent());
        Assertions.assertEquals("path_to_tool_arguments", parameters.getToolArgumentsDictPath().get());
        Assertions.assertFalse(parameters.getToolArgumentsStringPath().isPresent());
    }

    /**
     * Test loading with an overriding model name.
     *
     * @param tempDir temporary directory where to write the configuration file
     *
     * @throws IOException if there is an error reading the file
     * @throws MissingModelParameter if a compulsory parameter is missing
     * @throws InvalidModelParameter if a parameter has an incorrect value
     */
    @SuppressWarnings("static-method")
    @Test
    void testLoadFromFileWithOverriddenModelName(@TempDir final Path tempDir) throws IOException,
                                                                                     MissingModelParameter,
                                                                                     InvalidModelParameter,
                                                                                     URISyntaxException {
        // Given
        final String configContent = """
                modelName: custom-small-latest
                apiKeyEnvVar: CUSTOM_API_KEY
                url: https://api.custom.ai/v1
                payloadTemplate: the_template
                httpHeaders:
                  Authorization: Bearer {{apiKey}}
                answerPath: path_to_answer
                inputTokenPath: path_to_number_of_input_tokens
                outputTokenPath: path_to_number_of_output_tokens
                finishReasonPath: path_to_finish_reason
                finishReasonMappings:
                  string_for_stop: DONE
                  string_for_max_tokens: MAX_TOKENS
                toolCallsPath: path_to_tool_calls
                toolNamePath: path_to_tool_name
                toolArgumentsDictPath: path_to_tool_arguments
                """;
        final Path tempConfigPath = tempDir.resolve(("minimal-custom-config.yaml"));
        Files.writeString(tempConfigPath, configContent);

        // When
        final CustomModelParameters parameters = CustomModelParameters.loadFromFile(tempConfigPath, Optional.of("custom-large-latest"));

        // Then
        Assertions.assertEquals("custom-large-latest", parameters.getModelName());
        Assertions.assertEquals("CUSTOM_API_KEY", parameters.getApiKeyEnvironmentVariableName());
        Assertions.assertEquals(new URI("https://api.custom.ai/v1").toURL(), parameters.getBaseUrl().get());
        Assertions.assertEquals("the_template", parameters.getPayloadTemplate());
        Assertions.assertEquals(Map.of("Authorization", "Bearer {{apiKey}}"), parameters.getHttpHeaders());
        Assertions.assertEquals("path_to_answer", parameters.getAnswerPath());
        Assertions.assertEquals("path_to_number_of_input_tokens", parameters.getInputTokenPath());
        Assertions.assertEquals("path_to_number_of_output_tokens", parameters.getOutputTokenPath());
        Assertions.assertEquals("path_to_finish_reason", parameters.getFinishReasonPath());
        Assertions.assertEquals(Map.of("string_for_stop", CustomChatModel.FinishingReason.DONE, "string_for_max_tokens", CustomChatModel.FinishingReason.MAX_TOKENS), parameters.getFinishReasonMappings());
        Assertions.assertEquals("path_to_tool_calls", parameters.getToolCallsPath());
        Assertions.assertEquals("path_to_tool_name", parameters.getToolNamePath());
        Assertions.assertTrue(parameters.getToolArgumentsDictPath().isPresent());
        Assertions.assertEquals("path_to_tool_arguments", parameters.getToolArgumentsDictPath().get());
        Assertions.assertFalse(parameters.getToolArgumentsStringPath().isPresent());
    }

    /**
     * Test loading a non-existent file.
     */
    @SuppressWarnings("static-method")
    @Test
    void testLoadFromNonExistentFile() {
        // Given
        final Path nonExistentPath = Paths.get("non-existent-file.yaml");

        // When/Then
        final Exception exception = Assertions.assertThrows(IOException.class, () -> CustomModelParameters.loadFromFile(nonExistentPath, Optional.empty()));
        Assertions.assertTrue(exception.getMessage().contains("non-existent-file.yaml"));
    }

    /**
     * Test loading a file with invalid URL.
     *
     * @param tempDir temporary directory where to write the configuration file
     *
     * @throws IOException if there is an error creating or writing to the file
     */
    @SuppressWarnings("static-method")
    @Test
    void testLoadFromFileWithInvalidUrl(@TempDir final Path tempDir) throws IOException {
        // Given
        final Path invalidConfigPath = tempDir.resolve("invalid-url-config.yaml");
        Files.writeString(invalidConfigPath, """
                modelName: custom-medium-latest
                apiKeyEnvVar: CUSTOM_API_KEY
                url: invalid-url
                payloadTemplate: the_template
                httpHeaders:
                  Authorization: Bearer {{apiKey}}
                answerPath: path_to_answer
                inputTokenPath: path_to_number_of_input_tokens
                outputTokenPath: path_to_number_of_output_tokens
                finishReasonPath: path_to_finish_reason
                finishReasonMappings:
                  string_for_stop: DONE
                  string_for_max_tokens: MAX_TOKENS
                """);

        // When/Then
        final Exception exception = Assertions.assertThrows(InvalidModelParameter.class, () -> CustomModelParameters.loadFromFile(invalidConfigPath, Optional.empty()));
        Assertions.assertEquals("Invalid model parameter (should be of type URL): url has value \"invalid-url\"", exception.getMessage());
    }

    /**
     * Test loading a file with invalid finish reason mappings.
     *
     * @param tempDir temporary directory where to write the configuration file
     *
     * @throws IOException if there is an error creating or writing to the file
     */
    @SuppressWarnings("static-method")
    @Test
    void testLoadFromFileWithInvalidFinishReasonMappings(@TempDir final Path tempDir) throws IOException {
        // Given
        final Path invalidConfigPath = tempDir.resolve("invalid-finish-reason-mappings-config.yaml");
        Files.writeString(invalidConfigPath, """
                modelName: custom-medium-latest
                apiKeyEnvVar: CUSTOM_API_KEY
                url: https://api.custom.ai/v1
                payloadTemplate: the_template
                httpHeaders:
                  Authorization: Bearer {{apiKey}}
                answerPath: path_to_answer
                inputTokenPath: path_to_number_of_input_tokens
                outputTokenPath: path_to_number_of_output_tokens
                finishReasonPath: path_to_finish_reason
                finishReasonMappings:
                  string_for_stop: DONE
                  string_for_max_tokens: MAX_TOKEN
                """);

        // When/Then
        final Exception exception = Assertions.assertThrows(InvalidModelParameter.class, () -> CustomModelParameters.loadFromFile(invalidConfigPath, Optional.empty()));
        Assertions.assertEquals("Invalid model parameter (should be of type FinishingReason): finishReasonMappings has value \"MAX_TOKEN\"", exception.getMessage());
    }
}
