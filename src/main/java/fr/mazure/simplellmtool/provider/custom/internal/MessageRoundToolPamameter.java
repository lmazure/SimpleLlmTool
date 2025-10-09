package fr.mazure.simplellmtool.provider.custom.internal;

/*
 * Represents a paramteter in a tool call in a round of message exchange
 */
public record MessageRoundToolPamameter(String parameterName, String parameterValue) {
}
