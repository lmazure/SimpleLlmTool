package fr.mazure.aitestcasegeneration.provider.googlegemini;

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

import fr.mazure.aitestcasegeneration.provider.base.InvalidModelParameter;
import fr.mazure.aitestcasegeneration.provider.base.MissingModelParameter;

/**
 * Tests for the {@link GoogleGeminiModelParameters} class.
 */
public class GoogleGeminiModelParametersTest {

    /**
     * Test loading a valid configuration file with all parameters.
     *
     * @throws IOException if there is an error reading the file
     * @throws URISyntaxException if the expected URL is invalid (i.e. you screwed up the test itself)
     * @throws MissingModelParameter if a compulsory parameter is missing
     * @throws InvalidModelParameter if a parameter has an incorrect value
     */
    @Test
    public void testLoadFromFileWithAllParameters(@TempDir final Path tempDir) throws IOException, MissingModelParameter, InvalidModelParameter, URISyntaxException {
        // Given
        final String configContent = """
                modelName: gemini-1.5-flash
                apiKeyEnvVar: GEMINI_API_KEY
                baseUrl: https://generativelanguage.googleapis.com
                temperature: 0.7
                topP: 0.95
                topK: 40
                maxTokens: 4096
                """;
        final Path tempConfigPath = tempDir.resolve("valid-gemini-config.yaml");
        Files.writeString(tempConfigPath, configContent);

        // When
        final GoogleGeminiModelParameters parameters = GoogleGeminiModelParameters.loadFromFile(tempConfigPath, Optional.empty());

        // Then
        Assertions.assertEquals("gemini-1.5-flash", parameters.getModelName());
        Assertions.assertEquals("GEMINI_API_KEY", parameters.getApiKeyEnvironmentVariableName());
        Assertions.assertEquals(new URI("https://generativelanguage.googleapis.com").toURL(), parameters.getBaseUrl().get());
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
    @Test
    public void testLoadFromFileWithMinimalParameters(@TempDir final Path tempDir) throws IOException, MissingModelParameter, InvalidModelParameter {
        // Given
        final String configContent = """
                modelName: gemini-1.5-flash
                apiKeyEnvVar: GEMINI_API_KEY
                """;
        final Path tempConfigPath = tempDir.resolve("minimal-gemini-config.yaml");
        Files.writeString(tempConfigPath, configContent);

        // When
        final GoogleGeminiModelParameters parameters = GoogleGeminiModelParameters.loadFromFile(tempConfigPath, Optional.empty());

        // Then
        Assertions.assertEquals("gemini-1.5-flash", parameters.getModelName());
        Assertions.assertEquals("GEMINI_API_KEY", parameters.getApiKeyEnvironmentVariableName());
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
    @Test
    public void testLoadFromFileWithOverriddenModelName(@TempDir final Path tempDir) throws IOException, MissingModelParameter, InvalidModelParameter {
        // Given
        final String configContent = """
                modelName: gemini-1.0-pro
                apiKeyEnvVar: GEMINI_API_KEY
                """;
        final Path tempConfigPath = tempDir.resolve("minimal-gemini-config.yaml");
        Files.writeString(tempConfigPath, configContent);

        // When
        final GoogleGeminiModelParameters parameters = GoogleGeminiModelParameters.loadFromFile(tempConfigPath, Optional.of("gemini-1.5-flash"));

        // Then
        Assertions.assertEquals("gemini-1.5-flash", parameters.getModelName());
        Assertions.assertEquals("GEMINI_API_KEY", parameters.getApiKeyEnvironmentVariableName());
        Assertions.assertFalse(parameters.getBaseUrl().isPresent());
        Assertions.assertFalse(parameters.getTemperature().isPresent());
        Assertions.assertFalse(parameters.getTopP().isPresent());
        Assertions.assertFalse(parameters.getTopK().isPresent());
        Assertions.assertFalse(parameters.getMaxTokens().isPresent());
    }

    /**
     * Test loading a non-existent file.
     */
    @Test
    public void testLoadFromNonExistentFile() {
        // Given
        final Path nonExistentPath = Paths.get("non-existent-file.yaml");

        // When/Then
        Assertions.assertThrows(IOException.class, () -> GoogleGeminiModelParameters.loadFromFile(nonExistentPath, Optional.empty()));
    }

    /**
     * Test loading a file with invalid URL.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if there is an error creating or writing to the file
     */
    @Test
    public void testLoadFromFileWithInvalidUrl(@TempDir final Path tempDir) throws IOException {
        // Given
        final Path invalidConfigPath = tempDir.resolve("invalid-url-config.yaml");
        Files.writeString(invalidConfigPath, """
                modelName: gemini-1.5-flash
                apiKeyEnvVar: GEMINI_API_KEY
                baseUrl: invalid-url
                """);

        // When/Then
        Assertions.assertThrows(InvalidModelParameter.class, () -> GoogleGeminiModelParameters.loadFromFile(invalidConfigPath, Optional.empty()));
    }

    /**
     * Test loading a file with invalid numeric values.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if there is an error creating or writing to the file
     */
    @Test
    public void testLoadFromFileWithInvalidNumericValues(@TempDir final Path tempDir) throws IOException {
        // Given
        final Path invalidConfigPath = tempDir.resolve("invalid-numeric-config.yaml");
        Files.writeString(invalidConfigPath, """
                modelName: gemini-1.5-flash
                apiKeyEnvVar: GEMINI_API_KEY
                temperature: not-a-number
                """);

        // When/Then
        Assertions.assertThrows(InvalidModelParameter.class, () -> GoogleGeminiModelParameters.loadFromFile(invalidConfigPath, Optional.empty()));
    }
}
