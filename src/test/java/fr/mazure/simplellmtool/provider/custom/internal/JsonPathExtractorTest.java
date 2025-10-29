package fr.mazure.simplellmtool.provider.custom.internal;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test class for the {@link JsonPathExtractor} utility. This class contains multiple test cases
 * to verify the functionality of the JsonPathExtractor. It includes tests for validating
 * JSON paths and extracting values from JSON strings using those paths. The tests cover
 * various scenarios, including valid and invalid JSON paths, extraction of scalar values,
 * arrays, nested structures, and real-world JSON examples.
 */
class JsonPathExtractorTest {

    @SuppressWarnings("static-method")
    @Test
    void validPath() {
        Assertions.assertTrue(JsonPathExtractor.isPathValid("a"));
        Assertions.assertTrue(JsonPathExtractor.isPathValid("abcde"));
        Assertions.assertTrue(JsonPathExtractor.isPathValid("a2cd3"));
        Assertions.assertTrue(JsonPathExtractor.isPathValid("a.bc.def"));
        Assertions.assertTrue(JsonPathExtractor.isPathValid("salut.à.toi.le.Français"));
        Assertions.assertTrue(JsonPathExtractor.isPathValid("[0]"));
        Assertions.assertTrue(JsonPathExtractor.isPathValid("[1][0]"));
        Assertions.assertTrue(JsonPathExtractor.isPathValid("[1][0][1984]"));
        Assertions.assertTrue(JsonPathExtractor.isPathValid("node[1]"));
        Assertions.assertTrue(JsonPathExtractor.isPathValid("node[1][0][1984]"));
        Assertions.assertTrue(JsonPathExtractor.isPathValid("node[1].subnode[0][1984]"));
        Assertions.assertTrue(JsonPathExtractor.isPathValid("node[1].subnode[0][1984].truc"));
        Assertions.assertTrue(JsonPathExtractor.isPathValid("node.field[1].subnode[0][1984].truc"));
        Assertions.assertTrue(JsonPathExtractor.isPathValid("[7].node.field[1].subnode[0][1984].truc"));
        Assertions.assertTrue(JsonPathExtractor.isPathValid("[7].node.field[1].subn0de[0][1984].truc"));
    }

    @SuppressWarnings("static-method")
    @Test
    void invalidPath() {
        Assertions.assertFalse(JsonPathExtractor.isPathValid(""));
        Assertions.assertFalse(JsonPathExtractor.isPathValid("salut.à.toi.l'Ukrainien"));
        Assertions.assertFalse(JsonPathExtractor.isPathValid("salut."));
        Assertions.assertFalse(JsonPathExtractor.isPathValid(".salut"));
        Assertions.assertFalse(JsonPathExtractor.isPathValid("salut..toi"));
        Assertions.assertFalse(JsonPathExtractor.isPathValid("[-0]"));
        Assertions.assertFalse(JsonPathExtractor.isPathValid("[]"));
        Assertions.assertFalse(JsonPathExtractor.isPathValid("[-1]"));
        Assertions.assertFalse(JsonPathExtractor.isPathValid(".[0]"));
        Assertions.assertFalse(JsonPathExtractor.isPathValid("[0]."));
        Assertions.assertFalse(JsonPathExtractor.isPathValid("[2][-1]"));
        Assertions.assertFalse(JsonPathExtractor.isPathValid("[1].[1]"));
        Assertions.assertFalse(JsonPathExtractor.isPathValid("[1][1]."));
        Assertions.assertFalse(JsonPathExtractor.isPathValid("[7]node.field[1].subnode[0][1984].truc"));
        Assertions.assertFalse(JsonPathExtractor.isPathValid("[7].node.field[1]subnode[0][1984].truc"));
        Assertions.assertFalse(JsonPathExtractor.isPathValid("[7].node.field[1].subnode[0][1984]truc"));
        Assertions.assertFalse(JsonPathExtractor.isPathValid("node.[1].subnode[0][1984]truc"));
        Assertions.assertFalse(JsonPathExtractor.isPathValid("node[1].subnode[0][1984].truc."));
        Assertions.assertFalse(JsonPathExtractor.isPathValid("node[1"));
        Assertions.assertFalse(JsonPathExtractor.isPathValid("node[-1]"));
        Assertions.assertFalse(JsonPathExtractor.isPathValid("node[]"));
    }

    @SuppressWarnings("static-method")
    @Test
    void basicScalarString() throws IOException,
                                    JsonPathExtractorException {
        final JsonNode node = parseJson("{\"a\": \"b\"}");
        Assertions.assertEquals("b",
                                JsonPathExtractor.extractString(node,
                                                           "a"));
    }

    @SuppressWarnings("static-method")
    @Test
    void basicScalarInteger() throws IOException,
                                     JsonPathExtractorException {
        final JsonNode node = parseJson("{\"a\": 62}");
        Assertions.assertEquals("62",
                                JsonPathExtractor.extractString(node,
                                                           "a"));
    }

    @SuppressWarnings("static-method")
    @Test
    void basicArrayString() throws IOException,
                                   JsonPathExtractorException {
        final JsonNode node = parseJson("[ \"aa\", \"bb\", \"cc\" ]");
        Assertions.assertEquals("bb",
                                JsonPathExtractor.extractString(node,
                                                                "[1]"));
    }

    @SuppressWarnings("static-method")
    @Test
    void basicArrayInteger() throws IOException,
                                    JsonPathExtractorException {
        final JsonNode node = parseJson("[ 11, 22, 33 ]");
        Assertions.assertEquals("22",
                                JsonPathExtractor.extractString(node,
                                                                "[1]"));
    }

