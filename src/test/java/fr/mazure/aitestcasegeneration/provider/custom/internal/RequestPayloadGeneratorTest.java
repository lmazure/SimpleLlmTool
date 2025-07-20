package fr.mazure.aitestcasegeneration.provider.custom.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class RequestPayloadGeneratorTest {

    @Test
    @DisplayName("Should generate template with multiple messages")
    void testGenerateMultipleMessages() {
        // Given
        final String template = """
            {
              "messages": [
                {{#each messages}}{
                  "role": "{{#if (isSystem role)}}system{{/if}}{{#if (isUser role)}}user{{/if}}{{#if (isModel role)}}assistant{{/if}}",
                  "content": "{{content}}"
                }{{#unless @last}},
                {{/unless}}{{/each}}
              ]
            }
            """;

        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(Role.SYSTEM, "You are a helpful assistant"),
            new MessageRound(Role.USER, "What is the weather?"),
            new MessageRound(Role.MODEL, "I don't have access to weather data")
        );

        // When
        final String result = RequestPayloadGenerator.generate(template, messages);

        // Then
        final String expectedResult = """
                {
                  "messages": [
                    {
                      "role": "system",
                      "content": "You are a helpful assistant"
                    },
                    {
                      "role": "user",
                      "content": "What is the weather?"
                    },
                    {
                      "role": "assistant",
                      "content": "I don't have access to weather data"
                    }
                  ]
                }
                """;
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    @DisplayName("Should handle empty messages list")
    void testGenerateEmptyMessages() {
        // Given
        final String template = "Messages: {{#messages}}{{content}}{{/messages}}";
        final List<MessageRound> messages = Collections.emptyList();

        // When
        final String result = RequestPayloadGenerator.generate(template, messages);

        // Then
        Assertions.assertEquals("Messages: ", result);
    }

    @Test
    @DisplayName("Should handle template without message placeholders")
    void testGenerateTemplateWithoutPlaceholders() {
        // Given
        final String template = "This is a static template without placeholders";
        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(Role.USER, "Hello")
        );

        // When
        final String result = RequestPayloadGenerator.generate(template, messages);

        // Then
        Assertions.assertEquals("This is a static template without placeholders", result);
    }

    @Test
    @DisplayName("Should handle special characters in messages")
    void testGenerateWithSpecialCharacters() {
        // Given
        final String template = "Message: {{#messages}}{{content}}{{/messages}}";
        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(Role.USER, "Hello \"world\" with 'quotes' and \n newlines")
        );

        // When
        final String result = RequestPayloadGenerator.generate(template, messages);

        // Then
        Assertions.assertEquals("Message: Hello \\\"world\\\" with 'quotes' and \\n newlines", result);
    }

    @Test
    @DisplayName("Should throw runtime exception for invalid template syntax")
    void testGenerateInvalidTemplate() {
        // Given
        final String template = "Hello {{#messages}}{{role}"; // Missing closing tag
        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(Role.USER, "Hello")
        );

        // When & Then
        RuntimeException exception = Assertions.assertThrows(
            RuntimeException.class,
            () -> RequestPayloadGenerator.generate(template, messages)
        );
        Assertions.assertEquals("Failed to process Handlebars template", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle empty template")
    void testGenerateEmptyTemplate() {
        // Given
        final String template = "";
        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(Role.USER, "Hello")
        );

        // When
        final String result = RequestPayloadGenerator.generate(template, messages);

        // Then
        Assertions.assertEquals("", result);
    }
}
