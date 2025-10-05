package fr.mazure.simplellmtool.provider.anthropic;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import fr.mazure.simplellmtool.provider.base.InvalidModelParameter;
import fr.mazure.simplellmtool.provider.base.MissingModelParameter;

/**
 * Tests for the {@link AnthropicModelParameters} class.
 */
class AnthropicModelParametersTest {

    /**
     * Test loading a valid configuration file with all parameters.
     *
     * @throws IOException if there is an error reading the file
     * @throws URISyntaxException if the expected URL is invalid (i.e. you screwed up the test itself)
     * @throws MissingModelParameter if a compulsory parameter is missing
     * @throws InvalidModelParameter if a parameter has an incorrect value
     */
    @SuppressWarnings("static-method")
    @Test
    void testLoadFromFileWithAllParameters(@TempDir final Path tempDir) throws IOException, MissingModelParameter, InvalidModelParameter, URISyntaxException {
        // Given
        final String configContent = """
                modelName: claude-3-5-sonnet-20240620
                apiKeyEnvVar: ANTHROPIC_API_KEY
                baseUrl: https://api.anthropic.com
                temperature: 0.7
                topP: 0.95
                topK: 40
                maxTokens: 4096
                """;
        final Path tempConfigPath = tempDir.resolve("valid-anthropic-config.yaml");
        Files.writeString(tempConfigPath, configContent);

        // When
        final AnthropicModelParameters parameters = AnthropicModelParameters.loadFromFile(tempConfigPath, Optional.empty());

        // Then
        Assertions.assertEquals("claude-3-5-sonnet-20240620", parameters.getModelName());
        Assertions.assertEquals("ANTHROPIC_API_KEY", parameters.getApiKeyEnvironmentVariableName());
        Assertions.assertEquals(new URI("https://api.anthropic.com").toURL(), parameters.getBaseUrl().get());
        Assertions.assertEquals(0.7, parameters.getTemperature().get());
        Assertions.assertEquals(0.95, parameters.getTopP().get());
        Assertions.assertEquals(40, parameters.getTopK().get());
        Assertions.assertEquals(4096, parameters.getMaxTokens().get());
    }

    /**
     * Test loading a minimal configuration file with only required parameters.
     *
     * @throws IOException if there is an error reading the file
     * @throws MissingModelParameter if a compulsory parameter is missing
     * @throws InvalidModelParameter if a parameter has an incorrect value
     */
    @SuppressWarnings("static-method")
    @Test
    void testLoadFromFileWithMinimalParameters(@TempDir final Path tempDir) throws IOException, MissingModelParameter, InvalidModelParameter {
        // Given
        final String configContent = """
                modelName: claude-3-haiku-20240307
                apiKeyEnvVar: ANTHROPIC_API_KEY
                """;
        final Path tempConfigPath = tempDir.resolve("minimal-anthropic-config.yaml");
        Files.writeString(tempConfigPath, configContent);

        // When
        final AnthropicModelParameters parameters = AnthropicModelParameters.loadFromFile(tempConfigPath, Optional.empty());

        // Then
        Assertions.assertEquals("claude-3-haiku-20240307", parameters.getModelName());
        Assertions.assertEquals("ANTHROPIC_API_KEY", parameters.getApiKeyEnvironmentVariableName());
        Assertions.assertFalse(parameters.getBaseUrl().isPresent());
        Assertions.assertFalse(parameters.getTemperature().isPresent());
        Assertions.assertFalse(parameters.getTopP().isPresent());
        Assertions.assertFalse(parameters.getTopK().isPresent());
        Assertions.assertFalse(parameters.getMaxTokens().isPresent());
    }

    /**
     * Test loading with an overriding model name.
     *
     * @throws IOException if there is an error reading the file
     * @throws MissingModelParameter if a compulsory parameter is missing
     * @throws InvalidModelParameter if a parameter has an incorrect value
     */
    @SuppressWarnings("static-method")
    @Test
    void testLoadFromFileWithOverriddenModelName(@TempDir final Path tempDir) throws IOException, MissingModelParameter, InvalidModelParameter {
        // Given
        final String configContent = """
                modelName: claude-3-haiku-20240307
                apiKeyEnvVar: ANTHROPIC_API_KEY
                """;
        final Path tempConfigPath = tempDir.resolve("minimal-anthropic-config.yaml");
        Files.writeString(tempConfigPath, configContent);

        // When
        final AnthropicModelParameters parameters = AnthropicModelParameters.loadFromFile(tempConfigPath, Optional.of("claude-3-5-sonnet-20240620"));

        // Then
        Assertions.assertEquals("claude-3-5-sonnet-20240620", parameters.getModelName());
        Assertions.assertEquals("ANTHROPIC_API_KEY", parameters.getApiKeyEnvironmentVariableName());
        Assertions.assertFalse(parameters.getBaseUrl().isPresent());
        Assertions.assertFalse(parameters.getTemperature().isPresent());
        Assertions.assertFalse(parameters.getTopP().isPresent());
        Assertions.assertFalse(parameters.getTopK().isPresent());
        Assertions.assertFalse(parameters.getMaxTokens().isPresent());
    }

    /**
     * Test loading a non-existent file.
     */
    @SuppressWarnings("static-method")
    @Test
    public void testLoadFromNonExistentFile() {
        // Given
        final Path nonExistentPath = Paths.get("non-existent-file.yaml");

        // When/Then
        Assertions.assertThrows(IOException.class, () -> AnthropicModelParameters.loadFromFile(nonExistentPath, Optional.empty()));
    }

    /**
     * Test loading a file with invalid URL.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if there is an error creating or writing to the file
     */
    @SuppressWarnings("static-method")
    @Test
    void testLoadFromFileWithInvalidUrl(@TempDir final Path tempDir) throws IOException {
        // Given
        final Path invalidConfigPath = tempDir.resolve("invalid-url-config.yaml");
        Files.writeString(invalidConfigPath, """
                modelName: claude-3-5-sonnet-20240620
                apiKeyEnvVar: ANTHROPIC_API_KEY
                baseUrl: invalid-url
                """);

        // When/Then
        Assertions.assertThrows(InvalidModelParameter.class, () -> AnthropicModelParameters.loadFromFile(invalidConfigPath, Optional.empty()));
    }

    /**
     * Test loading a file with invalid numeric values.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if there is an error creating or writing to the file
     */
    @SuppressWarnings("static-method")
    @Test
    void testLoadFromFileWithInvalidNumericValues(@TempDir final Path tempDir) throws IOException {
        // Given
        final Path invalidConfigPath = tempDir.resolve("invalid-numeric-config.yaml");
        Files.writeString(invalidConfigPath, """
                modelName: claude-3-5-sonnet-20240620
                apiKeyEnvVar: ANTHROPIC_API_KEY
                temperature: not-a-number
                """);

        // When/Then
        Assertions.assertThrows(InvalidModelParameter.class, () -> AnthropicModelParameters.loadFromFile(invalidConfigPath, Optional.empty()));
    }
}
