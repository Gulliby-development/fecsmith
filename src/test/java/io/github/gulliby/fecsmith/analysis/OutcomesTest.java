package io.github.gulliby.fecsmith.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class OutcomesTest {
    @Test
    void kOneWithThreeErrorsMatchesEveryLiteralRowAtTheLowerBoundary() {
        // Literal rows: (1,3) -> (errors, patterns, corrected, detected, wrong).
        assertEquals(List.of(
                new Outcomes.Row(1, 4, 4, 0, 0),
                new Outcomes.Row(2, 6, 0, 6, 0),
                new Outcomes.Row(3, 4, 0, 0, 4)),
                Outcomes.run(1, 3));
    }

    @Test
    void kFiveWithOneErrorMatchesTheCompleteLiteralTable() {
        // Literal row: (5,1) -> (errors, patterns, corrected, detected, wrong).
        assertEquals(List.of(
                new Outcomes.Row(1, 10, 10, 0, 0)),
                Outcomes.run(5, 1));
    }

    @Test
    void kFiveWithTwoErrorsMatchesTheCompleteLiteralTable() {
        // Literal rows: (5,2) -> (errors, patterns, corrected, detected, wrong).
        assertEquals(List.of(
                new Outcomes.Row(1, 10, 10, 0, 0),
                new Outcomes.Row(2, 45, 0, 45, 0)),
                Outcomes.run(5, 2));
    }

    @Test
    void kFiveWithThreeErrorsMatchesEveryLiteralRowIncludingMiscorrections() {
        // Literal rows: (5,3) -> (errors, patterns, corrected, detected, wrong).
        assertEquals(List.of(
                new Outcomes.Row(1, 10, 10, 0, 0),
                new Outcomes.Row(2, 45, 0, 45, 0),
                new Outcomes.Row(3, 120, 0, 48, 72)),
                Outcomes.run(5, 3));
    }

    @Test
    void kSixtyFourWithThreeErrorsMatchesEveryLiteralRowAtTheUpperBoundary() {
        // Literal rows: (64,3) -> (errors, patterns, corrected, detected, wrong).
        assertEquals(List.of(
                new Outcomes.Row(1, 72, 72, 0, 0),
                new Outcomes.Row(2, 2556, 0, 2556, 0),
                new Outcomes.Row(3, 59640, 0, 14336, 45304)),
                Outcomes.run(64, 3));
    }
}
