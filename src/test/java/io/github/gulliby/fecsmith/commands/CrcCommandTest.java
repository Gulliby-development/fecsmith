package io.github.gulliby.fecsmith.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.gulliby.fecsmith.Cli;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

class CrcCommandTest {
    private static final String ZERO_WORD = """
            0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000
            0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000
            0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000
            0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000
            0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000
            0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000
            0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000
            0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000
            0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000
            0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000
            0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000
            0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000
            0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000
            0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000
            0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000
            0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000
            0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000
            0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000
            0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000
            0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000
            0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000
            0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000
            0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000
            0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000
            0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000
            0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000
            0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000
            0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000
            0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000
            0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000
            0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000
            0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 0000000000000000
            """.replace(" ", "").replace("\n", "").replace("\r", "");

    private static final byte[] NO_STDIN = {};

    @Test
    void listPrintsTheExactOrderedCatalogAndCheckValues() {
        assertRun(List.of("crc", "--list"), NO_STDIN, null, 0,
                "name               width  poly      init      refin  refout  xorout    check\n"
                        + "crc3-gsm           3      3         0         no     no      7         4\n"
                        + "crc4-itu           4      3         0         yes    yes     0         7\n"
                        + "crc5-usb           5      05        1f        yes    yes     1f        19\n"
                        + "crc8               8      07        00        no     no      00        f4\n"
                        + "crc8-maxim         8      31        00        yes    yes     00        a1\n"
                        + "crc16-ccitt-false  16     1021      ffff      no     no      0000      29b1\n"
                        + "crc16-xmodem       16     1021      0000      no     no      0000      31c3\n"
                        + "crc16-kermit       16     1021      0000      yes    yes     0000      2189\n"
                        + "crc16-arc          16     8005      0000      yes    yes     0000      bb3d\n"
                        + "crc16-modbus       16     8005      ffff      yes    yes     0000      4b37\n"
                        + "crc32              32     04c11db7  ffffffff  yes    yes     ffffffff  cbf43926\n"
                        + "crc32-bzip2        32     04c11db7  ffffffff  no     no      ffffffff  fc891918\n"
                        + "crc32c             32     1edc6f41  ffffffff  yes    yes     ffffffff  e3069283\n",
                "", 0);
    }

    @Test
    void everyCatalogModelPrintsItsLiteralCheckValue() {
        assertSuccess(List.of("crc", "crc3-gsm", "313233343536373839"),
                "crc3-gsm over 9 bytes: 4\n");
        assertSuccess(List.of("crc", "crc4-itu", "313233343536373839"),
                "crc4-itu over 9 bytes: 7\n");
        assertSuccess(List.of("crc", "crc5-usb", "313233343536373839"),
                "crc5-usb over 9 bytes: 19\n");
        assertSuccess(List.of("crc", "crc8", "313233343536373839"),
                "crc8 over 9 bytes: f4\n");
        assertSuccess(List.of("crc", "crc8-maxim", "313233343536373839"),
                "crc8-maxim over 9 bytes: a1\n");
        assertSuccess(List.of("crc", "crc16-ccitt-false", "313233343536373839"),
                "crc16-ccitt-false over 9 bytes: 29b1\n");
        assertSuccess(List.of("crc", "crc16-xmodem", "313233343536373839"),
                "crc16-xmodem over 9 bytes: 31c3\n");
        assertSuccess(List.of("crc", "crc16-kermit", "313233343536373839"),
                "crc16-kermit over 9 bytes: 2189\n");
        assertSuccess(List.of("crc", "crc16-arc", "313233343536373839"),
                "crc16-arc over 9 bytes: bb3d\n");
        assertSuccess(List.of("crc", "crc16-modbus", "313233343536373839"),
                "crc16-modbus over 9 bytes: 4b37\n");
        assertSuccess(List.of("crc", "crc32", "313233343536373839"),
                "crc32 over 9 bytes: cbf43926\n");
        assertSuccess(List.of("crc", "crc32-bzip2", "313233343536373839"),
                "crc32-bzip2 over 9 bytes: fc891918\n");
        assertSuccess(List.of("crc", "crc32c", "313233343536373839"),
                "crc32c over 9 bytes: e3069283\n");
    }

