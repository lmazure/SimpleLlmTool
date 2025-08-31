package fr.mazure.simplellmtool.provider.custom.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link RequestPayloadGenerator} class.
 */
class RequestPayloadGeneratorTest {

    @Test
    @DisplayName("Should generate OpenAI payload with multiple messages")
    void testGenerateMultipleMessagesForOpenAi() {
        // Given
        final String template = """
            {
              "model": "gpt-4.1",
              "messages": [
                {{#each messages}}{
                  "role": "{{#if (isSystem role)}}system{{/if}}{{#if (isUser role)}}user{{/if}}{{#if (isModel role)}}assistant{{/if}}",
                  "content": {{convertToJsonString content}}
                }{{#unless @last}},
                {{/unless}}{{/each}}
              ],
              "temperature": 0.7,
              "seed": 42
            }
            """;

        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(Role.SYSTEM, "You are a helpful assistant"),
            new MessageRound(Role.USER, "What is the weather?"),
            new MessageRound(Role.MODEL, "I don't have access to weather data"),
            new MessageRound(Role.USER, "What day is it?"),
            new MessageRound(Role.MODEL, "April fools' day"),
            new MessageRound(Role.USER, "So, tell me a joke!")
        );

        // When
        final String result = RequestPayloadGenerator.generate(template, messages, "my-model-name", "my-secret-API-key");

        // Then
        final String expectedResult = """
                {
                  "model": "gpt-4.1",
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
                    },
                    {
                      "role": "user",
                      "content": "What day is it?"
                    },
                    {
                      "role": "assistant",
                      "content": "April fools' day"
                    },
                    {
                      "role": "user",
                      "content": "So, tell me a joke!"
                    }
                  ],
                  "temperature": 0.7,
                  "seed": 42
                }
                """;
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    @DisplayName("Should generate Google's Gemini payload with multiple messages")
    void testGenerateMultipleMessagesForGoogleGemini() {
        // Given
        final String template = """
                {
                  {{#each messages}}{{#if (isSystem role)}}"system_instruction": {
                    "parts": [
                      {
                        "text": {{convertToJsonString content}}
                      }
                    ]
                  },{{/if}}{{/each}}
                  "contents": [
                    {{#each messages}}{{#if (isUser role)}}{
                      "role": "user",
                      "parts": [
                        {
                          "text": {{convertToJsonString content}}
                        }
                      ]
                    }{{#unless @last}},
                    {{/unless}}{{/if}}{{#if (isModel role)}}{
                      "role": "model",
                      "parts": [
                        {
                          "text": {{convertToJsonString content}}
                        }
                      ]
                    }{{#unless @last}},
                    {{/unless}}{{/if}}{{/each}}
                  ],
                  "generationConfig": {
                    "stopSequences": [
                      "Title"
                    ],
                    "temperature": 1.0,
                    "topP": 0.8,
                    "topK": 10
                  }
                }
                """;

        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(Role.SYSTEM, "You are a helpful assistant"),
            new MessageRound(Role.USER, "What is the weather?"),
            new MessageRound(Role.MODEL, "I don't have access to weather data"),
            new MessageRound(Role.USER, "What day is it?"),
            new MessageRound(Role.MODEL, "April fools' day"),
            new MessageRound(Role.USER, "So, tell me a joke!")
        );

        // When
        final String result = RequestPayloadGenerator.generate(template, messages, "my-model-name", "my-secret-API-key");

