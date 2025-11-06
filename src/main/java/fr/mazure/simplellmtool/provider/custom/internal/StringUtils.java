package fr.mazure.simplellmtool.provider.custom.internal;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Utility class for string operations
 */
class StringUtils {

    /**
     * Adds line numbers to the input string.
     */
    static String addLineNumbers(final String input) {
        final AtomicInteger counter = new AtomicInteger();
        return input.lines()
                    .map(line -> "%03d %s".formatted(Integer.valueOf(counter.incrementAndGet()), line))
                    .collect(Collectors.joining("\n"));
    }

    /**
     * Escapes a string for JSON
     *
     * @param input the string to escape
     * @return the escaped string
     */
    static String escapeStringForJson(final String input) {
        final StringBuilder escaped = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            final char c = input.charAt(i);

            switch (c) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        // Only escape control characters (0x00-0x1F)
                        escaped.append(String.format("\\u%04x", Integer.valueOf(c)));
                    } else {
                        // Preserve all other characters including emojis
                        escaped.append(c);
                    }
                    break;
            }
        }

        return escaped.toString();
    }
}