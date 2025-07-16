package fr.mazure.aitestcasegeneration.provider.custom;

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
 * Parameters for the custom model provider.
 * 
 * @param modelName           the name of the model
 * @param url                 the URL of the provider
 * @param apiKeyEnvVar        the name of the environment variable containing the API key
 */
public class CustomModelParameters extends ModelParameters {

    public CustomModelParameters(final String modelName,
                                 final URL url,
                                 final String apiKeyEnvVar) {
        super(modelName, Optional.of(url), apiKeyEnvVar);
    }

    /**
     * Load parameters from a YAML file and create a new CustomModelParameters instance.
     * 
     * @param yamlFilePath the path to the YAML file containing the parameters
     * @return a new CustomModelParameters instance with the parameters from the YAML file
     * @throws IOException if there is an error reading the file
     * @throws MissingModelParameter if a compulsory parameter is missing
     * @throws InvalidModelParameter if a parameter has an incorrect value
     */
    public static CustomModelParameters loadFromFile(final Path yamlFilePath) throws IOException, MissingModelParameter, InvalidModelParameter {
        try (final InputStream inputStream = new FileInputStream(yamlFilePath.toFile())) {
            final Yaml yaml = new Yaml();
            final ParameterMap parameterMap = new ParameterMap(yaml.load(inputStream));
            return new CustomModelParameters(parameterMap.getString("modelName"),
                                             parameterMap.getUrl("url"),
                                             parameterMap.getString("apiKeyEnvVar"));
        }
    }
}
