package fr.mazure.aitestcasegeneration.provider.custom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import fr.mazure.aitestcasegeneration.provider.base.InvalidModelParameter;
import fr.mazure.aitestcasegeneration.provider.base.MissingModelParameter;

/**
 * Tests for the {@link CustomModelParameters} class.
 */
public class CustomModelParametersTest {

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
                modelName: custom-large-latest
                apiKeyEnvVar: CUSTOM_API_KEY
                url: https://api.custom.ai/v1
                """;
        final Path tempConfigPath = tempDir.resolve("valid-custom-config.yaml");
        Files.writeString(tempConfigPath, configContent);

        // When
        final CustomModelParameters parameters = CustomModelParameters.loadFromFile(tempConfigPath);

        // Then
        assertEquals("custom-large-latest", parameters.getModelName());
        assertEquals("CUSTOM_API_KEY", parameters.getApiKeyEnvironmentVariableName());
        assertEquals(new URI("https://api.custom.ai/v1").toURL(), parameters.getUrl().get());
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
                modelName: custom-small-latest
                apiKeyEnvVar: CUSTOM_API_KEY
                """;
        final Path tempConfigPath = tempDir.resolve(("minimal-custom-config.yaml"));
        Files.writeString(tempConfigPath, configContent);

        // When
        final CustomModelParameters parameters = CustomModelParameters.loadFromFile(tempConfigPath);

        // Then
        assertEquals("custom-small-latest", parameters.getModelName());
        assertEquals("CUSTOM_API_KEY", parameters.getApiKeyEnvironmentVariableName());
        assertFalse(parameters.getUrl().isPresent());
    }

    /**
     * Test loading a non-existent file.
     */
    @Test
    public void testLoadFromNonExistentFile() {
        // Given
        final Path nonExistentPath = Paths.get("non-existent-file.yaml");

        // When/Then
        assertThrows(IOException.class, () -> CustomModelParameters.loadFromFile(nonExistentPath));
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
                modelName: custom-medium-latest
                apiKeyEnvVar: CUSTOM_API_KEY
                url: invalid-url
                """);

        // When/Then
        assertThrows(InvalidModelParameter.class, () -> CustomModelParameters.loadFromFile(invalidConfigPath));
    }

}
