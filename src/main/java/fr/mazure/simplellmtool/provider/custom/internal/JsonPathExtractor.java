package fr.mazure.simplellmtool.provider.custom.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonPathExtractor {

    private static class JsonPathExtractorInternalException extends Exception {

        private final String partialPath;

        public JsonPathExtractorInternalException(final String message,
                                                  final String partialPath) {
            super(message);
            this.partialPath = partialPath;
        }

        public String getPartialPath() {
            return this.partialPath;
        }
    }

    // In theory, a JSON field name can contain any character, but we will limit ourselves to alphanumeric and underscore characters
    final static Pattern validJsonPathPattern = Pattern.compile("((\\p{L}|\\d|_)+|\\[\\d+])" +        // first component
                                                                "(\\.(\\p{L}|\\d|_)+|\\[\\d+])*");    // following components

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static boolean isPathValid(final String path) {
        return validJsonPathPattern.matcher(path).matches();
    }

    public static String extract(final String json,
                                 final String path) throws IOException, JsonPathExtractorException {
        final JsonNode rootNode = objectMapper.readTree(json);
        try {
            return extract(rootNode, split(path), 0).asText();
        } catch (final JsonPathExtractorInternalException e) {
            throw new JsonPathExtractorException("Failed to extract JSON path '" + path + "', error occurred when retrieving element '" + e.getPartialPath() +"'", e.getCause());
        }
    }

    private static JsonNode extract(final JsonNode node,
                                    final List<String> pathParts,
                                    final int startIndex) throws JsonPathExtractorInternalException {
        if (startIndex == pathParts.size()) {
            return node;
        }
        final String part = pathParts.get(startIndex);
        if (part.startsWith("[")) {
            // array index (e.g. "[1]")
            final int index = Integer.parseInt(part.substring(1, part.length() - 1));
            final JsonNode n = node.get(index);
            if (n == null) {
                throw new JsonPathExtractorInternalException("Index " + index + " not found in array", part);
            }
            try {
                return extract(n, pathParts, startIndex + 1);
            } catch (final JsonPathExtractorInternalException e) {
                final String sep = e.getPartialPath().startsWith("[") ? "" : ".";
                throw new JsonPathExtractorInternalException(e.getMessage(), part + sep + e.getPartialPath());
            }
        }
        // name of a scalar field (e.g. "foo")
        final JsonNode n = node.get(part);
        if (n == null) {
            throw new JsonPathExtractorInternalException("'" + part + "' field not found", part);
        }
        try {
            return extract(n, pathParts, startIndex + 1);
        } catch (final JsonPathExtractorInternalException e) {
            final String sep = e.getPartialPath().startsWith("[") ? "" : ".";
            throw new JsonPathExtractorInternalException(e.getMessage(), part + sep + e.getPartialPath());
        }
    }

    private static List<String> split(final String str) {
        final Matcher m = Pattern.compile("\\[\\d+]|\\w+").matcher(str);
        final List<String> matches = new ArrayList<>();
        while (m.find()) {
            matches.add(m.group());
        }
        return matches;
    }
}
