package fr.mazure.simplellmtool;

/**
 * Enum for the log levels.
 */
public enum LogLevel {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR;

    public static LogLevel fromString(final String name) throws IllegalArgumentException {
        return valueOf(name.toUpperCase());
    }
}
