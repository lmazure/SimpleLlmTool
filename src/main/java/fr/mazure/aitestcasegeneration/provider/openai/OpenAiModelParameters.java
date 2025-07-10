package fr.mazure.aitestcasegeneration.provider.openai;

import java.net.URI;
import java.util.Optional;

import fr.mazure.aitestcasegeneration.provider.base.ModelParameters;

public class OpenAiModelParameters extends ModelParameters{
    
    public OpenAiModelParameters(final String modelName,
                                 final Optional<URI> url,
                                 final String apiKeyEnvVar) {
        super(modelName, url, apiKeyEnvVar);
    }
}
