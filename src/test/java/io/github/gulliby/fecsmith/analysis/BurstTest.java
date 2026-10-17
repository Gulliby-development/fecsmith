package io.github.gulliby.fecsmith.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.gulliby.fecsmith.codes.Crc;
import java.util.List;
import org.junit.jupiter.api.Test;

class BurstTest {
    private static final List<Burst.Row> CRC8_ROWS = List.of(
            new Burst.Row(1, 1L, 0L),
            new Burst.Row(2, 1L, 0L),
            new Burst.Row(3, 2L, 0L),
            new Burst.Row(4, 4L, 0L),
            new Burst.Row(5, 8L, 0L),
            new Burst.Row(6, 16L, 0L),
            new Burst.Row(7, 32L, 0L),
            new Burst.Row(8, 64L, 0L),
            new Burst.Row(9, 128L, 1L),
            new Burst.Row(10, 256L, 1L));

    private static final List<Burst.Row> CRC3_GSM_ROWS = List.of(
            new Burst.Row(1, 1L, 0L),
            new Burst.Row(2, 1L, 0L),
            new Burst.Row(3, 2L, 0L),
            new Burst.Row(4, 4L, 1L),
            new Burst.Row(5, 8L, 1L),
            new Burst.Row(6, 16L, 2L));

    private static final List<Burst.Row> CRC3_GSM_TO_18_ROWS = List.of(
            new Burst.Row(1, 1L, 0L),
            new Burst.Row(2, 1L, 0L),
            new Burst.Row(3, 2L, 0L),
            new Burst.Row(4, 4L, 1L),
            new Burst.Row(5, 8L, 1L),
            new Burst.Row(6, 16L, 2L),
            new Burst.Row(7, 32L, 4L),
            new Burst.Row(8, 64L, 8L),
            new Burst.Row(9, 128L, 16L),
            new Burst.Row(10, 256L, 32L),
            new Burst.Row(11, 512L, 64L),
            new Burst.Row(12, 1024L, 128L),
            new Burst.Row(13, 2048L, 256L),
            new Burst.Row(14, 4096L, 512L),
            new Burst.Row(15, 8192L, 1024L),
            new Burst.Row(16, 16384L, 2048L),
            new Burst.Row(17, 32768L, 4096L),
            new Burst.Row(18, 65536L, 8192L));

    private static final List<Burst.Row> CRC32_TO_18_ROWS = List.of(
            new Burst.Row(1, 1L, 0L),
            new Burst.Row(2, 1L, 0L),
            new Burst.Row(3, 2L, 0L),
            new Burst.Row(4, 4L, 0L),
            new Burst.Row(5, 8L, 0L),
            new Burst.Row(6, 16L, 0L),
            new Burst.Row(7, 32L, 0L),
            new Burst.Row(8, 64L, 0L),
            new Burst.Row(9, 128L, 0L),
            new Burst.Row(10, 256L, 0L),
            new Burst.Row(11, 512L, 0L),
            new Burst.Row(12, 1024L, 0L),
            new Burst.Row(13, 2048L, 0L),
            new Burst.Row(14, 4096L, 0L),
            new Burst.Row(15, 8192L, 0L),
            new Burst.Row(16, 16384L, 0L),
            new Burst.Row(17, 32768L, 0L),
            new Burst.Row(18, 65536L, 0L));

    // AC-005 model/maxLen: crc8/10.
    @Test
    void crc8MatchesTheCompleteLiteralListAndFirstDivisibleLengthIsWidthPlusOne() {
        Crc.Model crc8 = Crc.parse("crc8");
        List<Burst.Row> rows = Burst.run(crc8, 10);

        assertEquals(CRC8_ROWS, rows);
        assertEquals(8, crc8.width());
        assertEquals(0L, rows.get(7).undetected());
        assertEquals(9, rows.get(8).length());
        assertEquals(1L, rows.get(8).undetected());
    }

    // AC-005 model/maxLen: crc3-gsm/6.
    @Test
    void crc3GsmMatchesTheCompleteLiteralListThroughItsFirstSixLengths() {
        assertEquals(CRC3_GSM_ROWS, Burst.run(Crc.parse("crc3-gsm"), 6));
    }

    @Test
    void maximumLengthOneReturnsItsSingleLiteralRow() {
        assertEquals(List.of(new Burst.Row(1, 1L, 0L)),
                Burst.run(Crc.parse("crc8"), 1));
    }

    // AC-005 model/maxLen: crc3-gsm/18.
    @Test
    void crc3GsmMatchesEveryLiteralRowThroughMaximumSupportedLength() {
        assertEquals(CRC3_GSM_TO_18_ROWS, Burst.run(Crc.parse("crc3-gsm"), 18));
    }

    @Test
    void widthThirtyTwoHasTheCompleteLiteralZeroUndetectedList() {
        assertEquals(CRC32_TO_18_ROWS, Burst.run(Crc.parse("crc32"), 18));
    }

    @Test
    void countsDependOnlyOnWidthAndPolynomial() {
        assertEquals(CRC8_ROWS, Burst.run(
                new Crc.Model("different-name", 8, 0x07L, 0L, false, false, 0L), 10));
        assertEquals(CRC8_ROWS, Burst.run(
                new Crc.Model("crc8", 8, 0x07L, 0x01L, false, false, 0L), 10));
        assertEquals(CRC8_ROWS, Burst.run(
                new Crc.Model("crc8", 8, 0x07L, 0L, true, false, 0L), 10));
        assertEquals(CRC8_ROWS, Burst.run(
                new Crc.Model("crc8", 8, 0x07L, 0L, false, true, 0L), 10));
        assertEquals(CRC8_ROWS, Burst.run(
                new Crc.Model("crc8", 8, 0x07L, 0L, false, false, 0x01L), 10));
    }
}
