package fr.mazure.simplellmtool.provider.custom.internal;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.FinishReason;

class CustomChatModelTest {

    @SuppressWarnings("static-method")
    @Test
    @DisplayName("Can parse Gemini tool calls")
    void canParseGeminiToolCalls() throws IOException,
                                          JsonPathExtractorException {
        // Given
        final CustomChatModel model = buildGeminiModel();
        final String answer = """
            {
              "candidates": [
                {
                  "content": {
                    "parts": [
                      {
                        "functionCall": {
                          "name": "get_weather",
                          "args": {
                            "city": "Paris"
                          }
                        },
                        "thoughtSignature": "CtUCAdHtim8S3wOMH2ZYGf6AgxEYgv7debJzHTkf3K31Tcq+/VVVIFa2L2duh8Gp/899yCHaXM1NYAWptH4BsONXSFEaHYL7wno2r5ccy2YbBbY33pvG8CU15+hTANQVklsuyiyrNiWHHy7tPRB9H7Y+T3exjTzCWYmtAH8rPx+67FiAwLcGoxkk1WovYcr34H0qxff8kyk/tkJ9CnInXJBnlIfoTsNLoFC7NGorfVNTV67EnLEZztwmXF4ufUaPiiDtixA7lYTWgH3+pUuBIkCetAy2RRsY1lK8xEXTkJspRc2z7fpBf++5iK31b5Ej5g39vjy9wlCwlmuZ07KPL01tn19otIXqhwY+iPz4BDkRbUdeKukhr19lyKpxv/6hnAvHQOqqA7ceH8XLrL2MHCt5xXihNY8TDlldA0xZgyoNmXpHjo/hCv/xgcy5lfIqdnXryaQWTmE="
                      }
                    ],
                    "role": "model"
                  },
                  "finishReason": "STOP",
                  "index": 0,
                  "finishMessage": "Model generated function call(s)."
                }
              ],
              "usageMetadata": {
                "promptTokenCount": 215,
                "candidatesTokenCount": 15,
                "totalTokenCount": 306,
                "promptTokensDetails": [
                  {
                    "modality": "TEXT",
                    "tokenCount": 215
                  }
                ],
                "thoughtsTokenCount": 76
              },
              "modelVersion": "gemini-2.5-flash",
              "responseId": "HDHxaL_RJq-jvdIP8pX0uQg"
            }
            """;

        // When
        final ChatResponse response = model.parseApiResponse(answer);

        // Then
        Assertions.assertEquals(null, response.aiMessage().text());
        Assertions.assertTrue(response.aiMessage().hasToolExecutionRequests());
        Assertions.assertEquals(1, response.aiMessage().toolExecutionRequests().size());
        Assertions.assertEquals("get_weather", response.aiMessage().toolExecutionRequests().get(0).name());
        Assertions.assertEquals("{\"city\":\"Paris\"}", response.aiMessage().toolExecutionRequests().get(0).arguments());
        Assertions.assertEquals(215, response.tokenUsage().inputTokenCount());
        Assertions.assertEquals(15, response.tokenUsage().outputTokenCount());
        Assertions.assertEquals(FinishReason.STOP, response.finishReason());
    }

    @SuppressWarnings("static-method")
    @Test
    @DisplayName("Can parse Gemini final answer")
    void canParseGeminiFinalAnswer() throws IOException,
                                            JsonPathExtractorException {
        // Given
        final CustomChatModel model = buildGeminiModel();
        final String answer = """
            {
              "candidates": [
                {
                  "content": {
                    "parts": [
                      {
                        "text": "The weather in Paris is 13.9°C, partly cloudy, and feels like 12.5°C with 73% humidity.\\n\\nLe temps à Paris est de 13,9°C, partiellement nuageux, et la sensation est de 12,5°C avec 73% d'humidité."
                      }
                    ],
                    "role": "model"
                  },
                  "finishReason": "STOP",
                  "index": 0
                }
              ],
              "usageMetadata": {
                "promptTokenCount": 282,
                "candidatesTokenCount": 74,
                "totalTokenCount": 356,
                "promptTokensDetails": [
                  {
                    "modality": "TEXT",
                    "tokenCount": 282
                  }
                ]
              },
              "modelVersion": "gemini-2.5-flash",
              "responseId": "FkPxaJHgB5G0nsEP6bqH4QM"
            }
            """;

        // When
        final ChatResponse response = model.parseApiResponse(answer);

        // Then
        Assertions.assertEquals("The weather in Paris is 13.9°C, partly cloudy, and feels like 12.5°C with 73% humidity.\n\nLe temps à Paris est de 13,9°C, partiellement nuageux, et la sensation est de 12,5°C avec 73% d'humidité.", response.aiMessage().text());
        Assertions.assertFalse(response.aiMessage().hasToolExecutionRequests());
        Assertions.assertEquals(282, response.tokenUsage().inputTokenCount());
        Assertions.assertEquals(74, response.tokenUsage().outputTokenCount());
        Assertions.assertEquals(FinishReason.STOP, response.finishReason());
    }

