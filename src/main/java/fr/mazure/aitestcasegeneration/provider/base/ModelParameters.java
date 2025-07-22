package fr.mazure.aitestcasegeneration.provider.base;

import java.net.URL;
import java.util.Optional;

    /**
     * Parameters for model providers.
     *
     * @param modelName      the name of the model
     * @param baseUrl        the base URL of the provider
     * @param apiKeyEnvVar   the name of the environment variable that contains the API key
     */
public class ModelParameters {
    private final String modelName;
    private final Optional<URL> baseUrl;
    private final String apiKeyEnvVar;

    public ModelParameters(final String modelName,
                           final Optional<URL> baseUrl,
                           final String apiKeyEnvVar) {
        this.modelName = modelName;
        this.apiKeyEnvVar = apiKeyEnvVar;
        this.baseUrl = baseUrl;
    }

    public String getModelName() {
        return this.modelName;
    }

    public Optional<URL> getBaseUrl() {
        return this.baseUrl;
    }

    public String getApiKeyEnvironmentVariableName() {
        return this.apiKeyEnvVar;
    }
}
