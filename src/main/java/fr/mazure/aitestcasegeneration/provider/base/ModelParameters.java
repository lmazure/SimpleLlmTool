package fr.mazure.aitestcasegeneration.provider.base;

import java.net.URI;
import java.net.URL;
import java.util.Optional;

    /**
     * Parameters for model providers.
     * 
     * @param modelName      the name of the model
     * @param url            the base URL of the provider
     * @param apiKeyEnvVar   the name of the environment variable that contains the API key
     */
public class ModelParameters {
    private final String modelName;
    private final Optional<URL> url;
    private final String apiKeyEnvVar;

    public ModelParameters(final String modelName,
                           final Optional<URL> url,
                           final String apiKeyEnvVar) {
        this.modelName = modelName;
        this.apiKeyEnvVar = apiKeyEnvVar;
        this.url = url;
    }

    public String getModelName() {
        return modelName;
    }

    public Optional<URL> getUrl() {
        return url;
    }

    public String getApiKeyEnvironmentVariableName() {
        return apiKeyEnvVar;
    }
}
