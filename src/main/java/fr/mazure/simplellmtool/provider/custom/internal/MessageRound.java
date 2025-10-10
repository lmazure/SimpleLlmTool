package fr.mazure.simplellmtool.provider.custom.internal;

import java.util.List;

/**
 * Represents a round of message exchange containing the
 * role of the message and the content of the message.
 */
public record MessageRound(Role role, String content, List<MessageRoundToolCall> toolCalls, String tool) {
}
