package fr.mazure.simplellmtool.provider.mistralai;

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
 * Tests for the {@link MistralAiModelParameters} class.
 */
class MistralAiModelParametersTest {

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
                modelName: mistral-large-latest
                apiKeyEnvVar: MISTRALAI_API_KEY
                baseUrl: https://api.mistral.ai/v1
                temperature: 0.7
                topP: 0.95
                maxTokens: 2048
                """;
        final Path tempConfigPath = tempDir.resolve("valid-mistral-config.yaml");
        Files.writeString(tempConfigPath, configContent);

        // When
        final MistralAiModelParameters parameters = MistralAiModelParameters.loadFromFile(tempConfigPath, Optional.empty());

        // Then
        Assertions.assertEquals("mistral-large-latest", parameters.getModelName());
        Assertions.assertEquals("MISTRALAI_API_KEY", parameters.getApiKeyEnvironmentVariableName());
        Assertions.assertEquals(new URI("https://api.mistral.ai/v1").toURL(), parameters.getBaseUrl().get());
        Assertions.assertEquals(0.7, parameters.getTemperature().get());
        Assertions.assertEquals(0.95, parameters.getTopP().get());
        Assertions.assertEquals(2048, parameters.getMaxTokens().get());
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
                                                                                   InvalidModelParameter {
        // Given
        final String configContent = """
                modelName: mistral-small-latest
                apiKeyEnvVar: MISTRAL_API_KEY
                """;
        final Path tempConfigPath = tempDir.resolve(("minimal-mistral-config.yaml"));
        Files.writeString(tempConfigPath, configContent);

        // When
        final MistralAiModelParameters parameters = MistralAiModelParameters.loadFromFile(tempConfigPath, Optional.empty());

        // Then
        Assertions.assertEquals("mistral-small-latest", parameters.getModelName());
        Assertions.assertEquals("MISTRAL_API_KEY", parameters.getApiKeyEnvironmentVariableName());
        Assertions.assertFalse(parameters.getBaseUrl().isPresent());
        Assertions.assertFalse(parameters.getTemperature().isPresent());
        Assertions.assertFalse(parameters.getTopP().isPresent());
        Assertions.assertFalse(parameters.getMaxTokens().isPresent());
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
                                                                                     InvalidModelParameter {
        // Given
        final String configContent = """
                modelName: mistral-small-latest
                apiKeyEnvVar: MISTRAL_API_KEY
                """;
        final Path tempConfigPath = tempDir.resolve(("minimal-mistral-config.yaml"));
        Files.writeString(tempConfigPath, configContent);

        // When
        final MistralAiModelParameters parameters = MistralAiModelParameters.loadFromFile(tempConfigPath, Optional.of("mistral-large-latest"));

        // Then
        Assertions.assertEquals("mistral-large-latest", parameters.getModelName());
        Assertions.assertEquals("MISTRAL_API_KEY", parameters.getApiKeyEnvironmentVariableName());
        Assertions.assertFalse(parameters.getBaseUrl().isPresent());
        Assertions.assertFalse(parameters.getTemperature().isPresent());
        Assertions.assertFalse(parameters.getTopP().isPresent());
        Assertions.assertFalse(parameters.getMaxTokens().isPresent());
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
        Assertions.assertThrows(IOException.class, () -> MistralAiModelParameters.loadFromFile(nonExistentPath, Optional.empty()));
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
                modelName: mistral-medium-latest
                apiKeyEnvVar: MISTRAL_API_KEY
                baseUrl: invalid-url
                """);

        // When/Then
        Assertions.assertThrows(InvalidModelParameter.class, () -> MistralAiModelParameters.loadFromFile(invalidConfigPath, Optional.empty()));
    }

    /**
     * Test loading a file with invalid numeric values.
     *
     * @param tempDir temporary directory where to write the configuration file
     *
     * @throws IOException if there is an error creating or writing to the file
     */
    @SuppressWarnings("static-method")
	@Test
    public void testLoadFromFileWithInvalidNumericValues(@TempDir final Path tempDir) throws IOException {
        // Given
        final Path invalidConfigPath = tempDir.resolve("invalid-numeric-config.yaml");
        Files.writeString(invalidConfigPath, """
                modelName: mistral-medium-latest
                apiKeyEnvVar: MISTRAL_API_KEY
                temperature: not-a-number
                """);

        // When/Then
        Assertions.assertThrows(InvalidModelParameter.class, () -> MistralAiModelParameters.loadFromFile(invalidConfigPath, Optional.empty()));
    }
}
