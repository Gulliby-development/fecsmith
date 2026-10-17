package io.github.gulliby.fecsmith.gf;

import java.util.Arrays;

/** Polynomial operations over a finite field, with coefficients in ascending degree order. */
public final class Poly {
    private Poly() {
    }

    public static int[] trim(int[] p) {
        int length = p.length;
        while (length > 0 && p[length - 1] == 0) {
            length--;
        }
        return Arrays.copyOf(p, length);
    }

    public static int[] mul(Field f, int[] a, int[] b) {
        int[] left = trim(a);
        int[] right = trim(b);
        if (left.length == 0 || right.length == 0) {
            return new int[0];
        }

        int[] product = new int[left.length + right.length - 1];
        for (int i = 0; i < left.length; i++) {
            for (int j = 0; j < right.length; j++) {
                product[i + j] ^= f.mul(left[i], right[j]);
            }
        }
        return trim(product);
    }

    public static int[] mod(Field f, int[] a, int[] b) {
        int[] remainder = trim(a);
        int[] divisor = trim(b);
        if (divisor.length == 0) {
            throw new ArithmeticException("polynomial division by zero");
        }
        int inverseLeading = f.inv(divisor[divisor.length - 1]);

        while (remainder.length >= divisor.length) {
            int offset = remainder.length - divisor.length;
            int factor = f.mul(remainder[remainder.length - 1], inverseLeading);
            for (int i = 0; i < divisor.length; i++) {
                remainder[offset + i] ^= f.mul(factor, divisor[i]);
            }
            remainder = trim(remainder);
        }
        return remainder;
    }

    public static int eval(Field f, int[] p, int x) {
        int result = 0;
        for (int i = p.length - 1; i >= 0; i--) {
            result = f.mul(result, x) ^ p[i];
        }
        return result;
    }

    public static int[] derivative(int[] p) {
        if (p.length < 2) {
            return new int[0];
        }
        int[] derivative = new int[p.length - 1];
        for (int i = 1; i < p.length; i += 2) {
            derivative[i - 1] = p[i];
        }
        return trim(derivative);
    }
}
