package fr.mazure.aitestcasegeneration;

import java.util.Arrays;

/**
 * Enum for the providers of the application.
 *
 * The providers are as follows:
 * <ul>
 *     <li>{@link #OPENAI}: OpenAI</li>
 *     <li>{@link #MISTRAL_AI}: Mistral AI</li>
 *     <li>{@link #ANTHROPIC}: Anthropic</li>
 *     <li>{@link #GOOGLE_GEMINI}: Google Gemini</li>
 *     <li>{@link #CUSTOM}: Custom</li>
 *     <li>{@link #MOCK}: Mock</li>
 * </ul>
 */
public enum ProviderEnum {

    OPENAI("OpenAI"),
    MISTRAL_AI("Mistral AI"),
    ANTHROPIC("Anthropic"),
    GOOGLE_GEMINI("Google Gemini"),
    CUSTOM("custom"),
    MOCK("mock");

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
