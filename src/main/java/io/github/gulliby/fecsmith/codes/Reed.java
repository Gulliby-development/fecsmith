package io.github.gulliby.fecsmith.codes;

import io.github.gulliby.fecsmith.gf.Field;
import io.github.gulliby.fecsmith.gf.Poly;
import java.util.Arrays;
import java.util.SortedMap;
import java.util.TreeMap;

/** Systematic Reed-Solomon encoding and bounded-distance decoding. */
public final class Reed {
    private Reed() {
    }

    public record Code(Field field, int nsym) {
        public int t() {
            return nsym / 2;
        }
    }

    public record Decoded(int[] syndromes, boolean ok, SortedMap<Integer, Integer> errors) {
    }

    public static int[] parity(Code c, int[] message) {
        int nsym = c.nsym();
        int[] shiftedMessage = new int[message.length + nsym];
        for (int i = 0; i < message.length; i++) {
            shiftedMessage[nsym + i] = message[message.length - 1 - i];
        }

        int[] remainder = Poly.mod(c.field(), shiftedMessage, generator(c));
        int[] parity = new int[nsym];
        for (int i = 0; i < nsym; i++) {
            int degree = nsym - 1 - i;
            if (degree < remainder.length) {
                parity[i] = remainder[degree];
            }
        }
        return parity;
    }

    public static int[] syndromes(Code c, int[] word) {
        Field field = c.field();
        int[] syndromes = new int[c.nsym()];
        for (int j = 1; j <= c.nsym(); j++) {
            int root = field.exp(j);
            int value = 0;
            for (int symbol : word) {
                value = field.mul(value, root) ^ symbol;
            }
            syndromes[j - 1] = value;
        }
        return syndromes;
    }

    public static Decoded decode(Code c, int[] word) {
        int[] originalSyndromes = syndromes(c, word);
        if (allZero(originalSyndromes)) {
            return new Decoded(originalSyndromes, true, new TreeMap<>());
        }

        Field field = c.field();
        int[] locator = berlekampMassey(field, originalSyndromes);
        int degree = locator.length - 1;
        if (degree == 0 || degree > c.t()) {
            return failure(originalSyndromes);
        }

        int[] positions = new int[degree];
        int found = 0;
        for (int position = 0; position < word.length; position++) {
            int power = word.length - 1 - position;
            int inverseLocation = field.exp(-power);
            if (Poly.eval(field, locator, inverseLocation) == 0) {
                if (found == degree) {
                    return failure(originalSyndromes);
                }
                positions[found++] = position;
            }
        }
        if (found != degree) {
            return failure(originalSyndromes);
        }

        int[] magnitudes = solveMagnitudes(field, originalSyndromes, word.length, positions);
        if (magnitudes == null) {
            return failure(originalSyndromes);
        }

        int[] corrected = Arrays.copyOf(word, word.length);
        SortedMap<Integer, Integer> errors = new TreeMap<>();
        for (int i = 0; i < positions.length; i++) {
            int magnitude = magnitudes[i];
            if (magnitude != 0) {
                corrected[positions[i]] ^= magnitude;
                errors.put(positions[i], magnitude);
            }
        }
        if (errors.size() > c.t() || !allZero(syndromes(c, corrected))) {
            return failure(originalSyndromes);
        }
        return new Decoded(originalSyndromes, true, errors);
    }

    private static int[] generator(Code c) {
        int[] result = {1};
        for (int i = 1; i <= c.nsym(); i++) {
            result = Poly.mul(c.field(), result, new int[] {c.field().exp(i), 1});
        }
        return result;
    }

    private static int[] berlekampMassey(Field field, int[] syndromes) {
        int[] connection = new int[syndromes.length + 1];
        int[] previous = new int[syndromes.length + 1];
        connection[0] = 1;
        previous[0] = 1;
        int degree = 0;
        int shift = 1;
        int previousDiscrepancy = 1;

        for (int n = 0; n < syndromes.length; n++) {
            int discrepancy = syndromes[n];
            for (int i = 1; i <= degree && i <= n; i++) {
                discrepancy ^= field.mul(connection[i], syndromes[n - i]);
            }
            if (discrepancy == 0) {
                shift++;
                continue;
            }

            int[] oldConnection = Arrays.copyOf(connection, connection.length);
            int scale = field.div(discrepancy, previousDiscrepancy);
            for (int i = 0; i + shift < connection.length && i < previous.length; i++) {
                connection[i + shift] ^= field.mul(scale, previous[i]);
            }

            if (2 * degree <= n) {
                degree = n + 1 - degree;
                previous = oldConnection;
                previousDiscrepancy = discrepancy;
                shift = 1;
            } else {
                shift++;
            }
        }

        return Poly.trim(Arrays.copyOf(connection, degree + 1));
    }

    private static int[] solveMagnitudes(Field field, int[] syndromes, int wordLength, int[] positions) {
        int count = positions.length;
        int[][] matrix = new int[count][count + 1];
        for (int row = 0; row < count; row++) {
            int power = row + 1;
            for (int column = 0; column < count; column++) {
                int locationPower = wordLength - 1 - positions[column];
                matrix[row][column] = field.exp(locationPower * power);
            }
            matrix[row][count] = syndromes[row];
        }

        for (int column = 0; column < count; column++) {
            int pivot = column;
            while (pivot < count && matrix[pivot][column] == 0) {
                pivot++;
            }
            if (pivot == count) {
                return null;
            }
            if (pivot != column) {
                int[] temporary = matrix[column];
                matrix[column] = matrix[pivot];
                matrix[pivot] = temporary;
            }

            int inversePivot = field.inv(matrix[column][column]);
            for (int j = column; j <= count; j++) {
                matrix[column][j] = field.mul(matrix[column][j], inversePivot);
            }
            for (int row = 0; row < count; row++) {
                if (row == column || matrix[row][column] == 0) {
                    continue;
                }
                int factor = matrix[row][column];
                for (int j = column; j <= count; j++) {
                    matrix[row][j] ^= field.mul(factor, matrix[column][j]);
                }
            }
        }

        int[] magnitudes = new int[count];
        for (int i = 0; i < count; i++) {
            magnitudes[i] = matrix[i][count];
        }
        return magnitudes;
    }

    private static boolean allZero(int[] values) {
        for (int value : values) {
            if (value != 0) {
                return false;
            }
        }
        return true;
    }

    private static Decoded failure(int[] syndromes) {
        return new Decoded(syndromes, false, new TreeMap<>());
    }
}
