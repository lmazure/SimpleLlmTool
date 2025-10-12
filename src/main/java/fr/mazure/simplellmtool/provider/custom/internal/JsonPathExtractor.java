package fr.mazure.simplellmtool.provider.custom.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    static final Pattern validJsonPathPattern = Pattern.compile("((\\p{L}|\\d|_)+|\\[\\d+])" +        // first component
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

    /**
     * Extracts a JSON array from the given JSON string using the specified path.
     *
     * @param json the JSON string to extract from
     * @param path the JSON path to the array
     * @return a list of JsonNode elements from the array
     * @throws IOException if there is an error parsing the JSON
     * @throws JsonPathExtractorException if the path is invalid or the element is not found
     */
    public static List<JsonNode> extractArray(final String json,
                                              final String path) throws IOException, JsonPathExtractorException {
        final JsonNode rootNode = objectMapper.readTree(json);
        try {
            final JsonNode arrayNode = extract(rootNode, split(path), 0);
            if (!arrayNode.isArray()) {
                throw new JsonPathExtractorException("Path '" + path + "' does not point to an array", null);
            }
            final List<JsonNode> result = new ArrayList<>();
            arrayNode.forEach(result::add);
            return result;
        } catch (final JsonPathExtractorInternalException e) {
            throw new JsonPathExtractorException("Failed to extract JSON path '" + path + "', error occurred when retrieving element '" + e.getPartialPath() +"'", e.getCause());
        }
    }

    /**
     * Extracts a string value from a JsonNode using the specified path.
     *
     * @param node the JsonNode to extract from
     * @param path the JSON path to the string value
     * @return the extracted string value
     * @throws JsonPathExtractorException if the path is invalid or the element is not found
     */
    public static String extractFromNode(final JsonNode node,
                                         final String path) throws JsonPathExtractorException {
        try {
            return extract(node, split(path), 0).asText();
        } catch (final JsonPathExtractorInternalException e) {
            throw new JsonPathExtractorException("Failed to extract JSON path '" + path + "', error occurred when retrieving element '" + e.getPartialPath() +"'", e.getCause());
        }
    }

    /**
     * Extracts a JsonNode from a JsonNode using the specified path.
     *
     * @param node the JsonNode to extract from
     * @param path the JSON path to the target node
     * @return the extracted JsonNode
     * @throws JsonPathExtractorException if the path is invalid or the element is not found
     */
    public static JsonNode extractNodeFromNode(final JsonNode node,
                                               final String path) throws JsonPathExtractorException {
        try {
            return extract(node, split(path), 0);
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
            if (Objects.isNull(n)) {
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
        if (Objects.isNull(n)) {
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
