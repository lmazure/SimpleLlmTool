package fr.mazure.simplellmtool.provider.custom.internal;

import java.util.List;

/**
 * Represents a round of message exchange.
 */
public record MessageRound(Role role, String content, List<MessageRoundToolCall> toolCalls, String tool) {

    public MessageRound(final Role role, final String content, List<MessageRoundToolCall> toolCalls) {
        this(role, content, toolCalls, null);
    }

    public MessageRound(final Role role, final String content) {
        this(role, content, List.of(), null);
    }

    public MessageRound(final Role role, final String content, String tool) {
        this(role, content, List.of(), tool);
    }
}
