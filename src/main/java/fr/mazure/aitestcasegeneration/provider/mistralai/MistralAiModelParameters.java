package fr.mazure.aitestcasegeneration.provider.mistralai;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

import org.yaml.snakeyaml.Yaml;

import fr.mazure.aitestcasegeneration.provider.base.InvalidModelParameter;
import fr.mazure.aitestcasegeneration.provider.base.MissingModelParameter;
import fr.mazure.aitestcasegeneration.provider.base.ModelParameters;
import fr.mazure.aitestcasegeneration.provider.base.ParameterMap;

/**
 * Parameters for the Mistral AI model provider.
 *
 * @param modelName           the name of the model
 * @param baseUrl             the base URL of the provider
 * @param apiKeyEnvVar        the name of the environment variable containing the API key
 * @param temperature         the temperature of the model
 * @param topP                the top P value of the model
 * @param maxTokens           the maximum number of tokens the model should generate
 */
public class MistralAiModelParameters extends ModelParameters {

    private final Optional<Double> temperature;
    private final Optional<Double> topP;
    private final Optional<Integer> maxTokens;

    public MistralAiModelParameters(final String modelName,
                                    final Optional<URL> baseUrl,
                                    final String apiKeyEnvVar,
                                    final Optional<Double> temperature,
                                    final Optional<Double> topP,
                                    final Optional<Integer> maxTokens) {
        super(modelName, baseUrl, apiKeyEnvVar);
        this.temperature = temperature;
        this.topP = topP;
        this.maxTokens = maxTokens;
    }

    public Optional<Double> getTemperature() {
        return this.temperature;
    }

    public Optional<Double> getTopP() {
        return this.topP;
    }

    public Optional<Integer> getMaxTokens() {
        return this.maxTokens;
    }

    /**
     * Load parameters from a YAML file and create a new MistralAiModelParameters instance.
     *
     * @param yamlFilePath the path to the YAML file containing the parameters
     * @param overridingModelName the name of the model to override the one in the YAML file
     * @return a new MistralAiModelParameters instance with the parameters from the YAML file
     * @throws IOException if there is an error reading the file
     * @throws MissingModelParameter if a compulsory parameter is missing
     * @throws InvalidModelParameter if a parameter has an incorrect value
     */
    public static MistralAiModelParameters loadFromFile(final Path yamlFilePath,
                                                        final Optional<String> overridingModelName) throws IOException, MissingModelParameter, InvalidModelParameter {
        try (final InputStream inputStream = new FileInputStream(yamlFilePath.toFile())) {
            final Yaml yaml = new Yaml();
            final ParameterMap parameterMap = new ParameterMap(yaml.load(inputStream));
            return new MistralAiModelParameters(overridingModelName.orElse(parameterMap.getString("modelName")),
                                                parameterMap.getOptionalUrl("baseUrl"),
                                                parameterMap.getString("apiKeyEnvVar"),
                                                parameterMap.getOptionalDouble("temperature"),
                                                parameterMap.getOptionalDouble("topP"),
                                                parameterMap.getOptionalInteger("maxTokens"));
        }
    }
}
