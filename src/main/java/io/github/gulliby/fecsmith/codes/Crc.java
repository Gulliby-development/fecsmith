package io.github.gulliby.fecsmith.codes;

import io.github.gulliby.fecsmith.core.Failure;
import io.github.gulliby.fecsmith.core.Text;

import java.util.List;

/** CRC model catalog, specification parsing, and bit-serial computation. */
public final class Crc {
    private static final List<Model> CATALOG = List.of(
            new Model("crc3-gsm", 3, 0x3L, 0x0L, false, false, 0x7L),
            new Model("crc4-itu", 4, 0x3L, 0x0L, true, true, 0x0L),
            new Model("crc5-usb", 5, 0x05L, 0x1fL, true, true, 0x1fL),
            new Model("crc8", 8, 0x07L, 0x00L, false, false, 0x00L),
            new Model("crc8-maxim", 8, 0x31L, 0x00L, true, true, 0x00L),
            new Model("crc16-ccitt-false", 16, 0x1021L, 0xffffL, false, false, 0x0000L),
            new Model("crc16-xmodem", 16, 0x1021L, 0x0000L, false, false, 0x0000L),
            new Model("crc16-kermit", 16, 0x1021L, 0x0000L, true, true, 0x0000L),
            new Model("crc16-arc", 16, 0x8005L, 0x0000L, true, true, 0x0000L),
            new Model("crc16-modbus", 16, 0x8005L, 0xffffL, true, true, 0x0000L),
            new Model("crc32", 32, 0x04c11db7L, 0xffffffffL, true, true, 0xffffffffL),
            new Model("crc32-bzip2", 32, 0x04c11db7L, 0xffffffffL, false, false, 0xffffffffL),
            new Model("crc32c", 32, 0x1edc6f41L, 0xffffffffL, true, true, 0xffffffffL)
    );

    private Crc() {
    }

    /** Parameters for one CRC model. */
    public record Model(String name, int width, long poly, long init, boolean refIn, boolean refOut, long xorOut) {
    }

    public static List<Model> catalog() {
        return CATALOG;
    }

    public static Model parse(String text) {
        for (Model model : CATALOG) {
            if (model.name().equals(text)) {
                return model;
            }
        }
        if (!text.startsWith("crc:")) {
            throw new Failure.Usage("unknown model " + Text.show(text)
                    + " (see crc --list, or write crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)");
        }

        String[] fields = text.split(":", -1);
        if (fields.length != 7
                || !fields[0].equals("crc")
                || !isDecimalWidth(fields[1])
                || !isHexField(fields[2])
                || !isHexField(fields[3])
                || !isFlag(fields[4])
                || !isFlag(fields[5])
                || !isHexField(fields[6])) {
            throw invalidSpec(text, "expected crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT");
        }

        int width = Integer.parseInt(fields[1]);
        long poly = Long.parseLong(fields[2], 16);
        long init = Long.parseLong(fields[3], 16);
        boolean refIn = fields[4].equals("1");
        boolean refOut = fields[5].equals("1");
        long xorOut = Long.parseLong(fields[6], 16);

        if (width < 3 || width > 32) {
            throw invalidSpec(text, "WIDTH must be from 3 to 32");
        }

        long mask = (1L << width) - 1L;
        if ((poly & 1L) == 0L || poly > mask) {
            throw invalidSpec(text, "POLY must be odd and below 2^WIDTH");
        }
        if (init > mask) {
            throw invalidSpec(text, "INIT must be below 2^WIDTH");
        }
        if (xorOut > mask) {
            throw invalidSpec(text, "XOROUT must be below 2^WIDTH");
        }

        return new Model(text, width, poly, init, refIn, refOut, xorOut);
    }

    public static long compute(Model model, byte[] data) {
        int width = model.width();
        long mask = (1L << width) - 1L;
        long register = model.init();

        for (byte item : data) {
            int value = Byte.toUnsignedInt(item);
            if (model.refIn()) {
                value = Integer.reverse(value) >>> 24;
            }
            for (int bitIndex = 7; bitIndex >= 0; bitIndex--) {
                int inputBit = (value >>> bitIndex) & 1;
                int topBit = (int) ((register >>> (width - 1)) & 1L);
                register = (register << 1) & mask;
                if (topBit != inputBit) {
                    register ^= model.poly();
                }
            }
        }

        if (model.refOut()) {
            register = reverseBits(register, width);
        }
        return register ^ model.xorOut();
    }

    public static long generator(Model model) {
        return model.poly() | (1L << model.width());
    }

    private static boolean isDecimalWidth(String text) {
        if (text.isEmpty() || text.length() > 3) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) < '0' || text.charAt(i) > '9') {
                return false;
            }
        }
        return true;
    }

    private static boolean isHexField(String text) {
        if (text.isEmpty() || text.length() > 8) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            if (!(character >= '0' && character <= '9')
                    && !(character >= 'a' && character <= 'f')
                    && !(character >= 'A' && character <= 'F')) {
                return false;
            }
        }
        return true;
    }

    private static boolean isFlag(String text) {
        return text.equals("0") || text.equals("1");
    }

    private static Failure.Usage invalidSpec(String text, String reason) {
        return new Failure.Usage("invalid crc spec " + Text.show(text) + " (" + reason + ")");
    }

    private static long reverseBits(long value, int width) {
        long reversed = 0L;
        for (int bit = 0; bit < width; bit++) {
            reversed = (reversed << 1) | ((value >>> bit) & 1L);
        }
        return reversed;
    }
}
