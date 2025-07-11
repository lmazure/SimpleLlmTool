package fr.mazure.aitestcasegeneration.provider.openai;

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
    public void testLoadFromFileWithAllParameters() throws IOException, MissingModelParameter, InvalidModelParameter, URISyntaxException {
        // Given
        final Path configPath = Paths.get("src/test/resources/valid-openai-config.yaml");
        
        // When
        final OpenAiModelParameters parameters = OpenAiModelParameters.loadFromFile(configPath);
        
        // Then
        assertEquals("gpt-4", parameters.getModelName());
        assertEquals("OPENAI_API_KEY", parameters.getApiKeyEnvironmentVariableName());
        assertEquals(new URI("https://api.openai.com/v1").toURL(), parameters.getUrl().get());
        assertEquals("org-abcd1234", parameters.getOrganizationId().get());
        assertEquals("proj-5678efgh", parameters.getProjectId().get());
        assertEquals(0.7, parameters.getTemperature().get());
        assertEquals(42, parameters.getSeed().get());
        assertEquals(0.95, parameters.getTopP().get());
        assertEquals(2048, parameters.getMaxCompletionTokens().get());
    }
    
    /**
     * Test loading a minimal configuration file with only required parameters.
     * 
     * @throws IOException if there is an error reading the file
     * @throws MissingModelParameter if a compulsory parameter is missing
     * @throws InvalidModelParameter if a parameter has an incorrect value
     */
    @Test
    public void testLoadFromFileWithMinimalParameters() throws IOException, MissingModelParameter, InvalidModelParameter {
        // Given
        final Path configPath = Paths.get("src/test/resources/minimal-openai-config.yaml");
        
        // When
        final OpenAiModelParameters parameters = OpenAiModelParameters.loadFromFile(configPath);
        
        // Then
        assertEquals("gpt-3.5-turbo", parameters.getModelName());
        assertEquals("OPENAI_API_KEY", parameters.getApiKeyEnvironmentVariableName());
        assertFalse(parameters.getUrl().isPresent());
        assertFalse(parameters.getOrganizationId().isPresent());
        assertFalse(parameters.getProjectId().isPresent());
        assertFalse(parameters.getTemperature().isPresent());
        assertFalse(parameters.getSeed().isPresent());
        assertFalse(parameters.getTopP().isPresent());
        assertFalse(parameters.getMaxCompletionTokens().isPresent());
    }
    
    /**
     * Test loading a non-existent file.
     */
    @Test
    public void testLoadFromNonExistentFile() {
        // Given
        final Path nonExistentPath = Paths.get("non-existent-file.yaml");
        
        // When/Then
        assertThrows(IOException.class, () -> OpenAiModelParameters.loadFromFile(nonExistentPath));
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
        Files.writeString(invalidConfigPath, 
                "modelName: gpt-4\n" +
                "apiKeyEnvVar: OPENAI_API_KEY\n" +
                "url: invalid-url");
        
        // When/Then
        assertThrows(InvalidModelParameter.class, () -> OpenAiModelParameters.loadFromFile(invalidConfigPath));
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
        Files.writeString(invalidConfigPath, 
                "modelName: gpt-4\n" +
                "apiKeyEnvVar: OPENAI_API_KEY\n" +
                "temperature: not-a-number");
        
        // When/Then
        assertThrows(InvalidModelParameter.class, () -> OpenAiModelParameters.loadFromFile(invalidConfigPath));
    }
}
