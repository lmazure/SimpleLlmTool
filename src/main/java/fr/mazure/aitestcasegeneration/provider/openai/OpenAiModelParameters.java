package fr.mazure.aitestcasegeneration.provider.openai;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import org.yaml.snakeyaml.Yaml;

import fr.mazure.aitestcasegeneration.provider.base.InvalidModelParameter;
import fr.mazure.aitestcasegeneration.provider.base.MissingModelParameter;
import fr.mazure.aitestcasegeneration.provider.base.ModelParameters;
import fr.mazure.aitestcasegeneration.provider.base.ParameterMap;

    /**
     * Parameters for the OpenAI model provider.
     * 
     * @param modelName           the name of the model
     * @param url                 the base URL of the provider
     * @param apiKeyEnvVar        the name of the environment variable containing the API key
     * @param organizationId      the ID of the organization containing the model
     * @param projectId           the ID of the project containing the model
     * @param temperature         the temperature of the model
     * @param seed                the random seed of the model
     * @param topP                the top P value of the model
     * @param maxCompletionTokens the maximum number of tokens the model should generate
     */
public class OpenAiModelParameters extends ModelParameters{
    
    private final Optional<String> organizationId;
    private final Optional<String> projectId;
    private final Optional<Double> temperature;
    private final Optional<Integer> seed;
    private final Optional<Double> topP;
    private final Optional<Integer> maxCompletionTokens;

    public OpenAiModelParameters(final String modelName,
                                 final Optional<URL> url,
                                 final String apiKeyEnvVar,
                                 final Optional<String> organizationId,
                                 final Optional<String> projectId,
                                 final Optional<Double> temperature,
                                 final Optional<Integer> seed,
                                 final Optional<Double> topP,
                                 final Optional<Integer> maxCompletionTokens) {
        super(modelName, url, apiKeyEnvVar);
        this.organizationId = organizationId;
        this.projectId = projectId;
        this.temperature = temperature;
        this.seed = seed;
        this.topP = topP;
        this.maxCompletionTokens = maxCompletionTokens;
    }

    public Optional<String> getOrganizationId() {
        return this.organizationId;
    }

    public Optional<String> getProjectId() {
        return this.projectId;
    }

    public Optional<Double> getTemperature() {
        return this.temperature;
    }

    public Optional<Integer> getSeed() {
        return this.seed;
    }

    public Optional<Double> getTopP() {
        return this.topP;
    }

    public Optional<Integer> getMaxCompletionTokens() {
        return this.maxCompletionTokens;
    }
    
    /**
     * Load parameters from a YAML file and create a new OpenAiModelParameters instance.
     * 
     * @param yamlFilePath the path to the YAML file containing the parameters
     * @return a new OpenAiModelParameters instance with the parameters from the YAML file
     * @throws IOException if there is an error reading the file
     * @throws MissingModelParameter if a compulsory parameter is missing
     * @throws InvalidModelParameter if a parameter has an incorrect value
     */
    public static OpenAiModelParameters loadFromFile(final Path yamlFilePath) throws IOException, MissingModelParameter, InvalidModelParameter {
        try (final InputStream inputStream = new FileInputStream(yamlFilePath.toFile())) {
            final Yaml yaml = new Yaml();
            final Map<String, Object> yamlData = yaml.load(inputStream);
            final ParameterMap parameterMap = new ParameterMap(yamlData);
            
            // Extract required parameters
            final String modelName = parameterMap.getString("modelName");
            final String apiKeyEnvVar = parameterMap.getString("apiKeyEnvVar");

            // Extract optional parameters
            final Optional<URL> url = parameterMap.getOptionalUrl("url");

            final Optional<String> organizationId = parameterMap.getOptionalString("organizationId");

            final Optional<String> projectId = parameterMap.getOptionalString("projectId");

            final Optional<Double> temperature = parameterMap.getOptionalDouble("temperature");

            final Optional<Integer> seed = parameterMap.getOptionalInteger("seed");

            final Optional<Double> topP = parameterMap.getOptionalDouble("topP");

            final Optional<Integer> maxCompletionTokens = parameterMap.getOptionalInteger("maxCompletionTokens");

            return new OpenAiModelParameters(modelName,
                                             url,
                                             apiKeyEnvVar,
                                             organizationId,
                                             projectId,
                                             temperature,
                                             seed,
                                             topP,
                                             maxCompletionTokens);
        }
    }
}