        // Then
        final String expectedResult = """
                {
                  "system_instruction": {
                    "parts": [
                      {
                        "text": "You are a helpful assistant"
                      }
                    ]
                  },
                  "contents": [
                    {
                      "role": "user",
                      "parts": [
                        {
                          "text": "What is the weather?"
                        }
                      ]
                    },
                    {
                      "role": "model",
                      "parts": [
                        {
                          "text": "I don't have access to weather data"
                        }
                      ]
                    },
                    {
                      "role": "user",
                      "parts": [
                        {
                          "text": "What day is it?"
                        }
                      ]
                    },
                    {
                      "role": "model",
                      "parts": [
                        {
                          "text": "April fools' day"
                        }
                      ]
                    },
                    {
                      "role": "user",
                      "parts": [
                        {
                          "text": "So, tell me a joke!"
                        }
                      ]
                    }
                  ],
                  "generationConfig": {
                    "stopSequences": [
                      "Title"
                    ],
                    "temperature": 1.0,
                    "topP": 0.8,
                    "topK": 10
                  }
                }
                """;
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    @DisplayName("Should manage model name")
    void testGenerateModelName() {
        // Given
        final String template = """
            {
              "model": "{{modelName}}",
              "messages": [
                {{#each messages}}{
                  "role": "{{#if (isSystem role)}}system{{/if}}{{#if (isUser role)}}user{{/if}}{{#if (isModel role)}}assistant{{/if}}",
                  "content": {{convertToJsonString content}}
                }{{#unless @last}},
                {{/unless}}{{/each}}
              ],
              "temperature": 0.7,
              "seed": 42
            }
            """;

        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(Role.SYSTEM, "You are a helpful assistant"),
            new MessageRound(Role.USER, "What is the weather?"),
            new MessageRound(Role.MODEL, "I don't have access to weather data"),
            new MessageRound(Role.USER, "What day is it?"),
            new MessageRound(Role.MODEL, "April fools' day"),
            new MessageRound(Role.USER, "So, tell me a joke!")
        );

        // When
        final String result = RequestPayloadGenerator.generate(template, messages, "my-model-name", "my-secret-API-key");

        // Then
        final String expectedResult = """
                {
                  "model": "my-model-name",
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
                    },
                    {
                      "role": "user",
                      "content": "What day is it?"
                    },
                    {
                      "role": "assistant",
                      "content": "April fools' day"
                    },
                    {
                      "role": "user",
                      "content": "So, tell me a joke!"
                    }
                  ],
                  "temperature": 0.7,
                  "seed": 42
                }
                """;
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    @DisplayName("Should handle API key in HTTP headers")
    void testGenerateApiKet() {
        // Given
        final String template = "Bearer: {{apiKey}}";

        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(Role.SYSTEM, "You are a helpful assistant"),
            new MessageRound(Role.USER, "What is the weather?"),
            new MessageRound(Role.MODEL, "I don't have access to weather data")
        );

        // When
        final String result = RequestPayloadGenerator.generate(template, messages, "my-model-name", "my-secret-API-key");

        // Then
        Assertions.assertEquals("Bearer: my-secret-API-key", result);
    }

    @Test
    @DisplayName("Does not modify special characters in API key in HTTP headers")
    void testGenerateAPiKeyWithoutChangingCharacters() {
        // Given
        final String template = "Bearer: {{apiKey}}";

        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(Role.SYSTEM, "You are a helpful assistant"),
            new MessageRound(Role.USER, "What is the weather?"),
            new MessageRound(Role.MODEL, "I don't have access to weather data")
        );

        // When
        final String result = RequestPayloadGenerator.generate(template, messages, "my-model-name", "&é~\"#'{([-|è`_\\ç^à@)]");

        // Then
        Assertions.assertEquals("Bearer: &é~\"#'{([-|è`_\\ç^à@)]", result);
    }

    @Test
    @DisplayName("Should handle empty messages list")
    void testGenerateEmptyMessages() {
        // Given
        final String template = "Messages: {{#messages}}{{convertToJsonString content}}{{/messages}}";
        final List<MessageRound> messages = Collections.emptyList();

        // When
        final String result = RequestPayloadGenerator.generate(template, messages, "my-model-name", "my-secret-API-key");

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
        final String result = RequestPayloadGenerator.generate(template, messages, "my-model-name", "my-secret-API-key");

        // Then
        Assertions.assertEquals("This is a static template without placeholders", result);
    }

    @Test
    @DisplayName("Should handle special characters in messages")
    void testGenerateWithSpecialCharacters() {
        // Given
        final String template = "Message: {{#messages}}{{convertToJsonString content}}{{/messages}}";
        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(Role.USER, "Hello \"world\" with 'quotes' and \n newlines")
        );

        // When
        final String result = RequestPayloadGenerator.generate(template, messages, "my-model-name", "my-secret-API-key");

        // Then
        Assertions.assertEquals("Message: \"Hello \\\"world\\\" with 'quotes' and \\n newlines\"", result);
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
            () -> RequestPayloadGenerator.generate(template, messages, "my-model-name", "my-secret-API-key")
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
        final String result = RequestPayloadGenerator.generate(template, messages, "my-model-name", "my-secret-API-key");

        // Then
        Assertions.assertEquals("", result);
    }
}
