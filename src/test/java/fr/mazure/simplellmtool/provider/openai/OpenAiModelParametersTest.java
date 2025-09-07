package fr.mazure.simplellmtool.provider.openai;

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
 * Tests for the {@link OpenAiModelParameters} class.
 */
public class OpenAiModelParametersTest {

    /**
     * Test loading a valid configuration file with all parameters.
     *
     * @throws IOException if there is an error reading the file
     * @throws URISyntaxException if the exepcted URL is invalid (i.e. you screwed up the test itself)
     * @throws MissingModelParameter if a compulsory parameter is missing
     * @throws InvalidModelParameter if a parameter has an incorrect value
     */
    @Test
    public void testLoadFromFileWithAllParameters(@TempDir final Path tempDir) throws IOException, MissingModelParameter, InvalidModelParameter, URISyntaxException {
        // Given
        final String configContent = """
                modelName: gpt-4
                apiKeyEnvVar: OPENAI_API_KEY
                baseUrl: https://api.openai.com/v1
                organizationId: org-abcd1234
                projectId: proj-5678efgh
                temperature: 0.7
                seed: 42
                topP: 0.95
                maxCompletionTokens: 2048
                """;
        final Path tempConfigPath = tempDir.resolve("valid-openai-config.yaml");
        Files.writeString(tempConfigPath, configContent);

        // When
        final OpenAiModelParameters parameters = OpenAiModelParameters.loadFromFile(tempConfigPath, Optional.empty());

        // Then
        Assertions.assertEquals("gpt-4", parameters.getModelName());
        Assertions.assertEquals("OPENAI_API_KEY", parameters.getApiKeyEnvironmentVariableName());
        Assertions.assertEquals(new URI("https://api.openai.com/v1").toURL(), parameters.getBaseUrl().get());
        Assertions.assertEquals("org-abcd1234", parameters.getOrganizationId().get());
        Assertions.assertEquals("proj-5678efgh", parameters.getProjectId().get());
        Assertions.assertEquals(0.7, parameters.getTemperature().get());
        Assertions.assertEquals(42, parameters.getSeed().get());
        Assertions.assertEquals(0.95, parameters.getTopP().get());
        Assertions.assertEquals(2048, parameters.getMaxCompletionTokens().get());
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
                modelName: gpt-3.5-turbo
                apiKeyEnvVar: OPENAI_API_KEY
                """;
        final Path tempConfigPath = tempDir.resolve(("minimal-openai-config.yaml"));
        Files.writeString(tempConfigPath, configContent);

        // When
        final OpenAiModelParameters parameters = OpenAiModelParameters.loadFromFile(tempConfigPath, Optional.empty());

        // Then
        Assertions.assertEquals("gpt-3.5-turbo", parameters.getModelName());
        Assertions.assertEquals("OPENAI_API_KEY", parameters.getApiKeyEnvironmentVariableName());
        Assertions.assertFalse(parameters.getBaseUrl().isPresent());
        Assertions.assertFalse(parameters.getOrganizationId().isPresent());
        Assertions.assertFalse(parameters.getProjectId().isPresent());
        Assertions.assertFalse(parameters.getTemperature().isPresent());
        Assertions.assertFalse(parameters.getSeed().isPresent());
        Assertions.assertFalse(parameters.getTopP().isPresent());
        Assertions.assertFalse(parameters.getMaxCompletionTokens().isPresent());
    }

    /**
     * Test loading with an overriding model name.
     *
     * @throws IOException if there is an error reading the file
     * @throws MissingModelParameter if a compulsory parameter is missing
     * @throws InvalidModelParameter if a parameter has an incorrect value
     */
    @Test
    public void testLoadFromFileWithOverriddenModelName(@TempDir final Path tempDir) throws IOException, MissingModelParameter, InvalidModelParameter, URISyntaxException {
        // Given
        final String configContent = """
                modelName: gpt-3.5-turbo
                apiKeyEnvVar: OPENAI_API_KEY
                """;
        final Path tempConfigPath = tempDir.resolve(("minimal-openai-config.yaml"));
        Files.writeString(tempConfigPath, configContent);

        // When
        final OpenAiModelParameters parameters = OpenAiModelParameters.loadFromFile(tempConfigPath, Optional.of("gpt-4"));

        // Then
        Assertions.assertEquals("gpt-4", parameters.getModelName());
        Assertions.assertEquals("OPENAI_API_KEY", parameters.getApiKeyEnvironmentVariableName());
        Assertions.assertFalse(parameters.getBaseUrl().isPresent());
        Assertions.assertFalse(parameters.getOrganizationId().isPresent());
        Assertions.assertFalse(parameters.getProjectId().isPresent());
        Assertions.assertFalse(parameters.getTemperature().isPresent());
        Assertions.assertFalse(parameters.getSeed().isPresent());
        Assertions.assertFalse(parameters.getTopP().isPresent());
        Assertions.assertFalse(parameters.getMaxCompletionTokens().isPresent());
    }

    /**
     * Test loading a non-existent file.
     */
    @Test
    public void testLoadFromNonExistentFile() {
        // Given
        final Path nonExistentPath = Paths.get("non-existent-file.yaml");

        // When/Then
        Assertions.assertThrows(IOException.class, () -> OpenAiModelParameters.loadFromFile(nonExistentPath, Optional.empty()));
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
                modelName: gpt-4
                apiKeyEnvVar: OPENAI_API_KEY
                baseUrl: invalid-url
                """);

        // When/Then
        Assertions.assertThrows(InvalidModelParameter.class, () -> OpenAiModelParameters.loadFromFile(invalidConfigPath, Optional.empty()));
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
                modelName: gpt-4
                apiKeyEnvVar: OPENAI_API_KEY
                temperature: not-a-number
                """);

        // When/Then
        Assertions.assertThrows(InvalidModelParameter.class, () -> OpenAiModelParameters.loadFromFile(invalidConfigPath, Optional.empty()));
    }
}
