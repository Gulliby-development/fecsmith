package io.github.gulliby.fecsmith.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class TextTest {
    @Test
    void showQuotesAndPreservesPrintableAsciiBoundaries() {
        assertEquals("\"\"", Text.show(""));
        assertEquals("\"!~\"", Text.show("!~"));
        assertEquals("\"A?B???\"", Text.show("A B\n😀é"));
        assertEquals("\"A?B??????~\"", Text.show("A B\n😀é\t\r\u007f~"));
        assertEquals("\"?\"", Text.show("😀"));
        assertEquals("\"quote:?\"?and?slash:?\\\"", Text.show("quote: \" and slash: \\"));
    }

    @Test
    void listOrFormatsOneThroughFourItemsWithoutAnOxfordComma() {
        // Literal cases: listOr([A]), listOr([A,B]) and listOr([A,B,C]).
        assertEquals("A", Text.listOr(List.of("A")));
        assertEquals("A or B", Text.listOr(List.of("A", "B")));
        assertEquals("A, B or C", Text.listOr(List.of("A", "B", "C")));
        assertEquals("A, B, C or D", Text.listOr(List.of("A", "B", "C", "D")));
    }

    @Test
    void pluralUsesSingularOnlyForOne() {
        // Literal cases: plural(1,byte,bytes) and plural(2,byte,bytes).
        assertEquals("1 byte", Text.plural(1, "byte", "bytes"));
        assertEquals("0 bytes", Text.plural(0, "byte", "bytes"));
        assertEquals("2 bytes", Text.plural(2, "byte", "bytes"));
        assertEquals("-1 bytes", Text.plural(-1, "byte", "bytes"));
    }

    @Test
    void renderTableAlignsColumnsUsingTheWidestHeaderOrCell() {
        // Literal case: header [a,bb], rows [[long,x]].
        assertEquals(List.of("a     bb", "long  x"),
                Text.renderTable(List.of("a", "bb"), List.of(List.of("long", "x"))));
    }

    @Test
    void renderTableHandlesHeaderOnlyAndEmptyCellsWithoutTrailingSpaces() {
        List<String> headerOnly = Text.renderTable(List.of("wide", "x", "c"), List.of());
        assertEquals(List.of("wide  x  c"), headerOnly);
        assertFalse(headerOnly.get(0).contains("\n"));
        assertFalse(headerOnly.get(0).endsWith(" "));

        List<String> withEmptyCells = Text.renderTable(
                List.of("a", "bb"),
                List.of(List.of("long", ""), List.of("", "x")));
        assertEquals(List.of("a     bb", "long", "      x"), withEmptyCells);
        assertFalse(withEmptyCells.get(1).endsWith(" "));
    }

    @Test
    void linePadsShortKeysToEightCharactersAndLeavesEightCharacterKeysUnpadded() {
        // Literal cases: line(data,10110) and line(codeword,0011001100).
        assertEquals("data      10110", Text.line("data", "10110"));
        assertEquals("codeword  0011001100", Text.line("codeword", "0011001100"));
        assertEquals("          value", Text.line("", "value"));
    }

    @Test
    void usagePreservesItsExactMessage() {
        Failure.Usage failure = new Failure.Usage("invalid value \"x\"");
        assertEquals("invalid value \"x\"", failure.getMessage());
        assertTrue(failure instanceof RuntimeException);
    }
}
