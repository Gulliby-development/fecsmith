package io.github.gulliby.fecsmith.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class OptionsTest {
    @Test
    void specFactoriesExposeTheRequestedValues() {
        Options.Spec flag = Options.Spec.flag("--list");
        assertEquals("--list", flag.name());
        assertEquals(true, flag.flag());
        assertEquals(0, flag.lo());
        assertEquals(0, flag.hi());
        assertEquals(0, flag.fallback());

        Options.Spec integer = Options.Spec.integer("--count", 2, 8, 4);
        assertEquals("--count", integer.name());
        assertEquals(false, integer.flag());
        assertEquals(2, integer.lo());
        assertEquals(8, integer.hi());
        assertEquals(4, integer.fallback());
    }

    @Test
    void parseReturnsEveryDefaultAndKeepsTheGivenPositionalOrder() {
        List<Options.Spec> specs = List.of(
                Options.Spec.flag("--list"),
                Options.Spec.integer("--count", 1, 9, 3),
                Options.Spec.integer("--size", 0, 99, 12));

        Options.Parsed parsed = Options.parse(List.of("first", "-", "-5", "last"), specs);

        assertEquals(Map.of("--list", 0, "--count", 3, "--size", 12), parsed.values());
        assertEquals(List.of("--list", "--count", "--size"), List.copyOf(parsed.values().keySet()));
        assertEquals(List.of("first", "-", "-5", "last"), parsed.args());
    }

    @Test
    void parseAcceptsFlagsAndIgnoresRepeatedFlagOccurrences() {
        List<Options.Spec> specs = List.of(Options.Spec.flag("--list"));

        assertEquals(Map.of("--list", 1),
                Options.parse(List.of("--list"), specs).values());
        assertEquals(Map.of("--list", 1),
                Options.parse(List.of("--list", "--list"), specs).values());
    }

    @Test
    void parseAcceptsSeparateAndAssignedIntegerValues() {
        List<Options.Spec> specs = List.of(
                Options.Spec.integer("--count", 1, 20, 7),
                Options.Spec.integer("--size", 0, 99, 0));

        Options.Parsed parsed = Options.parse(
                List.of("--count", "5", "--size=0"), specs);

        assertEquals(Map.of("--count", 5, "--size", 0), parsed.values());
        assertEquals(List.of(), parsed.args());
    }

    @Test
    void assignmentSplitsOnlyAtTheFirstEqualsSign() {
        assertUsage("invalid --count \"12=34\" (expected an integer from 0 to 99)", () -> {
            Options.parse(List.of("--count=12=34"),
                    List.of(Options.Spec.integer("--count", 0, 99, 0)));
        });
    }

    @Test
    void optionsMayBeInterspersedWithPositionalsAndLeaveAllNonDoubleDashArgsAlone() {
        List<Options.Spec> specs = List.of(Options.Spec.integer("--count", 1, 9, 1));

        Options.Parsed parsed = Options.parse(
                List.of("before", "--count", "4", "-", "-5", "after"), specs);

        assertEquals(Map.of("--count", 4), parsed.values());
        assertEquals(List.of("before", "-", "-5", "after"), parsed.args());
    }

    @Test
    void lastRepeatedIntegerValueWinsEvenWhenEarlierTextWasInvalid() {
        List<Options.Spec> specs = List.of(Options.Spec.integer("--count", 1, 10, 3));

        Options.Parsed parsed = Options.parse(
                List.of("--count", "not-a-number", "--count=8"), specs);

        assertEquals(Map.of("--count", 8), parsed.values());
    }

    @Test
    void aDashPrefixedNextArgumentIsConsumedAsTheIntegerValue() {
        assertUsage("invalid --count \"-5\" (expected an integer from 0 to 10)", () -> {
            Options.parse(List.of("--count", "-5"),
                    List.of(Options.Spec.integer("--count", 0, 10, 0)));
        });
    }

    @Test
    void doubleDashAndHelpAreUnknownOptionsWhenNotSpecified() {
        assertUsage("unknown option \"--\"", () -> Options.parse(List.of("--"), List.of()));
        assertUsage("unknown option \"--help\"", () -> Options.parse(List.of("--help"), List.of()));
    }

    @Test
    void unknownOptionQuotesTheWholeArgumentAfterTextSanitization() {
        assertUsage("unknown option \"--bad?\"", () -> Options.parse(List.of("--bad😀"), List.of()));
        assertUsage("unknown option \"--bad=x?\"", () -> Options.parse(List.of("--bad=xé"), List.of()));
    }

    @Test
    void flagsRejectAssignmentsWithTheExactNameBasedMessage() {
        assertUsage("--list takes no value", () -> Options.parse(
                List.of("--list=true"), List.of(Options.Spec.flag("--list"))));
        assertUsage("--list takes no value", () -> Options.parse(
                List.of("--list="), List.of(Options.Spec.flag("--list"))));
    }

    @Test
    void integerOptionsDistinguishMissingAndExplicitlyEmptyValues() {
        assertUsage("--count needs a value", () -> Options.parse(
                List.of("--count"), List.of(Options.Spec.integer("--count", 0, 9, 0))));
        assertUsage("invalid --count \"\" (expected an integer from 0 to 9)", () -> Options.parse(
                List.of("--count="), List.of(Options.Spec.integer("--count", 0, 9, 0))));
    }

    @Test
    void firstSyntaxErrorWinsWhileScanningLeftToRight() {
        assertUsage("unknown option \"--bad\"", () -> Options.parse(
                List.of("--bad", "--count"), List.of(Options.Spec.integer("--count", 0, 9, 0))));
        assertUsage("--list takes no value", () -> Options.parse(
                List.of("--list=yes", "--bad"), List.of(Options.Spec.flag("--list"))));
    }

    @Test
    void syntaxErrorsAreReportedBeforePreviouslyCollectedInvalidIntegers() {
        assertUsage("unknown option \"--bad\"", () -> Options.parse(
                List.of("--count", "not-an-integer", "--bad"),
                List.of(Options.Spec.integer("--count", 0, 9, 0))));
    }

    @Test
    void retainedIntegerErrorsAreValidatedInSpecOrderNotArgumentOrder() {
        assertUsage("invalid --first \"bad-first\" (expected an integer from 0 to 9)", () -> {
            Options.parse(List.of("--second", "bad-second", "--first", "bad-first"),
                    List.of(Options.Spec.integer("--first", 0, 9, 0),
                            Options.Spec.integer("--second", 0, 9, 0)));
        });
    }

    @Test
    void integerAcceptsZeroAndInclusiveLowerAndUpperBounds() {
        assertEquals(0, Options.integer("N", "0", 0, 999999));
        assertEquals(3, Options.integer("N", "3", 3, 999999));
        assertEquals(999999, Options.integer("N", "999999", 3, 999999));
    }

    @Test
    void integerRejectsValuesOutsideTheInclusiveRange() {
        assertUsage("invalid N \"2\" (expected an integer from 3 to 999999)",
                () -> Options.integer("N", "2", 3, 999999));
        assertUsage("invalid N \"10\" (expected an integer from 0 to 9)",
                () -> Options.integer("N", "10", 0, 9));
    }

    @Test
    void integerRejectsSignsLeadingZeroesAndEmptyOrWhitespaceText() {
        assertUsage("invalid N \"-1\" (expected an integer from 0 to 999999)",
                () -> Options.integer("N", "-1", 0, 999999));
        assertUsage("invalid N \"+3\" (expected an integer from 0 to 999999)",
                () -> Options.integer("N", "+3", 0, 999999));
        assertUsage("invalid N \"007\" (expected an integer from 0 to 999999)",
                () -> Options.integer("N", "007", 0, 999999));
        assertUsage("invalid N \"\" (expected an integer from 0 to 999999)",
                () -> Options.integer("N", "", 0, 999999));
        assertUsage("invalid N \"??\" (expected an integer from 0 to 999999)",
                () -> Options.integer("N", " \t", 0, 999999));
    }

    @Test
    void integerAcceptsOnlyAsciiDigitsAndAtMostSixDigits() {
        assertEquals(999998, Options.integer("N", "999998", 0, 999999));
        assertUsage("invalid N \"1000000\" (expected an integer from 0 to 999999)",
                () -> Options.integer("N", "1000000", 0, 999999));
        assertUsage("invalid N \"?\" (expected an integer from 0 to 999999)",
                () -> Options.integer("N", "٣", 0, 999999));
        assertUsage("invalid N \"1a\" (expected an integer from 0 to 999999)",
                () -> Options.integer("N", "1a", 0, 999999));
    }

    private static void assertUsage(String expectedMessage, Executable action) {
        Failure.Usage failure = assertThrows(Failure.Usage.class, action);
        assertEquals(expectedMessage, failure.getMessage());
    }
}
