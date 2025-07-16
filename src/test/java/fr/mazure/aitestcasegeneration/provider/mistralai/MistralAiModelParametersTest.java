package fr.mazure.aitestcasegeneration.provider.mistralai;

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
 * Tests for the {@link MistralAiModelParameters} class.
 */
public class MistralAiModelParametersTest {

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
        final MistralAiModelParameters parameters = MistralAiModelParameters.loadFromFile(tempConfigPath);
        
        // Then
        assertEquals("mistral-large-latest", parameters.getModelName());
        assertEquals("MISTRALAI_API_KEY", parameters.getApiKeyEnvironmentVariableName());
        assertEquals(new URI("https://api.mistral.ai/v1").toURL(), parameters.getBaseUrl().get());
        assertEquals(0.7, parameters.getTemperature().get());
        assertEquals(0.95, parameters.getTopP().get());
        assertEquals(2048, parameters.getMaxTokens().get());
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
                modelName: mistral-small-latest
                apiKeyEnvVar: MISTRAL_API_KEY
                """;
        final Path tempConfigPath = tempDir.resolve(("minimal-mistral-config.yaml"));
        Files.writeString(tempConfigPath, configContent);
        
        // When
        final MistralAiModelParameters parameters = MistralAiModelParameters.loadFromFile(tempConfigPath);
        
        // Then
        assertEquals("mistral-small-latest", parameters.getModelName());
        assertEquals("MISTRAL_API_KEY", parameters.getApiKeyEnvironmentVariableName());
        assertFalse(parameters.getBaseUrl().isPresent());
        assertFalse(parameters.getTemperature().isPresent());
        assertFalse(parameters.getTopP().isPresent());
        assertFalse(parameters.getMaxTokens().isPresent());
    }
    
    /**
     * Test loading a non-existent file.
     */
    @Test
    public void testLoadFromNonExistentFile() {
        // Given
        final Path nonExistentPath = Paths.get("non-existent-file.yaml");
        
        // When/Then
        assertThrows(IOException.class, () -> MistralAiModelParameters.loadFromFile(nonExistentPath));
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
                modelName: mistral-medium-latest
                apiKeyEnvVar: MISTRAL_API_KEY
                baseUrl: invalid-url
                """);
        
        // When/Then
        assertThrows(InvalidModelParameter.class, () -> MistralAiModelParameters.loadFromFile(invalidConfigPath));
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
                modelName: mistral-medium-latest
                apiKeyEnvVar: MISTRAL_API_KEY
                temperature: not-a-number
                """);
        
        // When/Then
        assertThrows(InvalidModelParameter.class, () -> MistralAiModelParameters.loadFromFile(invalidConfigPath));
    }
}
