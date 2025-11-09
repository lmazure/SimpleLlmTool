package fr.mazure.simplellmtool.provider.custom.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.langchain4j.agent.tool.ToolSpecification;
import fr.mazure.simplellmtool.tools.ToolManager;
import fr.mazure.simplellmtool.tools.ToolManagerException;
import fr.mazure.simplellmtool.tools.ToolParameterType;
import fr.mazure.simplellmtool.tools.ToolParameterValue;

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
                  "content": {{convertStringToJsonString content}}
                }{{#unless @last}},
                {{/unless}}{{/each}}
              ],
              "temperature": 0.7,
              "seed": 42
            }
            """;

        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(MessageRound.Role.SYSTEM, "You are a helpful assistant"),
            new MessageRound(MessageRound.Role.USER, "What is the weather?"),
            new MessageRound(MessageRound.Role.MODEL, "I don't have access to weather data"),
            new MessageRound(MessageRound.Role.USER, "What day is it?"),
            new MessageRound(MessageRound.Role.MODEL, "April fools' day"),
            new MessageRound(MessageRound.Role.USER, "So, tell me a joke!")
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
        final String template = buildGeminiTemplate();

        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(MessageRound.Role.SYSTEM, "You are a helpful assistant"),
            new MessageRound(MessageRound.Role.USER, "What is the weather?"),
            new MessageRound(MessageRound.Role.MODEL, "I don't have access to weather data"),
            new MessageRound(MessageRound.Role.USER, "What day is it?"),
            new MessageRound(MessageRound.Role.MODEL, "April fools' day"),
            new MessageRound(MessageRound.Role.USER, "So, tell me a joke!")
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
                  \s\s\s\s\s\s
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
                  \s\s\s\s\s\s
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
                  "tools": [
                    {
                      "function_declarations": [
                  \s\s\s\s\s\s
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
    void testGenerateMultipleToolsForGoogleGemini() throws ToolManagerException {
        // Given
        final String template = buildGeminiTemplate();

        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(MessageRound.Role.SYSTEM, "You are a helpful assistant"),
            new MessageRound(MessageRound.Role.USER, "What is the weather?")
        );

        final ToolManager.Tool getWeatherTool = new ToolManager.Tool("getWeather",
                                                                     "Get the weather",
                                                                     List.of(new ToolManager.ToolParameter("city", "The city to get the weather for", ToolParameterType.STRING, true)));
        final ToolManager.Tool fooTool = new ToolManager.Tool("foo",
                                                              "Perform foo",
                                                              List.of(new ToolManager.ToolParameter("alpha", "first argument", ToolParameterType.STRING, true),
                                                                      new ToolManager.ToolParameter("beta", "second argument", ToolParameterType.INTEGER, true),
                                                                      new ToolManager.ToolParameter("gamma", "third argument", ToolParameterType.NUMBER, false),
                                                                      new ToolManager.ToolParameter("delta", "fourth argument", ToolParameterType.BOOLEAN, true)));

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
    @DisplayName("Should generate Google's Gemini payload with tool call results with string input parameters")
    void testGenerateToolCallResultsWithStringInputParametersForGoogleGemini() throws ToolManagerException {
        // Given
        final String template = buildGeminiTemplate();

        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(MessageRound.Role.SYSTEM, "You always provide an English anwer, followed by a precise translation in French"),
            new MessageRound(MessageRound.Role.USER, "What is the weather in Paris?"),
            new MessageRound(MessageRound.Role.MODEL, "", List.of(new MessageRound.ToolCall("get_weather", "call_XX01", List.of(new MessageRound.ToolParameter("city", new ToolParameterValue(ToolParameterType.STRING, "Paris")))))),
            new MessageRound(MessageRound.Role.TOOL, "Paris, ?le-de-France, France: 14.0?C, Mainly Clear, Feels like 13.3?C, Humidity 88%", "get_weather", "call_XX01")
        );

        final ToolManager.Tool getWeatherTool = new ToolManager.Tool("get_weather",
                                                                     "Returns the current weather for a given city",
                                                                     List.of(new ToolManager.ToolParameter("city", "The city for which the weather forecast should be returned, only the city name should be present", ToolParameterType.STRING, true)));

        final List<ToolSpecification> tools = List.of(
          ToolManager.getSpecification(getWeatherTool)
        );

        // When
        final String result = RequestPayloadGenerator.generate(template, messages, "my-model-name", tools, "my-secret-API-key");

        // Then
        final String expectedResult = """
        {
          "system_instruction": {
            "parts": [
              {
                "text": "You always provide an English anwer, followed by a precise translation in French"
              }
            ]
          },
          "contents": [
            {
              "role": "user",
              "parts": [
                {
                  "text": "What is the weather in Paris?"
                }
              ]
            },
            {
              "role": "model",
              "parts": [
            \s\s\s\s
                {
                "functionCall": {
                "name": "get_weather",
                  "args": {
             \s\s\s\s\s\s\s
                    "city": "Paris"
             \s\s\s\s\s\s\s
                  }
                }
                 }\s
              ]
            },
            {
              "role": "function",
              "parts": [
                {
                  "functionResponse": {
                    "name": "get_weather",
                    "response": {
                      "result": "Paris, ?le-de-France, France: 14.0?C, Mainly Clear, Feels like 13.3?C, Humidity 88%"
                    }
                  }
                }
              ]
            }
          ],
          "tools": [
            {
              "function_declarations": [
                {
                  "name": "get_weather",
                  "description": "Returns the current weather for a given city",
                  "parameters": {
                    "type": "object",
                    "properties": {
                      "city": {
                        "type": "string",
                        "description": "The city for which the weather forecast should be returned, only the city name should be present"
                      }
                    },
                    "required": [
                      "city"
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
    @DisplayName("Should generate Google's Gemini payload with tool call results with string float and boolean parameters")
    void testGenerateToolCallResultsWithFloatAndBooleanInputParametersForGoogleGemini() throws ToolManagerException {
        // Given
        final String template = buildGeminiTemplate();

        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(MessageRound.Role.SYSTEM, "You are an assistant helping users to set up weather alerts."),
            new MessageRound(MessageRound.Role.USER, "Set an alert if temperature reaches 25.8°C"),
            new MessageRound(MessageRound.Role.MODEL, null, List.of(new MessageRound.ToolCall("set_weather_alert", "0ad6ff0f-7ce8-4fc9-86f1-4db9ea0ac5d6", 
                                                                    List.of(new MessageRound.ToolParameter("enable_temperature_alert", new ToolParameterValue(ToolParameterType.BOOLEAN, Boolean.TRUE)),
                                                                            new MessageRound.ToolParameter("temperature_threshold_celsius", new ToolParameterValue(ToolParameterType.NUMBER, Double.valueOf(25.8))),
                                                                            new MessageRound.ToolParameter("precipitation_threshold_mm", new ToolParameterValue(ToolParameterType.INTEGER, Integer.valueOf(0))), //TODO this should a number, not an INTEGER (and the expected result is wrong)
                                                                            new MessageRound.ToolParameter("enable_precipitation_alert", new ToolParameterValue(ToolParameterType.BOOLEAN, Boolean.FALSE)))))),
            new MessageRound(MessageRound.Role.TOOL, "{\"success\": true, \"alerts\": {\"temperature\": {\"enabled\": true, \"threshold_celsius\": 25.8}, \"precipitation\": {\"enabled\": false, \"threshold_mm\": null}}, \"message\": \"Weather alerts configured for: temperature 25.8\u00b0C\"}\n", "set_weather_alert", "0ad6ff0f-7ce8-4fc9-86f1-4db9ea0ac5d6")
        );

        final ToolManager.Tool setWeatherAlertTool = new ToolManager.Tool("set_weather_alert",
                                                                          "Configure weather alerts for a location based on temperature and precipitation thresholds",
                                                                          List.of(new ToolManager.ToolParameter("enable_temperature_alert", "Whether to enable temperature-based alerts", ToolParameterType.BOOLEAN, true),
                                                                                  new ToolManager.ToolParameter("enable_precipitation_alert", "Whether to enable precipitation-based alerts", ToolParameterType.BOOLEAN, true),
                                                                                  new ToolManager.ToolParameter("temperature_threshold_celsius", "Temperature threshold in Celsius (e.g., 35.0 for heat warning)", ToolParameterType.NUMBER, true),
                                                                                  new ToolManager.ToolParameter("precipitation_threshold_mm", "Precipitation threshold in millimeters (e.g., 50.0 for flood risk)", ToolParameterType.NUMBER, true)));

        final List<ToolSpecification> tools = List.of(
          ToolManager.getSpecification(setWeatherAlertTool)
        );

        // When
        final String result = RequestPayloadGenerator.generate(template, messages, "my_model_name", tools, "my-secret-API-key");

        // Then
        final String expectedResult = """
            {
              "system_instruction": {
                "parts": [
                  {
                    "text": "You are an assistant helping users to set up weather alerts."
                  }
                ]
              },
              "contents": [
                {
                  "role": "user",
                  "parts": [
                    {
                      "text": "Set an alert if temperature reaches 25.8°C"
                    }
                  ]
                },
                {
                  "role": "model",
                  "parts": [
                   \s
                    {
                    "functionCall": {
                    "name": "set_weather_alert",
                      "args": {
                       \s
                        "enable_temperature_alert": true
                        ,
                       \s
                        "temperature_threshold_celsius": 25.8
                        ,
                       \s
                        "precipitation_threshold_mm": 0
                        ,
                       \s
                        "enable_precipitation_alert": false
                       \s
                      }
                    }
                     }\s
                  ]
                },
                {
                  "role": "function",
                  "parts": [
                    {
                      "functionResponse": {
                        "name": "set_weather_alert",
                        "response": {
                          "result": "{\\"success\\": true, \\"alerts\\": {\\"temperature\\": {\\"enabled\\": true, \\"threshold_celsius\\": 25.8}, \\"precipitation\\": {\\"enabled\\": false, \\"threshold_mm\\": null}}, \\"message\\": \\"Weather alerts configured for: temperature 25.8\u00b0C\\"}\\n"
                        }
                      }
                    }
                  ]
                }
              ],
              "tools": [
                {
                  "function_declarations": [
                    {
                      "name": "set_weather_alert",
                      "description": "Configure weather alerts for a location based on temperature and precipitation thresholds",
                      "parameters": {
                        "type": "object",
                        "properties": {
                          "enable_temperature_alert": {
                            "type": "boolean",
                            "description": "Whether to enable temperature-based alerts"
                          },
                          "enable_precipitation_alert": {
                            "type": "boolean",
                            "description": "Whether to enable precipitation-based alerts"
                          },
                          "temperature_threshold_celsius": {
                            "type": "number",
                            "description": "Temperature threshold in Celsius (e.g., 35.0 for heat warning)"
                          },
                          "precipitation_threshold_mm": {
                            "type": "number",
                            "description": "Precipitation threshold in millimeters (e.g., 50.0 for flood risk)"
                          }
                        },
                        "required": [
                          "enable_temperature_alert",
                          "enable_precipitation_alert",
                          "temperature_threshold_celsius",
                          "precipitation_threshold_mm"
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
    @DisplayName("Should generate GPT5 payload with multiple messages")
    void testGenerateMultipleMessagesForGPT5() {
        // Given
        final String template = buildGPT5Template();

        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(MessageRound.Role.SYSTEM, "You are a helpful assistant"),
            new MessageRound(MessageRound.Role.USER, "What is the weather?"),
            new MessageRound(MessageRound.Role.MODEL, "I don't have access to weather data"),
            new MessageRound(MessageRound.Role.USER, "What day is it?"),
            new MessageRound(MessageRound.Role.MODEL, "April fools' day"),
            new MessageRound(MessageRound.Role.USER, "So, tell me a joke!")
        );

        // When
        final String result = RequestPayloadGenerator.generate(template, messages, "gpt-5-nano-2025-08-07", List.of(), "my-secret-API-key");

        // Then
        final String expectedResult = """
                {
                  "model": "gpt-5-nano-2025-08-07",
                  "messages": [
                    {
                      "role": "system",
                      "content": "You are a helpful assistant"
                    },{
                      "role": "user",
                      "content": "What is the weather?"
                    },{
                      "role": "assistant",
                      "content": "I don't have access to weather data"
                    },{
                      "role": "user",
                      "content": "What day is it?"
                    },{
                      "role": "assistant",
                      "content": "April fools' day"
                    },{
                      "role": "user",
                      "content": "So, tell me a joke!"
                    }
                  ]
                }
                """;
        Assertions.assertEquals(expectedResult, result);
    }

    @SuppressWarnings("static-method")
    @Test
    @DisplayName("Should generate GPT5 payload with multiple tools")
    void testGenerateMultipleToolsForGPT5() throws ToolManagerException {
        // Given
        final String template = buildGPT5Template();

        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(MessageRound.Role.SYSTEM, "You are a helpful assistant"),
            new MessageRound(MessageRound.Role.USER, "What is the weather?")
        );

        final ToolManager.Tool getWeatherTool = new ToolManager.Tool("getWeather",
                                                                     "Get the weather",
                                                                     List.of(new ToolManager.ToolParameter("city", "The city to get the weather for", ToolParameterType.STRING, true)));
        final ToolManager.Tool fooTool = new ToolManager.Tool("foo",
                                                              "Perform foo",
                                                              List.of(new ToolManager.ToolParameter("alpha", "first argument", ToolParameterType.STRING, true),
                                                                      new ToolManager.ToolParameter("beta", "second argument", ToolParameterType.INTEGER, true),
                                                                      new ToolManager.ToolParameter("gamma", "third argument", ToolParameterType.NUMBER, false),
                                                                      new ToolManager.ToolParameter("delta", "fourth argument", ToolParameterType.BOOLEAN, true)));

        final List<ToolSpecification> tools = List.of(
          ToolManager.getSpecification(getWeatherTool),
          ToolManager.getSpecification(fooTool)
        );

        // When
        final String result = RequestPayloadGenerator.generate(template, messages, "gpt-5-nano-2025-08-07", tools, "my-secret-API-key");

        // Then
        final String expectedResult = """
            {
              "model": "gpt-5-nano-2025-08-07",
              "messages": [
                {
                  "role": "system",
                  "content": "You are a helpful assistant"
                },{
                  "role": "user",
                  "content": "What is the weather?"
                }
              ],
              "tools": [
                {
                  "type": "function",
                  "function": {
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
                      "required": ["city"]
                    }
                  }
                },{
                  "type": "function",
                  "function": {
                    "name": "foo",
                    "description": "Perform foo",
                    "parameters": {
                      "type": "object",
                      "properties": {
                        "alpha": {
                          "type": "string",
                          "description": "first argument"
                        },"beta": {
                          "type": "integer",
                          "description": "second argument"
                        },"gamma": {
                          "type": "number",
                          "description": "third argument"
                        },"delta": {
                          "type": "boolean",
                          "description": "fourth argument"
                        }
                      },
                      "required": ["alpha","beta","delta"]
                    }
                  }
                }
              ]
            }
            """;
        Assertions.assertEquals(expectedResult, result);
    }

    @SuppressWarnings("static-method")
    @Test
    @DisplayName("Should generate GPT5 payload with tool call results with string input parameters")
    void testGenerateToolCallResultsWithStringInputParametersForGPT5() throws ToolManagerException {
        // Given
        final String template = buildGPT5Template();

        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(MessageRound.Role.SYSTEM, "You always provide an English anwer, followed by a precise translation in French"),
            new MessageRound(MessageRound.Role.USER, "What is the weather in Paris?"),
            new MessageRound(MessageRound.Role.MODEL, "", List.of(new MessageRound.ToolCall("get_weather", "call_XX01", List.of(new MessageRound.ToolParameter("city", new ToolParameterValue(ToolParameterType.STRING, "Paris")))))),
            new MessageRound(MessageRound.Role.TOOL, "Paris, ?le-de-France, France: 14.0?C, Mainly Clear, Feels like 13.3?C, Humidity 88%", "get_weather", "call_XX01")
        );

        final ToolManager.Tool getWeatherTool = new ToolManager.Tool("get_weather",
                                                                     "Returns the current weather for a given city",
                                                                     List.of(new ToolManager.ToolParameter("city", "The city for which the weather forecast should be returned, only the city name should be present", ToolParameterType.STRING, true)));

        final List<ToolSpecification> tools = List.of(
          ToolManager.getSpecification(getWeatherTool)
        );

        // When
        final String result = RequestPayloadGenerator.generate(template, messages, "gpt-5-nano-2025-08-07", tools, "my-secret-API-key");

        // Then
        final String expectedResult = """
            {
              "model": "gpt-5-nano-2025-08-07",
              "messages": [
                {
                  "role": "system",
                  "content": "You always provide an English anwer, followed by a precise translation in French"
                },{
                  "role": "user",
                  "content": "What is the weather in Paris?"
                },{
                  "role": "assistant",
                  "content": ""null,
                  "tool_calls": [
                    {
                      "id": "call_XX01",
                      "type": "function",
                      "function": {
                        "name": "get_weather",
                        "arguments": "{ \\"city\\": \\"Paris\\" }"
                      }
                    }
                  ]
                },{
                  "role": "tool",
                  "content": "Paris, ?le-de-France, France: 14.0?C, Mainly Clear, Feels like 13.3?C, Humidity 88%",
                  "tool_call_id": "call_XX01"
                }
              ],
              "tools": [
                {
                  "type": "function",
                  "function": {
                    "name": "get_weather",
                    "description": "Returns the current weather for a given city",
                    "parameters": {
                      "type": "object",
                      "properties": {
                        "city": {
                          "type": "string",
                          "description": "The city for which the weather forecast should be returned, only the city name should be present"
                        }
                      },
                      "required": ["city"]
                    }
                  }
                }
              ]
            }
            """;
        Assertions.assertEquals(expectedResult, result);
    }

    @SuppressWarnings("static-method")
    @Test
    @DisplayName("Should generate GPT5 payload with tool call results with float and boolean input parameters")
    void testGenerateToolCallResultsWithFloatAndBooleanInputParametersForGPT5() throws ToolManagerException {
        // Given
        final String template = buildGPT5Template();

        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(MessageRound.Role.SYSTEM, "You are an assistant helping users to set up weather alerts."),
            new MessageRound(MessageRound.Role.USER, "Set an alert if temperature reaches 25.8°C"),
            new MessageRound(MessageRound.Role.MODEL, null, List.of(new MessageRound.ToolCall("set_weather_alert", "0ad6ff0f-7ce8-4fc9-86f1-4db9ea0ac5d6", 
                                                                    List.of(new MessageRound.ToolParameter("enable_temperature_alert", new ToolParameterValue(ToolParameterType.BOOLEAN, Boolean.TRUE)),
                                                                            new MessageRound.ToolParameter("enable_precipitation_alert", new ToolParameterValue(ToolParameterType.BOOLEAN, Boolean.FALSE)),
                                                                            new MessageRound.ToolParameter("temperature_threshold_celsius", new ToolParameterValue(ToolParameterType.NUMBER, Double.valueOf(25.8))),
                                                                            new MessageRound.ToolParameter("precipitation_threshold_mm", new ToolParameterValue(ToolParameterType.INTEGER, Integer.valueOf(0))))))), //TODO this should a number, not an INTEGER (and the expected result is wrong)
            new MessageRound(MessageRound.Role.TOOL, "{\"success\": true, \"alerts\": {\"temperature\": {\"enabled\": true, \"threshold_celsius\": 25.8}, \"precipitation\": {\"enabled\": false, \"threshold_mm\": null}}, \"message\": \"Weather alerts configured for: temperature 25.8\u00b0C\"}\n", "set_weather_alert", "0ad6ff0f-7ce8-4fc9-86f1-4db9ea0ac5d6")
        );

        final ToolManager.Tool setWeatherAlertTool = new ToolManager.Tool("set_weather_alert",
                                                                          "Configure weather alerts for a location based on temperature and precipitation thresholds",
                                                                          List.of(new ToolManager.ToolParameter("enable_temperature_alert", "Whether to enable temperature-based alerts", ToolParameterType.BOOLEAN, true),
                                                                                  new ToolManager.ToolParameter("enable_precipitation_alert", "Whether to enable precipitation-based alerts", ToolParameterType.BOOLEAN, true),
                                                                                  new ToolManager.ToolParameter("temperature_threshold_celsius", "Temperature threshold in Celsius (e.g., 35.0 for heat warning)", ToolParameterType.NUMBER, true),
                                                                                  new ToolManager.ToolParameter("precipitation_threshold_mm", "Precipitation threshold in millimeters (e.g., 50.0 for flood risk)", ToolParameterType.NUMBER, true)));

        final List<ToolSpecification> tools = List.of(
          ToolManager.getSpecification(setWeatherAlertTool)
        );

        // When
        final String result = RequestPayloadGenerator.generate(template, messages, "gpt-5-nano-2025-08-07", tools, "my-secret-API-key");

        // Then
        final String expectedResult = """
            {
              "model": "gpt-5-nano-2025-08-07",
              "messages": [
                {
                  "role": "system",
                  "content": "You are an assistant helping users to set up weather alerts."
                },{
                  "role": "user",
                  "content": "Set an alert if temperature reaches 25.8°C"
                },{
                  "role": "assistant",
                  "content": null,
                  "tool_calls": [
                    {
                      "id": "0ad6ff0f-7ce8-4fc9-86f1-4db9ea0ac5d6",
                      "type": "function",
                      "function": {
                        "name": "set_weather_alert",
                        "arguments": "{ \\"enable_temperature_alert\\": true, \\"enable_precipitation_alert\\": false, \\"temperature_threshold_celsius\\": 25.8, \\"precipitation_threshold_mm\\": 0 }"
                      }
                    }
                  ]
                },{
                  "role": "tool",
                  "content": "{\\"success\\": true, \\"alerts\\": {\\"temperature\\": {\\"enabled\\": true, \\"threshold_celsius\\": 25.8}, \\"precipitation\\": {\\"enabled\\": false, \\"threshold_mm\\": null}}, \\"message\\": \\"Weather alerts configured for: temperature 25.8\u00b0C\\"}\\n",
                  "tool_call_id": "0ad6ff0f-7ce8-4fc9-86f1-4db9ea0ac5d6"
                }
              ],
              "tools": [
                {
                  "type": "function",
                  "function": {
                    "name": "set_weather_alert",
                    "description": "Configure weather alerts for a location based on temperature and precipitation thresholds",
                    "parameters": {
                      "type": "object",
                      "properties": {
                        "enable_temperature_alert": {
                          "type": "boolean",
                          "description": "Whether to enable temperature-based alerts"
                        },"enable_precipitation_alert": {
                          "type": "boolean",
                          "description": "Whether to enable precipitation-based alerts"
                        },"temperature_threshold_celsius": {
                          "type": "number",
                          "description": "Temperature threshold in Celsius (e.g., 35.0 for heat warning)"
                        },"precipitation_threshold_mm": {
                          "type": "number",
                          "description": "Precipitation threshold in millimeters (e.g., 50.0 for flood risk)"
                        }
                      },
                      "required": ["enable_temperature_alert","enable_precipitation_alert","temperature_threshold_celsius","precipitation_threshold_mm"]
                    }
                  }
                }
              ]
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
                  "content": {{convertStringToJsonString content}}
                }{{#unless @last}},
                {{/unless}}{{/each}}
              ],
              "temperature": 0.7,
              "seed": 42
            }
            """;

        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(MessageRound.Role.SYSTEM, "You are a helpful assistant"),
            new MessageRound(MessageRound.Role.USER, "What is the weather?"),
            new MessageRound(MessageRound.Role.MODEL, "I don't have access to weather data"),
            new MessageRound(MessageRound.Role.USER, "What day is it?"),
            new MessageRound(MessageRound.Role.MODEL, "April fools' day"),
            new MessageRound(MessageRound.Role.USER, "So, tell me a joke!")
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
    void testGenerateApiKey() {
        // Given
        final String template = "Bearer: {{apiKey}}";

        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(MessageRound.Role.SYSTEM, "You are a helpful assistant"),
            new MessageRound(MessageRound.Role.USER, "What is the weather?"),
            new MessageRound(MessageRound.Role.MODEL, "I don't have access to weather data")
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
            new MessageRound(MessageRound.Role.SYSTEM, "You are a helpful assistant"),
            new MessageRound(MessageRound.Role.USER, "What is the weather?"),
            new MessageRound(MessageRound.Role.MODEL, "I don't have access to weather data")
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
        final String template = "Messages: {{#messages}}{{convertStringToJsonString content}}{{/messages}}";
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
            new MessageRound(MessageRound.Role.USER, "Hello")
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
        final String template = "Message: {{#messages}}{{convertStringToJsonString content}}{{/messages}}";
        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(MessageRound.Role.USER, "Hello \"world\" with 'quotes' and \n newlines")
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
            new MessageRound(MessageRound.Role.USER, "Hello")
        );

        // When & Then
        RuntimeException exception = Assertions.assertThrows(
            RuntimeException.class,
            () -> RequestPayloadGenerator.generate(template, messages, "my-model-name", List.of(), "my-secret-API-key")
        );
        Assertions.assertEquals("Failed to process Handlebars template\n001 Hello {{#messages}}{{role}", exception.getMessage());
    }

    @SuppressWarnings("static-method")
    @Test
    @DisplayName("Should handle empty template")
    void testGenerateEmptyTemplate() {
        // Given
        final String template = "";
        final List<MessageRound> messages = Arrays.asList(
            new MessageRound(MessageRound.Role.USER, "Hello")
        );

        // When
        final String result = RequestPayloadGenerator.generate(template, messages, "my-model-name", List.of(), "my-secret-API-key");

        // Then
        Assertions.assertEquals("", result);
    }

    private static String buildGeminiTemplate() {
        return """
            {
              {{#each messages}}{{#if (isSystem role)}}"system_instruction": {
                "parts": [
                  {
                    "text": {{convertStringToJsonString content}}
                  }
                ]
              },{{/if}}{{/each}}
              "contents": [
                {{#each messages}}{{#if (isUser role)}}{
                  "role": "user",
                  "parts": [
                    {
                      "text": {{convertStringToJsonString content}}
                    }
                  ]
                }{{#unless @last}},
                {{/unless}}{{/if}}{{#if (isModel role)}}{
                  "role": "model",
                  "parts": [
                    {{#if content}}{
                      "text": {{convertStringToJsonString content}}
                    }{{/if}}
                    {{#each toolCalls}}{
                    "functionCall": {
                    "name": {{convertStringToJsonString toolName}},
                      "args": {
                        {{#each toolParameters}}
                        {{convertStringToJsonString parameterName}}: {{convertToolParameterValueToJsonString parameterValue}}
                        {{#unless @last}},
                        {{/unless}}{{/each}}
                      }
                    }
                    {{#unless @last}},
                    {{/unless}} } {{/each}}
                  ]
                }{{#unless @last}},
                {{/unless}}{{/if}}{{#if (isTool role)}}{
                  "role": "function",
                  "parts": [
                    {
                      "functionResponse": {
                        "name": {{convertStringToJsonString toolName}},
                        "response": {
                          "result": {{convertStringToJsonString content}}
                        }
                      }
                    }
                  ]
                }{{#unless @last}},
                {{/unless}}{{/if}}{{/each}}
              ],
              "tools": [
                {
                  "function_declarations": [
                    {{#each tools}}{
                      "name": {{convertStringToJsonString name}},
                      "description": {{convertStringToJsonString description}},
                      "parameters": {
                        "type": "object",
                        "properties": {
                          {{#each parameters}}{{convertStringToJsonString name}}: {
                            "type": {{#if (isStringType type)}}"string"{{/if}}{{#if (isIntegerType type)}}"integer"{{/if}}{{#if (isNumberType type)}}"number"{{/if}}{{#if (isBooleanType type)}}"boolean"{{/if}},
                            "description": {{convertStringToJsonString description}}
                          }{{#unless @last}},
                          {{/unless}}{{/each}}
                        },
                        "required": [
                          {{#each requiredParameters}}{{convertStringToJsonString name}}{{#unless @last}},
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
    }

    private static String buildGPT5Template() {
        return """
            {
              "model": "{{modelName}}",
              "messages": [
                {{#each messages}}{
                  "role": "{{#if (isSystem role)}}system{{/if}}{{#if (isUser role)}}user{{/if}}{{#if (isModel role)}}assistant{{/if}}{{#if (isTool role)}}tool{{/if}}",
                  "content": {{convertStringToJsonString content}}{{#if (isModel role)}}{{#if toolCalls}}null,
                  "tool_calls": [
                    {{#each toolCalls}}{
                      "id": {{convertStringToJsonString toolCallId}},
                      "type": "function",
                      "function": {
                        "name": {{convertStringToJsonString toolName}},
                        "arguments": {{convertToolParametersToJsonString toolParameters}}
                      }
                    }{{#unless @last}},{{/unless}}{{/each}}
                  ]{{/if}}{{/if}}{{#if (isTool role)}},
                  "tool_call_id": {{convertStringToJsonString toolCallId}}{{/if}}
                }{{#unless @last}},{{/unless}}{{/each}}
              ]{{#if tools}},
              "tools": [
                {{#each tools}}{
                  "type": "function",
                  "function": {
                    "name": "{{name}}",
                    "description": {{convertStringToJsonString description}},
                    "parameters": {
                      "type": "object",
                      "properties": {
                        {{#each parameters}}"{{name}}": {
                          "type": "{{#if (isStringType type)}}string{{/if}}{{#if (isIntegerType type)}}integer{{/if}}{{#if (isNumberType type)}}number{{/if}}{{#if (isBooleanType type)}}boolean{{/if}}",
                          "description": {{convertStringToJsonString description}}
                        }{{#unless @last}},{{/unless}}{{/each}}
                      },
                      "required": [{{#each requiredParameters}}{{convertStringToJsonString name}}{{#unless @last}},{{/unless}}{{/each}}]
                    }
                  }
                }{{#unless @last}},{{/unless}}{{/each}}
              ]{{/if}}
            }
            """;
    }
}
