package fr.mazure.simplellmtool.provider.custom.internal;

import java.util.List;

/**
 * Represents a round of message exchange.
 */
public record MessageRound(Role role, String content, List<ToolCall> toolCalls, String tool) {

    public MessageRound(final Role role, final String content, List<ToolCall> toolCalls) {
        this(role, content, toolCalls, null);
    }

    public MessageRound(final Role role, final String content) {
        this(role, content, List.of(), null);
    }

    public MessageRound(final Role role, final String content, String tool) {
        this(role, content, List.of(), tool);
    }

    /*
     * Represents a tool call in a round of message exchange
     */
    public record ToolCall(String toolName, List<ToolParameter> toolParameters) {
    }
    
    /*
     * Represents a paramteter in a tool call in a round of message exchange
     */
    public record ToolParameter(String parameterName, String parameterValue) {
    }
}
