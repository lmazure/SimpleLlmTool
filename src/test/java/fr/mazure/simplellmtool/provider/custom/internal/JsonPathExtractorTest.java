package fr.mazure.simplellmtool.provider.custom.internal;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
    void basicScalarString() throws IOException, JsonPathExtractorException {
        Assertions.assertEquals("b",
                                JsonPathExtractor.extract("{\"a\": \"b\"}",
                                                          "a"));
    }

    @SuppressWarnings("static-method")
    @Test
    void basicScalarInteger() throws IOException, JsonPathExtractorException {
        Assertions.assertEquals("62",
                                JsonPathExtractor.extract("{\"a\": 62}",
                                                          "a"));
    }

    @SuppressWarnings("static-method")
    @Test
    void basicArrayString() throws IOException, JsonPathExtractorException {
        Assertions.assertEquals("bb",
                                JsonPathExtractor.extract("[ \"aa\", \"bb\", \"cc\" ]",
                                                          "[1]"));
    }

    @SuppressWarnings("static-method")
    @Test
    void basicArrayInteger() throws IOException, JsonPathExtractorException {
        Assertions.assertEquals("22",
                                JsonPathExtractor.extract("[ 11, 22, 33 ]",
                                                          "[1]"));
    }

    @SuppressWarnings("static-method")
    @Test
    void arrayInArray() throws IOException, JsonPathExtractorException {
        Assertions.assertEquals("dd",
                                JsonPathExtractor.extract("[ \"aa\", [ \"bb\", \"cc\", \"dd\", \"ee\" ], \"ff\" ]",
                                                          "[1][2]"));
    }

    @SuppressWarnings("static-method")
    @Test
    void dictInDict() throws IOException, JsonPathExtractorException {
        Assertions.assertEquals("ee",
                                JsonPathExtractor.extract("{ \"aa\": { \"bb\": \"cc\", \"dd\": \"ee\", \"ff\": \"gg\" }, \"hh\": \"ii\" }",
                                                          "aa.dd"));
    }

    @SuppressWarnings("static-method")
    @Test
    void dictInArray() throws IOException, JsonPathExtractorException {
        Assertions.assertEquals("jj",
                                JsonPathExtractor.extract("[ { \"aa\": \"bb\", \"cc\": \"dd\", \"ee\": \"ff\" }, { \"gg\": \"hh\", \"ii\": \"jj\", \"kk\": \"ll\" } ]",
                                                          "[1].ii"));

    }

    @SuppressWarnings("static-method")
    @Test
    void arrayInDict() throws IOException, JsonPathExtractorException {
        Assertions.assertEquals("ff",
                                JsonPathExtractor.extract("{ \"aa\": \"bb\", \"cc\": [ \"dd\", \"ee\", \"ff\" ], \"gg\": \"hh\" }",
                                                          "cc[2]"));

    }

    @SuppressWarnings("static-method")
    @Test
    void realWorld() throws IOException, JsonPathExtractorException {
        Assertions.assertEquals("Fiat",
                                JsonPathExtractor.extract("{\"name\":\"John\", \"age\":30, \"cars\":[\"Ford\", \"BMW\", \"Fiat\"]}",
                                                          "cars[2]"));
    }

    @SuppressWarnings("static-method")
    @Test
    void realWorld2() throws IOException, JsonPathExtractorException {
        Assertions.assertEquals("2017",
                                JsonPathExtractor.extract("{\"name\":\"John\", \"age\":30, \"cars\":[ { \"model\":\"Ford\", \"year\":2017 }, { \"model\":\"BMW\", \"year\":2018 }, { \"model\":\"Fiat\", \"year\":2019 } ]}",
                                                          "cars[0].year"));
    }
}
