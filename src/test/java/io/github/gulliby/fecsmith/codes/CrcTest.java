package io.github.gulliby.fecsmith.codes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.gulliby.fecsmith.core.Failure;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class CrcTest {
    private static final byte[] CHECK_BYTES = {
            0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39
    };

    @Test
    void catalogHasEveryLiteralModelInTheSpecifiedOrder() {
        assertEquals(List.of(
                new Crc.Model("crc3-gsm", 3, 0x3L, 0x0L, false, false, 0x7L),
                new Crc.Model("crc4-itu", 4, 0x3L, 0x0L, true, true, 0x0L),
                new Crc.Model("crc5-usb", 5, 0x05L, 0x1fL, true, true, 0x1fL),
                new Crc.Model("crc8", 8, 0x07L, 0x00L, false, false, 0x00L),
                new Crc.Model("crc8-maxim", 8, 0x31L, 0x00L, true, true, 0x00L),
                new Crc.Model("crc16-ccitt-false", 16, 0x1021L, 0xffffL, false, false, 0x0000L),
                new Crc.Model("crc16-xmodem", 16, 0x1021L, 0x0000L, false, false, 0x0000L),
                new Crc.Model("crc16-kermit", 16, 0x1021L, 0x0000L, true, true, 0x0000L),
                new Crc.Model("crc16-arc", 16, 0x8005L, 0x0000L, true, true, 0x0000L),
                new Crc.Model("crc16-modbus", 16, 0x8005L, 0xffffL, true, true, 0x0000L),
                new Crc.Model("crc32", 32, 0x04c11db7L, 0xffffffffL, true, true, 0xffffffffL),
                new Crc.Model("crc32-bzip2", 32, 0x04c11db7L, 0xffffffffL, false, false, 0xffffffffL),
                new Crc.Model("crc32c", 32, 0x1edc6f41L, 0xffffffffL, true, true, 0xffffffffL)),
                Crc.catalog());
    }

    @Test
    void everyCatalogNameParsesToItsLiteralModel() {
        assertEquals(new Crc.Model("crc3-gsm", 3, 0x3L, 0x0L, false, false, 0x7L),
                Crc.parse("crc3-gsm"));
        assertEquals(new Crc.Model("crc4-itu", 4, 0x3L, 0x0L, true, true, 0x0L),
                Crc.parse("crc4-itu"));
        assertEquals(new Crc.Model("crc5-usb", 5, 0x05L, 0x1fL, true, true, 0x1fL),
                Crc.parse("crc5-usb"));
        assertEquals(new Crc.Model("crc8", 8, 0x07L, 0x00L, false, false, 0x00L),
                Crc.parse("crc8"));
        assertEquals(new Crc.Model("crc8-maxim", 8, 0x31L, 0x00L, true, true, 0x00L),
                Crc.parse("crc8-maxim"));
        assertEquals(new Crc.Model("crc16-ccitt-false", 16, 0x1021L, 0xffffL, false, false, 0x0000L),
                Crc.parse("crc16-ccitt-false"));
        assertEquals(new Crc.Model("crc16-xmodem", 16, 0x1021L, 0x0000L, false, false, 0x0000L),
                Crc.parse("crc16-xmodem"));
        assertEquals(new Crc.Model("crc16-kermit", 16, 0x1021L, 0x0000L, true, true, 0x0000L),
                Crc.parse("crc16-kermit"));
        assertEquals(new Crc.Model("crc16-arc", 16, 0x8005L, 0x0000L, true, true, 0x0000L),
                Crc.parse("crc16-arc"));
        assertEquals(new Crc.Model("crc16-modbus", 16, 0x8005L, 0xffffL, true, true, 0x0000L),
                Crc.parse("crc16-modbus"));
        assertEquals(new Crc.Model("crc32", 32, 0x04c11db7L, 0xffffffffL, true, true, 0xffffffffL),
                Crc.parse("crc32"));
        assertEquals(new Crc.Model("crc32-bzip2", 32, 0x04c11db7L, 0xffffffffL, false, false, 0xffffffffL),
                Crc.parse("crc32-bzip2"));
        assertEquals(new Crc.Model("crc32c", 32, 0x1edc6f41L, 0xffffffffL, true, true, 0xffffffffL),
                Crc.parse("crc32c"));
    }

    @Test
    void everyCatalogModelHasItsLiteralCheckValueFor123456789() {
        assertEquals(0x4L, Crc.compute(Crc.parse("crc3-gsm"), CHECK_BYTES));
        assertEquals(0x7L, Crc.compute(Crc.parse("crc4-itu"), CHECK_BYTES));
        assertEquals(0x19L, Crc.compute(Crc.parse("crc5-usb"), CHECK_BYTES));
        assertEquals(0xf4L, Crc.compute(Crc.parse("crc8"), CHECK_BYTES));
        assertEquals(0xa1L, Crc.compute(Crc.parse("crc8-maxim"), CHECK_BYTES));
        assertEquals(0x29b1L, Crc.compute(Crc.parse("crc16-ccitt-false"), CHECK_BYTES));
        assertEquals(0x31c3L, Crc.compute(Crc.parse("crc16-xmodem"), CHECK_BYTES));
        assertEquals(0x2189L, Crc.compute(Crc.parse("crc16-kermit"), CHECK_BYTES));
        assertEquals(0xbb3dL, Crc.compute(Crc.parse("crc16-arc"), CHECK_BYTES));
        assertEquals(0x4b37L, Crc.compute(Crc.parse("crc16-modbus"), CHECK_BYTES));
        assertEquals(0xcbf43926L, Crc.compute(Crc.parse("crc32"), CHECK_BYTES));
        assertEquals(0xfc891918L, Crc.compute(Crc.parse("crc32-bzip2"), CHECK_BYTES));
        assertEquals(0xe3069283L, Crc.compute(Crc.parse("crc32c"), CHECK_BYTES));
    }

    @Test
    void customSpecificationPreservesItsOriginalNameAndParsesItsFields() {
        assertEquals(new Crc.Model("crc:5:15:00:0:0:00", 5, 0x15L, 0L, false, false, 0L),
                Crc.parse("crc:5:15:00:0:0:00"));
        assertEquals(new Crc.Model("crc:5:15:00:0:0:00", 5, 0x15L, 0L, false, false, 0L).name(),
                Crc.parse("crc:5:15:00:0:0:00").name());
    }

    @Test
    void customSpecificationAcceptsUppercaseHexAndLeadingZeroWidth() {
        assertEquals(new Crc.Model("crc:032:04C11DB7:FFFFFFFF:1:1:FFFFFFFF",
                        32, 0x04c11db7L, 0xffffffffL, true, true, 0xffffffffL),
                Crc.parse("crc:032:04C11DB7:FFFFFFFF:1:1:FFFFFFFF"));
        assertEquals(new Crc.Model("crc:003:03:00:0:1:00", 3, 0x3L, 0L, false, true, 0L),
                Crc.parse("crc:003:03:00:0:1:00"));
    }

    // AC-003 condition: noncatalog without exact crc: prefix.
    @Test
    void unknownNamesAreCaseSensitiveAndDoNotAcceptTrailingWhitespace() {
        assertUsage("unknown model \"CRC8\" (see crc --list, or write crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)",
                () -> Crc.parse("CRC8"));
        assertUsage("unknown model \"crc8?\" (see crc --list, or write crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)",
                () -> Crc.parse("crc8 "));
        assertUsage("invalid crc spec \"crc:8\" (expected crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)",
                () -> Crc.parse("crc:8"));
    }

    // AC-003 bad shape cases verify the exact Failure.Usage diagnostics.
    @Test
    void malformedSpecificationShapesAreRejectedBeforeNumericValidation() {
        assertUsage("invalid crc spec \"crc:\" (expected crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)",
                () -> Crc.parse("crc:"));
        assertUsage("invalid crc spec \"crc:8:07:00:0:0\" (expected crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)",
                () -> Crc.parse("crc:8:07:00:0:0"));
        assertUsage("invalid crc spec \"crc:8:07:00:0:0:00:extra\" (expected crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)",
                () -> Crc.parse("crc:8:07:00:0:0:00:extra"));
        assertUsage("invalid crc spec \"crc::07:00:0:0:00\" (expected crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)",
                () -> Crc.parse("crc::07:00:0:0:00"));
        assertUsage("invalid crc spec \"crc:+8:07:00:0:0:00\" (expected crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)",
                () -> Crc.parse("crc:+8:07:00:0:0:00"));
        assertUsage("invalid crc spec \"crc:0008:07:00:0:0:00\" (expected crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)",
                () -> Crc.parse("crc:0008:07:00:0:0:00"));
        assertUsage("invalid crc spec \"crc:?:07:00:0:0:00\" (expected crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)",
                () -> Crc.parse("crc:٨:07:00:0:0:00"));
        assertUsage("invalid crc spec \"crc:8:0x07:00:0:0:00\" (expected crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)",
                () -> Crc.parse("crc:8:0x07:00:0:0:00"));
        assertUsage("invalid crc spec \"crc:8:07:00:2:0:00\" (expected crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)",
                () -> Crc.parse("crc:8:07:00:2:0:00"));
        assertUsage("invalid crc spec \"crc:8:07:00:true:0:00\" (expected crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)",
                () -> Crc.parse("crc:8:07:00:true:0:00"));
        assertUsage("invalid crc spec \"crc:8:07:00:0:0:000000000\" (expected crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)",
                () -> Crc.parse("crc:8:07:00:0:0:000000000"));
        assertUsage("invalid crc spec \"crc:8?:07:00:0:0:00\" (expected crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)",
                () -> Crc.parse("crc:8 :07:00:0:0:00"));
    }

    @Test
    void widthRangeIncludesThreeAndThirtyTwoAndRejectsTwoAndThirtyThree() {
        assertEquals(3, Crc.parse("crc:3:3:0:0:0:0").width());
        assertEquals(32, Crc.parse("crc:32:1:0:0:0:0").width());
        assertUsage("invalid crc spec \"crc:2:1:0:0:0:0\" (WIDTH must be from 3 to 32)",
                () -> Crc.parse("crc:2:1:0:0:0:0"));
        assertUsage("invalid crc spec \"crc:33:1:0:0:0:0\" (WIDTH must be from 3 to 32)",
                () -> Crc.parse("crc:33:1:0:0:0:0"));
    }

    @Test
    void polynomialMustBeOddAndStrictlyBelowTheWidthLimit() {
        assertEquals(0x7L, Crc.parse("crc:3:7:0:0:0:0").poly());
        assertUsage("invalid crc spec \"crc:3:0:0:0:0:0\" (POLY must be odd and below 2^WIDTH)",
                () -> Crc.parse("crc:3:0:0:0:0:0"));
        assertUsage("invalid crc spec \"crc:3:2:0:0:0:0\" (POLY must be odd and below 2^WIDTH)",
                () -> Crc.parse("crc:3:2:0:0:0:0"));
        assertUsage("invalid crc spec \"crc:3:8:0:0:0:0\" (POLY must be odd and below 2^WIDTH)",
                () -> Crc.parse("crc:3:8:0:0:0:0"));
    }

    @Test
    void initialAndFinalXorValuesMustBeStrictlyBelowTheWidthLimit() {
        assertUsage("invalid crc spec \"crc:3:3:8:0:0:0\" (INIT must be below 2^WIDTH)",
                () -> Crc.parse("crc:3:3:8:0:0:0"));
        assertUsage("invalid crc spec \"crc:3:3:0:0:0:8\" (XOROUT must be below 2^WIDTH)",
                () -> Crc.parse("crc:3:3:0:0:0:8"));
        assertEquals(7L, Crc.parse("crc:3:3:7:0:0:7").init());
        assertEquals(7L, Crc.parse("crc:3:3:7:0:0:7").xorOut());
    }

    @Test
    void validationPrecedenceIsShapeThenWidthThenPolyThenInitThenXorOut() {
        assertUsage("invalid crc spec \"crc:2:3:0:0:0\" (expected crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)",
                () -> Crc.parse("crc:2:3:0:0:0"));
        assertUsage("invalid crc spec \"crc:2:8:8:0:0:8\" (WIDTH must be from 3 to 32)",
                () -> Crc.parse("crc:2:8:8:0:0:8"));
        assertUsage("invalid crc spec \"crc:3:8:8:0:0:8\" (POLY must be odd and below 2^WIDTH)",
                () -> Crc.parse("crc:3:8:8:0:0:8"));
        assertUsage("invalid crc spec \"crc:3:3:8:0:0:8\" (INIT must be below 2^WIDTH)",
                () -> Crc.parse("crc:3:3:8:0:0:8"));
    }

    @Test
    void unicodeDiagnosticsUseTheSharedPrintableAsciiSanitizer() {
        assertUsage("unknown model \"crc8?x\" (see crc --list, or write crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)",
                () -> Crc.parse("crc8😀x"));
        assertUsage("invalid crc spec \"crc:3:3:0:0:0:??\" (expected crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)",
                () -> Crc.parse("crc:3:3:0:0:0:é😀"));
    }

    // AC-004 model/input cases: crc:5:15:00:0:0:00 / 313233343536373839, crc32 / empty, crc3-gsm / empty.
    @Test
    void computesWidthFiveExampleAndEmptyInputLiterals() {
        assertEquals(0x1cL,
                Crc.compute(Crc.parse("crc:5:15:00:0:0:00"), CHECK_BYTES));
        assertEquals(0L, Crc.compute(Crc.parse("crc32"), new byte[0]));
        assertEquals(0x7L, Crc.compute(Crc.parse("crc3-gsm"), new byte[0]));
    }

    // AC-004 model/input cases: crc:3:3:0:0:0:0 / 01, crc:3:3:0:1:0:0 / 01,
    // crc:3:3:0:0:1:0 / 01, crc:3:3:0:1:1:0 / 01.
    @Test
    void inputAndOutputReflectionAreIndependentForTheLiteralByte01() {
        assertEquals(0x3L, Crc.compute(Crc.parse("crc:3:3:0:0:0:0"), new byte[] {0x01}));
        assertEquals(0x3L, Crc.compute(Crc.parse("crc:3:3:0:1:0:0"), new byte[] {0x01}));
        assertEquals(0x6L, Crc.compute(Crc.parse("crc:3:3:0:0:1:0"), new byte[] {0x01}));
        assertEquals(0x6L, Crc.compute(Crc.parse("crc:3:3:0:1:1:0"), new byte[] {0x01}));
    }

    @Test
    void treatsHighBitBytesAsUnsignedWithInputReflectionEnabledOrDisabled() {
        assertEquals(0x3L, Crc.compute(Crc.parse("crc:3:3:0:0:0:0"), new byte[] {(byte) 0x80}));
        assertEquals(0x3L, Crc.compute(Crc.parse("crc:3:3:0:1:0:0"), new byte[] {(byte) 0x80}));
    }

    // Literal generator(crc3-gsm) and generator(crc32) cases verify the implicit top bit.
    @Test
    void generatorsIncludeTheImplicitTopBitAtSmallAndThirtyTwoBitWidths() {
        assertEquals(0xbL, Crc.generator(Crc.parse("crc3-gsm")));
        assertEquals(0x104c11db7L, Crc.generator(Crc.parse("crc32")));
        assertEquals(0x100000001L, Crc.generator(Crc.parse("crc:32:1:0:0:0:0")));
    }

    private static void assertUsage(String expectedMessage, Executable action) {
        Failure.Usage failure = assertThrows(Failure.Usage.class, action);
        assertEquals(expectedMessage, failure.getMessage());
    }
}
