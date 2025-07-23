package fr.mazure.aitestcasegeneration.provider.custom.internal;

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
     * @return the evaluated template as a string
     */
    public static String generate(final String handlebarsTemplate,
                                  final List<MessageRound> messages,
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

            handlebars.registerHelper("convertToJson", new Helper<String>() {
                @Override
                public String apply(String text, Options options) {
                    return jsonConverter(text);
                }
            });

            final Template template = handlebars.compileInline(handlebarsTemplate);

            final Map<String, Object> context = new HashMap<>();
            context.put("messages", messages);
            context.put("apiKey", apiKey);

            return template.apply(context);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to process Handlebars template", e);
        }
    }

    /**
     * Convert a string to a JSON string (including the quotes)
     *
     * @param text the string to convert
     * @return the JSON string
     */
    private static String jsonConverter(final String text) {
        if (text == null) return null;
        final String escapedText = text.replace("\\", "\\\\")
                                       .replace("\"", "\\\"")
                                       .replace("\b", "\\b")
                                       .replace("\f", "\\f")
                                       .replace("\n", "\\n")
                                       .replace("\r", "\\r")
                                       .replace("\t", "\\t");
        return "\"" + escapedText + "\"";
    };
}
