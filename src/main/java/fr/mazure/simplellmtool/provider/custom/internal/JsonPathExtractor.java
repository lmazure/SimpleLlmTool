package fr.mazure.simplellmtool.provider.custom.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;

/*
 * Utility methods to extract data from a JSON payloac
 */
class JsonPathExtractor {

    private static class JsonPathExtractorInternalException extends Exception {

        private final String partialPath;

        JsonPathExtractorInternalException(final String message,
                                           final String partialPath) {
            super(message);
            this.partialPath = partialPath;
        }

        String getPartialPath() {
            return this.partialPath;
        }
    }

    // In theory, a JSON field name can contain any character, but we will limit ourselves to alphanumeric and underscore characters
    static final Pattern validJsonPathPattern = Pattern.compile("((\\p{L}|\\d|_)+|\\[\\d+])" +        // first component
                                                                "(\\.(\\p{L}|\\d|_)+|\\[\\d+])*");    // following components

    static boolean isPathValid(final String path) {
        return validJsonPathPattern.matcher(path).matches();
    }

    /**
     * Extracts a string from a JsonNode using the specified path.
     *
     * @param rootNode the JSON node to extract from
     * @param path the JSON path to the string
     * @return the extracted string
     * @throws JsonPathExtractorException if the path is invalid or the element is not found
     */
    static String extractString(final JsonNode rootNode,
                                final String path) throws JsonPathExtractorException {
        try {
            return extract(rootNode, split(path), 0).asText();
        } catch (final JsonPathExtractorInternalException e) {
            throw new JsonPathExtractorException("Failed to extract JSON path '" + path + "', error occurred when retrieving element '" + e.getPartialPath() +"'", e.getCause());
        }
    }

    /**
     * Extracts a JSON array from a JsonNode using the specified path.
     *
     * @param rootNode the JSON node to extract from
     * @param path the JSON path to the array
     * @return a list of JsonNode elements from the array
     * @throws JsonPathExtractorException if the path is invalid or the element is not found
     */
    static List<JsonNode> extractArray(final JsonNode rootNode,
                                       final String path) throws JsonPathExtractorException {
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
     * Extracts a JsonNode from a JsonNode using the specified path.
     *
     * @param node the JsonNode to extract from
     * @param path the JSON path to the target node
     * @return the extracted JsonNode
     * @throws JsonPathExtractorException if the path is invalid or the element is not found
     */
    static JsonNode extractNode(final JsonNode node,
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
