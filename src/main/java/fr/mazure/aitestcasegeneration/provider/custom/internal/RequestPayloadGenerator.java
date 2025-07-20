package fr.mazure.aitestcasegeneration.provider.custom.internal;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.EscapingStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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

            // Auto-escape JSON
            handlebars.with(new EscapingStrategy() {
                @Override
                public String escape(CharSequence value) {
                    return jsonEscaper.apply(value.toString());
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
     * Escapes a string for JSON.
     *
     * @param text the string to escape
     * @return the escaped string
     */
    private static Function<String, String> jsonEscaper = (text) -> {
        if (text == null) return null;
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\b", "\\b")
                   .replace("\f", "\\f")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    };
}
