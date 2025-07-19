package fr.mazure.aitestcasegeneration.provider.custom.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class RequestPayloadGeneratorTest {

    @Test
    @DisplayName("Should generate simple template with single message")
    void testGenerateSimpleTemplate() {
        // Given
        final String template = "Hello {{#messages}}{{messageActor}}: {{message}}{{/messages}}";
        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(MessageActor.USER, "How are you?")
        );

        // When
        final String result = RequestPayloadGenerator.generate(template, messages);

        // Then
        Assertions.assertEquals("Hello USER: How are you?", result);
    }

    @Test
    @DisplayName("Should generate template with multiple messages")
    void testGenerateMultipleMessages() {
        // Given
        final String template = """
            {
              "messages": [
                {{#messages}}
                {
                  "role": "{{messageActor}}",
                  "content": "{{message}}"
                }{{^last}},{{/last}}
                {{/messages}}
              ]
            }
            """;

        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(MessageActor.SYSTEM, "You are a helpful assistant"),
            new MessageRound(MessageActor.USER, "What is the weather?"),
            new MessageRound(MessageActor.MODEL, "I don't have access to weather data")
        );

        // When
        final String result = RequestPayloadGenerator.generate(template, messages);

        // Then
        final String expectedResult = """
                {
                  "messages": [
                    {
                      "role": "SYSTEM",
                      "content": "You are a helpful assistant"
                    },
                    {
                      "role": "USER",
                      "content": "What is the weather?"
                    },
                    {
                      "role": "MODEL",
                      "content": "I don't have access to weather data"
                    },
                  ]
                }
                """;
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    @DisplayName("Should handle empty messages list")
    void testGenerateEmptyMessages() {
        // Given
        final String template = "Messages: {{#messages}}{{messageActor}}: {{message}}{{/messages}}";
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
            new MessageRound(MessageActor.USER, "Hello")
        );

        // When
        final String result = RequestPayloadGenerator.generate(template, messages);

        // Then
        Assertions.assertEquals("This is a static template without placeholders", result);
    }

    @Test
    @DisplayName("Should handle complex JSON template")
    void testGenerateComplexJsonTemplate() {
        // Given
        final String template = """
            {
              "model": "gpt-3.5-turbo",
              "messages": [
                {{#messages}}
                {
                  "role": "{{messageActor}}",
                  "content": "{{message}}"
                }{{#hasNext}},{{/hasNext}}
                {{/messages}}
              ],
              "temperature": 0.7
            }""";

        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(MessageActor.USER, "Hello world")
        );

        // When
        final String result = RequestPayloadGenerator.generate(template, messages);

        // Then
        final String expectedResult = """
                {
                  "model": "gpt-3.5-turbo",
                  "messages": [
                    {
                      "role": "USER",
                      "content": "Hello world"
                    }
                  ],
                  "temperature": 0.7
                }""";
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    @DisplayName("Should handle special characters in messages")
    void testGenerateWithSpecialCharacters() {
        // Given
        final String template = "Message: {{#messages}}{{message}}{{/messages}}";
        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(MessageActor.USER, "Hello \"world\" with 'quotes' and \n newlines")
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
        final String template = "Hello {{#messages}}{{messageActor}"; // Missing closing tag
        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(MessageActor.USER, "Hello")
        );

        // When & Then
        RuntimeException exception = Assertions.assertThrows(
            RuntimeException.class,
            () -> RequestPayloadGenerator.generate(template, messages)
        );
        Assertions.assertEquals("Failed to process Mustache template", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle message with all actor types")
    void testGenerateAllActorTypes() {
        // Given
        final String template = """
            {{#messages}}
            {{messageActor}}: {{message}}
            {{/messages}}""";

        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(MessageActor.SYSTEM, "System message"),
            new MessageRound(MessageActor.USER, "User message"),
            new MessageRound(MessageActor.MODEL, "Model message")
        );

        // When
        final String result = RequestPayloadGenerator.generate(template, messages);

        // Then
        final String expectedResult = """
            SYSTEM: System message
            USER: User message
            MODEL: Model message
            """;
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    @DisplayName("Should handle empty template")
    void testGenerateEmptyTemplate() {
        // Given
        final String template = "";
        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(MessageActor.USER, "Hello")
        );

        // When
        final String result = RequestPayloadGenerator.generate(template, messages);

        // Then
        Assertions.assertEquals("", result);
    }
}
