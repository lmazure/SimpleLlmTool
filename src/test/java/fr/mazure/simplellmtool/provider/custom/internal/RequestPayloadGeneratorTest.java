package fr.mazure.simplellmtool.provider.custom.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.langchain4j.agent.tool.ToolSpecification;
import fr.mazure.simplellmtool.ToolManager;

/**
 * Tests for the {@link RequestPayloadGenerator} class.
 */
class RequestPayloadGeneratorTest {

    @SuppressWarnings("static-method")
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
            new MessageRound(Role.SYSTEM, "You are a helpful assistant", List.of()),
            new MessageRound(Role.USER, "What is the weather?", List.of()),
            new MessageRound(Role.MODEL, "I don't have access to weather data", List.of()),
            new MessageRound(Role.USER, "What day is it?", List.of()),
            new MessageRound(Role.MODEL, "April fools' day", List.of()),
            new MessageRound(Role.USER, "So, tell me a joke!", List.of())
        );

        // When
        final String result = RequestPayloadGenerator.generate(template, messages, "my-model-name", List.of(), "my-secret-API-key");

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

    @SuppressWarnings("static-method")
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
            new MessageRound(Role.SYSTEM, "You are a helpful assistant", List.of()),
            new MessageRound(Role.USER, "What is the weather?", List.of()),
            new MessageRound(Role.MODEL, "I don't have access to weather data", List.of()),
            new MessageRound(Role.USER, "What day is it?", List.of()),
            new MessageRound(Role.MODEL, "April fools' day", List.of()),
            new MessageRound(Role.USER, "So, tell me a joke!", List.of())
        );

        // When
        final String result = RequestPayloadGenerator.generate(template, messages, "my-model-name", List.of(), "my-secret-API-key");

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

    @SuppressWarnings("static-method")
    @Test
    @DisplayName("Should generate Google's Gemini payload with multiple tools")
    void testGenerateMultipleToolsForGoogleGemini() {
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
          "tools": [
            {
              "function_declarations": [
                {{#each tools}}{
                  "name": {{convertToJsonString name}},
                  "description": {{convertToJsonString description}},
                  "parameters": {
                    "type": "object",
                    "properties": {
                      {{#each parameters}}{{convertToJsonString name}}: {
                        "type": {{#if (isStringType type)}}"string"{{/if}}{{#if (isIntegerType type)}}"integer"{{/if}}{{#if (isNumberType type)}}"number"{{/if}}{{#if (isBooleanType type)}}"boolean"{{/if}},
                        "description": {{convertToJsonString description}}
                      }{{#unless @last}},
                      {{/unless}}{{/each}}
                    },
                    "required": [
                      {{#each requiredParameters}}{{convertToJsonString name}}{{#unless @last}},
                      {{/unless}}{{/each}}
                    ]
                  }
                }{{#unless @last}},
                {{/unless}}{{/each}}
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

        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(Role.SYSTEM, "You are a helpful assistant", List.of()),
            new MessageRound(Role.USER, "What is the weather?", List.of())
        );

        final ToolManager.Tool getWeatherTool = new ToolManager.Tool("getWeather",
                                                                     "Get the weather",
                                                                     List.of(new ToolManager.ToolParameter("city", "The city to get the weather for", ToolManager.ToolParameterType.STRING, true)));
        final ToolManager.Tool fooTool = new ToolManager.Tool("foo",
                                                              "Perform foo",
                                                              List.of(new ToolManager.ToolParameter("alpha", "first argument", ToolManager.ToolParameterType.STRING, true),
                                                                      new ToolManager.ToolParameter("beta", "second argument", ToolManager.ToolParameterType.INTEGER, true),
                                                                      new ToolManager.ToolParameter("gamma", "third argument", ToolManager.ToolParameterType.NUMBER, false),
                                                                      new ToolManager.ToolParameter("delta", "fourth argument", ToolManager.ToolParameterType.BOOLEAN, true)));

        final List<ToolSpecification> tools = List.of(
          ToolManager.getSpecification(getWeatherTool),
          ToolManager.getSpecification(fooTool)
        );
        
        // When
        final String result = RequestPayloadGenerator.generate(template, messages, "my-model-name", tools, "my-secret-API-key");

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
            }
          ],
          "tools": [
            {
              "function_declarations": [
                {
                  "name": "getWeather",
                  "description": "Get the weather",
                  "parameters": {
                    "type": "object",
                    "properties": {
                      "city": {
                        "type": "string",
                        "description": "The city to get the weather for"
                      }
                    },
                    "required": [
                      "city"
                    ]
                  }
                },
                {
                  "name": "foo",
                  "description": "Perform foo",
                  "parameters": {
                    "type": "object",
                    "properties": {
                      "alpha": {
                        "type": "string",
                        "description": "first argument"
                      },
                      "beta": {
                        "type": "integer",
                        "description": "second argument"
                      },
                      "gamma": {
                        "type": "number",
                        "description": "third argument"
                      },
                      "delta": {
                        "type": "boolean",
                        "description": "fourth argument"
                      }
                    },
                    "required": [
                      "alpha",
                      "beta",
                      "delta"
                    ]
                  }
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

    @SuppressWarnings("static-method")
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
            new MessageRound(Role.SYSTEM, "You are a helpful assistant", List.of()),
            new MessageRound(Role.USER, "What is the weather?", List.of()),
            new MessageRound(Role.MODEL, "I don't have access to weather data", List.of()),
            new MessageRound(Role.USER, "What day is it?", List.of()),
            new MessageRound(Role.MODEL, "April fools' day", List.of()),
            new MessageRound(Role.USER, "So, tell me a joke!", List.of())
        );

        // When
        final String result = RequestPayloadGenerator.generate(template, messages, "my-model-name", List.of(), "my-secret-API-key");

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

    @SuppressWarnings("static-method")
    @Test
    @DisplayName("Should handle API key in HTTP headers")
    void testGenerateApiKet() {
        // Given
        final String template = "Bearer: {{apiKey}}";

        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(Role.SYSTEM, "You are a helpful assistant", List.of()),
            new MessageRound(Role.USER, "What is the weather?", List.of()),
            new MessageRound(Role.MODEL, "I don't have access to weather data", List.of())
        );

        // When
        final String result = RequestPayloadGenerator.generate(template, messages, "my-model-name", List.of(), "my-secret-API-key");

        // Then
        Assertions.assertEquals("Bearer: my-secret-API-key", result);
    }

    @SuppressWarnings("static-method")
    @Test
    @DisplayName("Does not modify special characters in API key in HTTP headers")
    void testGenerateAPiKeyWithoutChangingCharacters() {
        // Given
        final String template = "Bearer: {{apiKey}}";

        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(Role.SYSTEM, "You are a helpful assistant", List.of()),
            new MessageRound(Role.USER, "What is the weather?", List.of()),
            new MessageRound(Role.MODEL, "I don't have access to weather data", List.of())
        );

        // When
        final String result = RequestPayloadGenerator.generate(template, messages, "my-model-name", List.of(), "&é~\"#'{([-|è`_\\ç^à@)]");

        // Then
        Assertions.assertEquals("Bearer: &é~\"#'{([-|è`_\\ç^à@)]", result);
    }

    @SuppressWarnings("static-method")
    @Test
    @DisplayName("Should handle empty messages list")
    void testGenerateEmptyMessages() {
        // Given
        final String template = "Messages: {{#messages}}{{convertToJsonString content}}{{/messages}}";
        final List<MessageRound> messages = Collections.emptyList();

        // When
        final String result = RequestPayloadGenerator.generate(template, messages, "my-model-name", List.of(), "my-secret-API-key");

        // Then
        Assertions.assertEquals("Messages: ", result);
    }

    @SuppressWarnings("static-method")
    @Test
    @DisplayName("Should handle template without message placeholders")
    void testGenerateTemplateWithoutPlaceholders() {
        // Given
        final String template = "This is a static template without placeholders";
        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(Role.USER, "Hello", List.of())
        );

        // When
        final String result = RequestPayloadGenerator.generate(template, messages, "my-model-name", List.of(), "my-secret-API-key");

        // Then
        Assertions.assertEquals("This is a static template without placeholders", result);
    }

    @SuppressWarnings("static-method")
    @Test
    @DisplayName("Should handle special characters in messages")
    void testGenerateWithSpecialCharacters() {
        // Given
        final String template = "Message: {{#messages}}{{convertToJsonString content}}{{/messages}}";
        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(Role.USER, "Hello \"world\" with 'quotes' and \n newlines", List.of())
        );

        // When
        final String result = RequestPayloadGenerator.generate(template, messages, "my-model-name", List.of(), "my-secret-API-key");

        // Then
        Assertions.assertEquals("Message: \"Hello \\\"world\\\" with 'quotes' and \\n newlines\"", result);
    }

    @SuppressWarnings("static-method")
    @Test
    @DisplayName("Should throw runtime exception for invalid template syntax")
    void testGenerateInvalidTemplate() {
        // Given
        final String template = "Hello {{#messages}}{{role}"; // Missing closing tag
        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(Role.USER, "Hello", List.of())
        );

        // When & Then
        RuntimeException exception = Assertions.assertThrows(
            RuntimeException.class,
            () -> RequestPayloadGenerator.generate(template, messages, "my-model-name", List.of(), "my-secret-API-key")
        );
        Assertions.assertEquals("Failed to process Handlebars template", exception.getMessage());
    }

    @SuppressWarnings("static-method")
    @Test
    @DisplayName("Should handle empty template")
    void testGenerateEmptyTemplate() {
        // Given
        final String template = "";
        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(Role.USER, "Hello", List.of())
        );

        // When
        final String result = RequestPayloadGenerator.generate(template, messages, "my-model-name", List.of(),   "my-secret-API-key");

        // Then
        Assertions.assertEquals("", result);
    }
}
