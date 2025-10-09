package fr.mazure.simplellmtool.provider.custom.internal;

import java.util.List;

/*
 * Represents a tool call in a round of message exchange
 */
public record MessageRoundToolCall(String toolName, List<MessageRoundToolPamameter> toolParameters) {
}
