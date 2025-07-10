package fr.mazure.aitestcasegeneration.provider.openai;

import java.net.URI;
import java.util.Optional;

import fr.mazure.aitestcasegeneration.provider.base.ModelParameters;

    /**
     * Parameters for the OpenAI model provider.
     * 
     * @param modelName           the name of the model
     * @param url                 the URL of the model server (if not using the default)
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
                                 final Optional<URI> url,
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
        return organizationId;
    }

    public Optional<String> getProjectId() {
        return projectId;
    }

    public Optional<Double> getTemperature() {
        return temperature;
    }

    public Optional<Integer> getSeed() {
        return seed;
    }

    public Optional<Double> getTopP() {
        return topP;
    }

    public Optional<Integer> getMaxCompletionTokens() {
        return maxCompletionTokens;
    }
}