    @SuppressWarnings("static-method")
    @Test
    @DisplayName("Can parse GPT-5 tool calls")
    void canParseGPT5ToolCalls() throws IOException,
                                        JsonPathExtractorException {
        // Given
        final CustomChatModel model = buildGPT5Model();
        final String answer = """
            {
              "id": "chatcmpl-CXOK93wMx3plCJWQw7IjVDwRDBh5B",
              "object": "chat.completion",
              "created": 1762074361,
              "model": "gpt-5-nano-2025-08-07",
              "choices": [
                {
                  "index": 0,
                  "message": {
                    "role": "assistant",
                    "content": null,
                    "tool_calls": [
                      {
                        "id": "call_t2CEgA6YKxLRDnU6IuwEcch4",
                        "type": "function",
                        "function": {
                          "name": "get_weather",
                          "arguments": "{\\"city\\":\\"Paris\\"}"
                        }
                      }
                    ],
                    "refusal": null,
                    "annotations": []
                  },
                  "finish_reason": "tool_calls"
                }
              ],
              "usage": {
                "prompt_tokens": 291,
                "completion_tokens": 87,
                "total_tokens": 378,
                "prompt_tokens_details": {
                  "cached_tokens": 0,
                  "audio_tokens": 0
                },
                "completion_tokens_details": {
                  "reasoning_tokens": 64,
                  "audio_tokens": 0,
                  "accepted_prediction_tokens": 0,
                  "rejected_prediction_tokens": 0
                }
              },
              "service_tier": "default",
              "system_fingerprint": null
            }
            """;

        // When
        final ChatResponse response = model.parseApiResponse(answer);

        // Then
        Assertions.assertEquals(null, response.aiMessage().text());
        Assertions.assertTrue(response.aiMessage().hasToolExecutionRequests());
        Assertions.assertEquals(1, response.aiMessage().toolExecutionRequests().size());
        Assertions.assertEquals("get_weather", response.aiMessage().toolExecutionRequests().get(0).name());
        Assertions.assertEquals("call_t2CEgA6YKxLRDnU6IuwEcch4", response.aiMessage().toolExecutionRequests().get(0).id());
        Assertions.assertEquals("{\"city\":\"Paris\"}", response.aiMessage().toolExecutionRequests().get(0).arguments());
        Assertions.assertEquals(291, response.tokenUsage().inputTokenCount());
        Assertions.assertEquals(87, response.tokenUsage().outputTokenCount());
        Assertions.assertEquals(FinishReason.TOOL_EXECUTION, response.finishReason());
    }

    private static CustomChatModel buildGeminiModel() {
        return CustomChatModel.builder()
                              .baseUrl("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent")
                              .modelName("unused")
                              .apiKey("GOOGLE_GEMINI_API_KEY")
                              .payloadTemplate("unused")
                              .httpHeaders(Map.of())
                              .answerPath("candidates[0].content.parts[0].text")
                              .inputTokenPath("usageMetadata.promptTokenCount")
                              .outputTokenPath("usageMetadata.candidatesTokenCount")
                              .finishReasonPath("candidates[0].finishReason")
                              .finishReasonMappings(Map.of("STOP", CustomChatModel.FinishingReason.DONE))
                              .toolCallsPath("candidates[0].content.parts")
                              .toolNamePath("functionCall.name")
                              .toolCallIdPath(Optional.empty())
                              .toolArgumentsDictPath(Optional.of("functionCall.args"))
                              .toolArgumentsStringPath(Optional.empty())
                              .build();
    }

    private static CustomChatModel buildGPT5Model() {
        return CustomChatModel.builder()
                              .baseUrl("https://api.openai.com/v1/chat/completions")
                              .modelName("gpt-5-nano-2025-08-07")
                              .apiKey("OPENAI_API_KEY")
                              .payloadTemplate("unused")
                              .httpHeaders(Map.of())
                              .answerPath("choices[0].message.content")
                              .inputTokenPath("usage.prompt_tokens")
                              .outputTokenPath("usage.completion_tokens")
                              .finishReasonPath("choices[0].finish_reason")
                              .finishReasonMappings(Map.of("stop", CustomChatModel.FinishingReason.DONE,
                                                           "length", CustomChatModel.FinishingReason.MAX_TOKENS,
                                                           "tool_calls", CustomChatModel.FinishingReason.TOOL_CALL))
                              .toolCallsPath("choices[0].message.tool_calls")
                              .toolNamePath("function.name")
                              .toolCallIdPath(Optional.of("id"))
                              .toolArgumentsDictPath(Optional.empty())
                              .toolArgumentsStringPath(Optional.of("function.arguments"))
                              .build();
    }
}

