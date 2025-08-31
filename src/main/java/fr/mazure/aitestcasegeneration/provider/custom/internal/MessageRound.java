package fr.mazure.simplellmtool.provider.custom.internal;

/**
 * Represents a round of message exchange containing the
 * role of the message and the content of the message.
 */
public record MessageRound(Role role, String content) {
}