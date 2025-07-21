package fr.mazure.aitestcasegeneration.provider.custom.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonPathExtractor {

    // In theory, A JSON field name can contain any character, but we will limit ourselves to alphanumeric and underscore characters
    final static Pattern validJsonPathPattern = Pattern.compile("((\\p{L}|\\d|_)+|\\[\\d+])" +        // first component
                                                                "(\\.(\\p{L}|\\d|_)+|\\[\\d+])*");    // following components

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static boolean isPathValid(final String path) {
        return validJsonPathPattern.matcher(path).matches();
    }

    public static String extract(final String json,
                                 final String path) throws IOException {
        final JsonNode rootNode = objectMapper.readTree(json);
        return extract(rootNode, split(path), 0).asText();
    }

    private static JsonNode extract(final JsonNode node,
                                    final List<String> pathParts,
                                    final int startIndex) {
        if (startIndex == pathParts.size()) {
            return node;
        }
        final String part = pathParts.get(startIndex);
        if (part.startsWith("[")) {
        	// array index (e.g. "[1]")
            final int index = Integer.parseInt(part.substring(1, part.length() - 1));
            return extract(node.get(index), pathParts, startIndex + 1);
        } else {
            // name of a scalar field (e.g. "foo")
            return extract(node.get(part), pathParts, startIndex + 1);
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
