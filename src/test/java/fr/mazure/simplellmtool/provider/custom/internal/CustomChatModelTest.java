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
        final CustomChatModel model = builGeminiModel();
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
        final CustomChatModel model = builGeminiModel();
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

    private static CustomChatModel builGeminiModel() {
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
                              .toolArgumentsDictPath(Optional.of("functionCall.args"))
                              .toolArgumentsStringPath(Optional.empty())
                              .build();
    }
}

