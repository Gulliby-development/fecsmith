package io.github.gulliby.fecsmith.gf;

/** Arithmetic in a binary extension field using the specified primitive polynomial. */
public final class Field {
    private static final int[] PRIMITIVE_POLYNOMIALS = {
            0, 0, 0, 0xB, 0x13, 0x25, 0x43, 0x89, 0x11D
    };

    private final int m;
    private final int order;
    private final int[] exponent;
    private final int[] logarithm;

    private Field(int m, int primitivePolynomial) {
        this.m = m;
        this.order = (1 << m) - 1;
        this.exponent = new int[2 * order];
        this.logarithm = new int[order + 1];

        int value = 1;
        for (int i = 0; i < order; i++) {
            exponent[i] = value;
            logarithm[value] = i;
            value <<= 1;
            if ((value & (1 << m)) != 0) {
                value ^= primitivePolynomial;
            }
        }
        for (int i = 0; i < order; i++) {
            exponent[i + order] = exponent[i];
        }
    }

    public static Field of(int m) {
        if (m < 3 || m > 8) {
            throw new IllegalArgumentException("m must be from 3 to 8");
        }
        return new Field(m, PRIMITIVE_POLYNOMIALS[m]);
    }

    public int m() {
        return m;
    }

    public int order() {
        return order;
    }

    public int mul(int a, int b) {
        if (a == 0 || b == 0) {
            return 0;
        }
        return exponent[logarithm[a] + logarithm[b]];
    }

    public int div(int a, int b) {
        if (b == 0) {
            throw new ArithmeticException("division by zero");
        }
        if (a == 0) {
            return 0;
        }
        return exponent[Math.floorMod(logarithm[a] - logarithm[b], order)];
    }

    public int inv(int a) {
        if (a == 0) {
            throw new ArithmeticException("zero has no inverse");
        }
        return exponent[Math.floorMod(-logarithm[a], order)];
    }

    public int exp(int i) {
        return exponent[Math.floorMod(i, order)];
    }
}