    @Test
    void formatsWorkedCustomModelAndOneByteResultWithOriginalSpelling() {
        assertSuccess(List.of("crc", "crc32", "313233343536373839"),
                "crc32 over 9 bytes: cbf43926\n");
        assertSuccess(List.of("crc", "crc:5:15:00:0:0:00", "313233343536373839"),
                "crc:5:15:00:0:0:00 over 9 bytes: 1c\n");
        assertSuccess(List.of("crc", "crc:032:04C11DB7:FFFFFFFF:1:1:FFFFFFFF",
                        "313233343536373839"),
                "crc:032:04C11DB7:FFFFFFFF:1:1:FFFFFFFF over 9 bytes: cbf43926\n");
        assertSuccess(List.of("crc", "crc:003:03:00:0:1:00", "00"),
                "crc:003:03:00:0:1:00 over 1 byte: 0\n");
    }

    @Test
    void byteLimitAccepts4096BytesAndRejectsTheNextByte() {
        assertEquals(8192, ZERO_WORD.length());
        assertSuccess(List.of("crc", "crc:3:3:0:0:0:0", ZERO_WORD),
                "crc:3:3:0:0:0:0 over 4096 bytes: 0\n");
        assertUsage(List.of("crc", "crc:3:3:0:0:0:0", ZERO_WORD, "00"),
                "too many bytes (at most 4096)", NO_STDIN, 0);
    }

    @Test
    void listMustBeAloneAndOptionSyntaxIsCheckedFirst() {
        assertUsage(List.of("crc", "--list", "crc32"), "too many arguments", NO_STDIN, 0);
        assertUsage(List.of("crc", "--list=x"), "--list takes no value", NO_STDIN, 0);
        assertUsage(List.of("crc", "--bad", "--list=x"), "unknown option \"--bad\"", NO_STDIN, 0);
        assertUsage(List.of("crc", "crc32", "00", "--list"), "too many arguments", NO_STDIN, 0);
        assertUsage(List.of("crc", "--help"), "unknown option \"--help\"", NO_STDIN, 0);
    }

    @Test
    void missingOperandsPrecedeModelAndHexValidation() {
        assertUsage(List.of("crc"), "missing MODEL", NO_STDIN, 0);
        assertUsage(List.of("crc", "unknown"), "missing HEX", NO_STDIN, 0);
        assertUsage(List.of("crc", "crc32", "313"), 
                "invalid hex word \"313\" (expected pairs of hex digits)", NO_STDIN, 0);
    }

    @Test
    void modelValidationPrecedesInputReadingAndHexValidation() {
        assertUsage(List.of("crc", "unknown", "313"),
                "unknown model \"unknown\" (see crc --list, or write crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)",
                NO_STDIN, 0);
        assertUsage(List.of("crc", "crc:bad", "-"),
                "invalid crc spec \"crc:bad\" (expected crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)",
                new byte[] {(byte) 0xff}, 0);
        assertUsage(List.of("crc", "crc32", "zz", "0g"),
                "invalid hex word \"zz\" (expected pairs of hex digits)", NO_STDIN, 0);
    }

