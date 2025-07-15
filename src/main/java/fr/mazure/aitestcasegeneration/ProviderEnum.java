package fr.mazure.aitestcasegeneration;

import java.util.Arrays;

public enum ProviderEnum {

    OPENAI("OpenAI"),
    MISTRAL_AI("Mistral AI"),
    CUSTOM("custom");

    private final String name;

    ProviderEnum(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
    
    public static ProviderEnum fromString(final String text) {
        return Arrays.stream(ProviderEnum.values())
                     .filter(p -> p.name.equals(text))
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException("Unknown provider: " + text));
    }
}
