package fr.mazure.aitestcasegeneration.provider.custom.internal;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JsonPathExtractorTest {

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

    @Test
    void basicScalarString() throws IOException {
        Assertions.assertEquals("b",
                                JsonPathExtractor.extract("{\"a\": \"b\"}",
                                                          "a"));
    }

    @Test
    void basicScalarInteger() throws IOException {
        Assertions.assertEquals("62",
                                JsonPathExtractor.extract("{\"a\": 62}",
                                                          "a"));
    }

    @Test
    void basicArrayString() throws IOException {
        Assertions.assertEquals("bb",
                                JsonPathExtractor.extract("[ \"aa\", \"bb\", \"cc\" ]",
                                                          "[1]"));
    }

    @Test
    void basicArrayInteger() throws IOException {
        Assertions.assertEquals("22",
                                JsonPathExtractor.extract("[ 11, 22, 33 ]",
                                                          "[1]"));
    }

    @Test
    void arrayInArray() throws IOException {
        Assertions.assertEquals("dd",
                                JsonPathExtractor.extract("[ \"aa\", [ \"bb\", \"cc\", \"dd\", \"ee\" ], \"ff\" ]",
                                                          "[1][2]"));
    }

    @Test
    void dictInDict() throws IOException {
        Assertions.assertEquals("ee",
                                JsonPathExtractor.extract("{ \"aa\": { \"bb\": \"cc\", \"dd\": \"ee\", \"ff\": \"gg\" }, \"hh\": \"ii\" }",
                                                          "aa.dd"));
    }

    @Test
    void dictInArray() throws IOException {
        Assertions.assertEquals("jj",
                                JsonPathExtractor.extract("[ { \"aa\": \"bb\", \"cc\": \"dd\", \"ee\": \"ff\" }, { \"gg\": \"hh\", \"ii\": \"jj\", \"kk\": \"ll\" } ]",
                                                          "[1].ii"));

    }

    @Test
    void arrayInDict() throws IOException {
        Assertions.assertEquals("ff",
                                JsonPathExtractor.extract("{ \"aa\": \"bb\", \"cc\": [ \"dd\", \"ee\", \"ff\" ], \"gg\": \"hh\" }",
                                                          "cc[2]"));

    }

    @Test
    void realWorld() throws IOException {
        Assertions.assertEquals("Fiat",
                                JsonPathExtractor.extract("{\"name\":\"John\", \"age\":30, \"cars\":[\"Ford\", \"BMW\", \"Fiat\"]}",
                                                          "cars[2]"));
    }

    @Test
    void realWorld2() throws IOException {
        Assertions.assertEquals("2017",
                                JsonPathExtractor.extract("{\"name\":\"John\", \"age\":30, \"cars\":[ { \"model\":\"Ford\", \"year\":2017 }, { \"model\":\"BMW\", \"year\":2018 }, { \"model\":\"Fiat\", \"year\":2019 } ]}",
                                                          "cars[0].year"));
    }
}