    @Test
    void customSpecShapeErrorsUseTheExactDiagnostic() {
        assertUsage(List.of("crc", "crc:8:07:00:0:0", "00"),
                "invalid crc spec \"crc:8:07:00:0:0\" (expected crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)",
                NO_STDIN, 0);
        assertUsage(List.of("crc", "crc:8:0x07:00:0:0:00", "00"),
                "invalid crc spec \"crc:8:0x07:00:0:0:00\" (expected crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)",
                NO_STDIN, 0);
        assertUsage(List.of("crc", "crc:8:07:00:2:0:00", "00"),
                "invalid crc spec \"crc:8:07:00:2:0:00\" (expected crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)",
                NO_STDIN, 0);
        assertUsage(List.of("crc", "crc:8:07:00:0:0:000000000", "00"),
                "invalid crc spec \"crc:8:07:00:0:0:000000000\" "
                        + "(expected crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)",
                NO_STDIN, 0);
        assertUsage(List.of("crc", "CRC8", "00"),
                "unknown model \"CRC8\" (see crc --list, or write crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)",
                NO_STDIN, 0);
        assertUsage(List.of("crc", "crc😀x", "00"),
                "unknown model \"crc?x\" (see crc --list, or write crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)",
                NO_STDIN, 0);
    }

    @Test
    void customSpecRangesAreCheckedInWidthPolyInitXorOrder() {
        assertUsage(List.of("crc", "crc:2:8:8:0:0:8", "00"),
                "invalid crc spec \"crc:2:8:8:0:0:8\" (WIDTH must be from 3 to 32)", NO_STDIN, 0);
        assertUsage(List.of("crc", "crc:33:1:0:0:0:0", "00"),
                "invalid crc spec \"crc:33:1:0:0:0:0\" (WIDTH must be from 3 to 32)", NO_STDIN, 0);
        assertUsage(List.of("crc", "crc:3:8:8:0:0:8", "00"),
                "invalid crc spec \"crc:3:8:8:0:0:8\" (POLY must be odd and below 2^WIDTH)",
                NO_STDIN, 0);
        assertUsage(List.of("crc", "crc:3:2:0:0:0:0", "00"),
                "invalid crc spec \"crc:3:2:0:0:0:0\" (POLY must be odd and below 2^WIDTH)",
                NO_STDIN, 0);
        assertUsage(List.of("crc", "crc:3:3:8:0:0:8", "00"),
                "invalid crc spec \"crc:3:3:8:0:0:8\" (INIT must be below 2^WIDTH)",
                NO_STDIN, 0);
        assertUsage(List.of("crc", "crc:3:3:0:0:0:8", "00"),
                "invalid crc spec \"crc:3:3:0:0:0:8\" (XOROUT must be below 2^WIDTH)",
                NO_STDIN, 0);
    }

    @Test
    void hexWordsAcceptUppercaseAndRejectMalformedWordsLeftToRight() {
        assertSuccess(List.of("crc", "crc8", "0A"), "crc8 over 1 byte: 36\n");
        assertSuccess(List.of("crc", "crc8", "0a1B"), "crc8 over 2 bytes: c3\n");
        assertUsage(List.of("crc", "crc8", "0G", "313"),
                "invalid hex word \"0G\" (expected pairs of hex digits)", NO_STDIN, 0);
        assertUsage(List.of("crc", "crc8", "313"),
                "invalid hex word \"313\" (expected pairs of hex digits)", NO_STDIN, 0);
        assertUsage(List.of("crc", "crc8", "0x"),
                "invalid hex word \"0x\" (expected pairs of hex digits)", NO_STDIN, 0);
    }

    @Test
    void stdinWordsAcceptOneBomAndTheSpecifiedWhitespace() {
        assertRun(List.of("crc", "crc32", "-"),
                new byte[] {
                        (byte) 0xef, (byte) 0xbb, (byte) 0xbf,
                        0x33, 0x31, 0x20, 0x33, 0x32, 0x0d, 0x0a, 0x33, 0x33, 0x0a
                },
                null, 0, "crc32 over 3 bytes: 884863d2\n", "", 1);
        assertRun(List.of("crc", "crc8", "-"),
                new byte[] {0x20, 0x30, 0x41, 0x09, 0x0d, 0x0a},
                null, 0, "crc8 over 1 byte: 36\n", "", 1);
    }