    @SuppressWarnings("static-method")
    @Test
    void arrayInArray() throws IOException,
                               JsonPathExtractorException {
        final JsonNode node = parseJson("[ \"aa\", [ \"bb\", \"cc\", \"dd\", \"ee\" ], \"ff\" ]");
        Assertions.assertEquals("dd",
                                JsonPathExtractor.extractString(node,
                                                                "[1][2]"));
    }

    @SuppressWarnings("static-method")
    @Test
    void dictInDict() throws IOException,
                             JsonPathExtractorException {
        final JsonNode node = parseJson("{ \"aa\": { \"bb\": \"cc\", \"dd\": \"ee\", \"ff\": \"gg\" }, \"hh\": \"ii\" }");
        Assertions.assertEquals("ee",
                                JsonPathExtractor.extractString(node,
                                                                "aa.dd"));
    }

    @SuppressWarnings("static-method")
    @Test
    void dictInArray() throws IOException,
                              JsonPathExtractorException {
        final JsonNode node = parseJson("[ { \"aa\": \"bb\", \"cc\": \"dd\", \"ee\": \"ff\" }, { \"gg\": \"hh\", \"ii\": \"jj\", \"kk\": \"ll\" } ]");
        Assertions.assertEquals("jj",
                                JsonPathExtractor.extractString(node,
                                                                "[1].ii"));

    }

    @SuppressWarnings("static-method")
    @Test
    void arrayInDict() throws IOException,
                              JsonPathExtractorException {
        final JsonNode node = parseJson("{ \"aa\": \"bb\", \"cc\": [ \"dd\", \"ee\", \"ff\" ], \"gg\": \"hh\" }");
        Assertions.assertEquals("ff",
                                JsonPathExtractor.extractString(node,
                                                                "cc[2]"));

    }

    @SuppressWarnings("static-method")
    @Test
    void realWorld() throws IOException,
                            JsonPathExtractorException {
        final JsonNode node = parseJson("{\"name\":\"John\", \"age\":30, \"cars\":[\"Ford\", \"BMW\", \"Fiat\"]}");
        Assertions.assertEquals("Fiat",
                                JsonPathExtractor.extractString(node,
                                                          "cars[2]"));
    }

    @SuppressWarnings("static-method")
    @Test
    void realWorld2() throws IOException,
                             JsonPathExtractorException {
        final JsonNode node = parseJson("{\"name\":\"John\", \"age\":30, \"cars\":[ { \"model\":\"Ford\", \"year\":2017 }, { \"model\":\"BMW\", \"year\":2018 }, { \"model\":\"Fiat\", \"year\":2019 } ]}");
        Assertions.assertEquals("2017",
                                JsonPathExtractor.extractString(node,
                                                                "cars[0].year"));
    }

    @SuppressWarnings("static-method")
    @Test
    void handleInvalidArrayIndex() throws IOException {
        final JsonNode node = parseJson("{\"name\":\"John\", \"age\":30, \"cars\":[ { \"model\":\"Ford\", \"year\":2017 }, { \"model\":\"BMW\", \"year\":2018 }, { \"model\":\"Fiat\", \"year\":2019 } ]}");
        final Exception exception = Assertions.assertThrows(JsonPathExtractorException.class,
                                                            () -> JsonPathExtractor.extractString(node,
                                                                                                  "cars[4].year.dummy.field"));
        Assertions.assertEquals("Failed to extract JSON path 'cars[4].year.dummy.field', error occurred when retrieving element 'cars[4]'", exception.getMessage());
    }

    @SuppressWarnings("static-method")
    @Test
    void handleInvalidField() throws IOException {
        final JsonNode node = parseJson("{\"name\":\"John\", \"age\":30, \"cars\":[ { \"model\":\"Ford\", \"year\":2017 }, { \"model\":\"BMW\", \"year\":2018 }, { \"model\":\"Fiat\", \"year\":2019 } ]}");
        final Exception exception = Assertions.assertThrows(JsonPathExtractorException.class,
                                                            () -> JsonPathExtractor.extractString(node,
                                                                                                  "cars.year.dummy.field"));
        Assertions.assertEquals("Failed to extract JSON path 'cars.year.dummy.field', error occurred when retrieving element 'cars.year'", exception.getMessage());
    }

    @SuppressWarnings("static-method")
    @Test
    void handleInvalidField2() throws IOException {
        final JsonNode node = parseJson("{\"name\":\"John\", \"age\":30, \"cars\":[ { \"model\":\"Ford\", \"year\":2017 }, { \"model\":\"BMW\", \"year\":2018 }, { \"model\":\"Fiat\", \"year\":2019 } ]}");
        final Exception exception = Assertions.assertThrows(JsonPathExtractorException.class,
                                                            () -> JsonPathExtractor.extractString(node,
                                                                                                  "cars[0].years.dummy.field"));
        Assertions.assertEquals("Failed to extract JSON path 'cars[0].years.dummy.field', error occurred when retrieving element 'cars[0].years'", exception.getMessage());
    }

    @SuppressWarnings("static-method")
    @Test
    void handleInvalidField3() throws IOException {
        final JsonNode node = parseJson("{\"name\":\"John\", \"age\":30, \"cars\":[ { \"model\":\"Ford\", \"year\":2017 }, { \"model\":\"BMW\", \"year\":2018 }, { \"model\":\"Fiat\", \"year\":2019 } ]}");
        final Exception exception = Assertions.assertThrows(JsonPathExtractorException.class,
                                                            () -> JsonPathExtractor.extractString(node,
                                                                                                  "car[0].year.dummy.field"));
        Assertions.assertEquals("Failed to extract JSON path 'car[0].year.dummy.field', error occurred when retrieving element 'car'", exception.getMessage());
    }

    private static JsonNode parseJson(final String str) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(str);
    }
}
