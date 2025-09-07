package fr.mazure.simplellmtool.provider.custom.internal;

import com.github.jknack.handlebars.EscapingStrategy;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.Template;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestPayloadGenerator {

    /**
     * Generates a payload by evaluating a Handlebars template with the provided messages.
     *
     * @param handlebarsTemplate the Handlebars template string to evaluate
     * @param messages the list of message rounds to use as template data
     * @param modelName the name of the model
     * @param apiKey the API key
     * @return the evaluated template as a string
     */
    public static String generate(final String handlebarsTemplate,
                                  final List<MessageRound> messages,
                                  final String modelName,
                                  final String apiKey) {
        try {
            final Handlebars handlebars = new Handlebars();

            handlebars.with(EscapingStrategy.NOOP);

            // Register boolean helpers for each role
            handlebars.registerHelper("isSystem", new Helper<Role>() {
                @Override
                public Boolean apply(Role role, Options options) {
                    return Role.SYSTEM.equals(role);
                }
            });

            handlebars.registerHelper("isUser", new Helper<Role>() {
                @Override
                public Boolean apply(Role role, Options options) {
                    return Role.USER.equals(role);
                }
            });

            handlebars.registerHelper("isModel", new Helper<Role>() {
                @Override
                public Boolean apply(Role role, Options options) {
                    return Role.MODEL.equals(role);
                }
            });

            handlebars.registerHelper("convertToJsonString", new Helper<String>() {
                @Override
                public String apply(String text, Options options) {
                    return jsonConverter(text);
                }
            });

            final Template template = handlebars.compileInline(handlebarsTemplate);

            final Map<String, Object> context = new HashMap<>();
            context.put("messages", messages);
            context.put("modelName", modelName);
            context.put("apiKey", apiKey);

            return template.apply(context);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to process Handlebars template", e);
        }
    }

    /**
     * Convert a string to a JSON string (including the enclosing quotes)
     *
     * @param text the string to convert
     * @return the JSON string
     */
    private static String jsonConverter(final String input) {
        if (input == null) {
            return null;
        }

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
                        escaped.append(String.format("\\u%04x", (int) c));
                    } else {
                        // Preserve all other characters including emojis
                        escaped.append(c);
                    }
                    break;
            }
        }

        return "\"" + escaped.toString() + "\"";
    }
}