    @Test
    void strictStdinErrorsAreReportedOnceWithoutPartialOutput() {
        assertUsage(List.of("crc", "crc32", "-"),
                "<stdin>: input is not valid UTF-8",
                new byte[] {0x31, 0x32, (byte) 0xff, 0x0a}, 1);
        assertUsage(List.of("crc", "crc32", "-"),
                "<stdin>: no input", new byte[] {}, 1);
        assertUsage(List.of("crc", "crc32", "-"),
                "<stdin>: no input", new byte[] {(byte) 0xef, (byte) 0xbb, (byte) 0xbf}, 1);
        assertUsage(List.of("crc", "crc32", "-"),
                "<stdin>: no input", new byte[] {0x20, 0x09, 0x0d, 0x0a}, 1);
        assertUsage(List.of("crc", "crc32", "-"),
                "invalid hex word \"1?2\" (expected pairs of hex digits)",
                new byte[] {0x31, 0x00, 0x32}, 1);
    }

    @Test
    void stdinReadFailureHasItsDistinctDiagnostic() {
        assertRun(List.of("crc", "crc32", "-"), NO_STDIN, new IOException("unavailable"), 2, "",
                "fecsmith: <stdin>: cannot read the input\n", 1);
    }

    @Test
    void stdinIsReadOnlyForTheSoleDashWordAfterValidModelChecks() {
        assertUsage(List.of("crc", "crc32"), "missing HEX", new byte[] {(byte) 0xff}, 0);
        assertUsage(List.of("crc", "not-a-model", "-"),
                "unknown model \"not-a-model\" (see crc --list, or write crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)",
                new byte[] {(byte) 0xff}, 0);
        assertUsage(List.of("crc", "--list", "ignored"),
                "too many arguments", new byte[] {(byte) 0xff}, 0);
        assertUsage(List.of("crc", "crc32", "-", "00"),
                "invalid hex word \"-\" (expected pairs of hex digits)",
                new byte[] {(byte) 0xff}, 0);
        assertUsage(List.of("crc", "crc32", "00", "-"),
                "invalid hex word \"-\" (expected pairs of hex digits)",
                new byte[] {(byte) 0xff}, 0);
        assertUsage(List.of("crc", "crc32", "--bad"),
                "unknown option \"--bad\"", new byte[] {(byte) 0xff}, 0);
    }

    private static void assertSuccess(List<String> argv, String expectedOutput) {
        assertRun(argv, NO_STDIN, null, 0, expectedOutput, "", 0);
    }

    private static void assertUsage(List<String> argv, String message, byte[] input, int expectedReads) {
        assertRun(argv, input, null, 2, "", "fecsmith: " + message + "\n", expectedReads);
    }

    private static void assertRun(List<String> argv, byte[] input, IOException readFailure,
            int expectedStatus, String expectedOutput, String expectedError, int expectedReads) {
        FakeIo io = new FakeIo(input, readFailure);

        int status = Cli.run(argv, io);

        assertEquals(expectedStatus, status);
        assertEquals(expectedOutput.isEmpty() ? List.of() : List.of(expectedOutput), io.outputs);
        assertEquals(expectedError.isEmpty() ? List.of() : List.of(expectedError), io.errors);
        assertEquals(expectedReads, io.reads);
    }

    private static final class FakeIo implements io.github.gulliby.fecsmith.core.Io {
        private final byte[] input;
        private final IOException readFailure;
        private final java.util.ArrayList<String> outputs = new java.util.ArrayList<>();
        private final java.util.ArrayList<String> errors = new java.util.ArrayList<>();
        private int reads;

        private FakeIo(byte[] input, IOException readFailure) {
            this.input = input;
            this.readFailure = readFailure;
        }

        @Override
        public byte[] readStdin() throws IOException {
            reads++;
            if (readFailure != null) {
                throw readFailure;
            }
            return input;
        }

        @Override
        public void out(String text) {
            outputs.add(text);
        }

        @Override
        public void err(String text) {
            errors.add(text);
        }
    }
}
