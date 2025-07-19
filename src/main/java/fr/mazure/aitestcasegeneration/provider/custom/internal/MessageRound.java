package fr.mazure.aitestcasegeneration.provider.custom.internal;

/**
 * Represents a round of message exchange containing the
 * actor of the message and the content of the message.
 */
public record MessageRound(MessageActor messageActor, String message) {
}