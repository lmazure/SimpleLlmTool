package fr.mazure.aitestcasegeneration.provider.custom.internal;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class RequestPayloadGenerator {

    /**
     * Generates a payload by evaluating a Mustache template with the provided messages.
     *
     * @param mustacheTemplate the Mustache template string to evaluate
     * @param messages the list of message rounds to use as template data
     * @return the evaluated template as a string
     */
    public static String generate(final String mustacheTemplate,
                                  final List<MessageRound> messages) {

        try {
            final MustacheFactory mf = new DefaultMustacheFactory() {
                @Override
                public void encode(final String value, final Writer writer) {
                    try {
                        writer.write(jsonEscaper.apply(value));
                    } catch (final IOException e) {
                        e.printStackTrace();
                        System.exit(-1);
                    }
                }
            };
            final Mustache mustache = mf.compile(new StringReader(mustacheTemplate), "template");

            // Create the context map for the template
            final Map<String, Object> context = new HashMap<>();
            context.put("messages", messages);

            final StringWriter writer = new StringWriter();
            mustache.execute(writer, context);

            return writer.toString();
        } catch (final Exception e) {
            throw new RuntimeException("Failed to process Mustache template", e);
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
