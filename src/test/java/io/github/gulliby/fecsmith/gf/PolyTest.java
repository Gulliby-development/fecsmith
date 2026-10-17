package io.github.gulliby.fecsmith.gf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PolyTest {
    @Test
    void trimReturnsCanonicalAscendingDegreeArrays() {
        assertArrayEquals(new int[] {}, Poly.trim(new int[] {}));
        assertArrayEquals(new int[] {}, Poly.trim(new int[] {0}));
        assertArrayEquals(new int[] {}, Poly.trim(new int[] {0, 0, 0}));
        assertArrayEquals(new int[] {1, 2}, Poly.trim(new int[] {1, 2, 0}));
        assertArrayEquals(new int[] {0, 0, 3}, Poly.trim(new int[] {0, 0, 3, 0, 0}));
        assertArrayEquals(new int[] {7}, Poly.trim(new int[] {7}));
    }

    @Test
    void multiplicationHandlesZeroProductsAndCharacteristicTwoCancellation() {
        Field field = Field.of(3);

        assertArrayEquals(new int[] {}, Poly.mul(field, new int[] {}, new int[] {1, 2}));
        assertArrayEquals(new int[] {}, Poly.mul(field, new int[] {0, 0}, new int[] {1}));
        assertArrayEquals(new int[] {}, Poly.mul(field, new int[] {1, 2}, new int[] {0}));
        assertArrayEquals(new int[] {1, 0, 1},
                Poly.mul(field, new int[] {1, 1}, new int[] {1, 1}));
        assertArrayEquals(new int[] {1, 0, 4},
                Poly.mul(field, new int[] {1, 2}, new int[] {1, 2}));
        assertArrayEquals(new int[] {1, 0, 1},
                Poly.mul(Field.of(8), new int[] {1, 1}, new int[] {1, 1}));
    }

    @Test
    void multiplicationUsesEachSupportedField() {
        assertArrayEquals(new int[] {1, 0, 1},
                Poly.mul(Field.of(3), new int[] {1, 1}, new int[] {1, 1}));
        assertArrayEquals(new int[] {1, 0, 1},
                Poly.mul(Field.of(4), new int[] {1, 1}, new int[] {1, 1}));
        assertArrayEquals(new int[] {1, 0, 1},
                Poly.mul(Field.of(5), new int[] {1, 1}, new int[] {1, 1}));
        assertArrayEquals(new int[] {1, 0, 1},
                Poly.mul(Field.of(6), new int[] {1, 1}, new int[] {1, 1}));
        assertArrayEquals(new int[] {1, 0, 1},
                Poly.mul(Field.of(7), new int[] {1, 1}, new int[] {1, 1}));
        assertArrayEquals(new int[] {1, 0, 1},
                Poly.mul(Field.of(8), new int[] {1, 1}, new int[] {1, 1}));
    }

    @Test
    void moduloSupportsNonmonicAndConstantDivisorsAndCanonicalRemainders() {
        Field field = Field.of(3);

        assertArrayEquals(new int[] {}, Poly.mod(field, new int[] {1, 0, 1}, new int[] {1, 1}));
        assertArrayEquals(new int[] {3}, Poly.mod(field, new int[] {1, 2}, new int[] {2, 2}));
        assertArrayEquals(new int[] {}, Poly.mod(field, new int[] {1, 2, 3}, new int[] {2}));
        assertArrayEquals(new int[] {1, 2}, Poly.mod(field, new int[] {1, 2}, new int[] {2, 1, 3}));
        assertArrayEquals(new int[] {4}, Poly.mod(field, new int[] {1, 2, 3, 4}, new int[] {2, 2}));
        assertArrayEquals(new int[] {}, Poly.mod(field, new int[] {0, 0, 0}, new int[] {3, 2}));
    }

    @Test
    void evaluationUsesAscendingCoefficientsAndFieldArithmetic() {
        Field field = Field.of(3);

        assertEquals(0, Poly.eval(field, new int[] {}, 2));
        assertEquals(1, Poly.eval(field, new int[] {1, 2, 3}, 0));
        assertEquals(0, Poly.eval(field, new int[] {1, 2, 3}, 1));
        assertEquals(2, Poly.eval(field, new int[] {1, 2, 3}, 2));
        assertEquals(5, Poly.eval(field, new int[] {5}, 6));
        assertEquals(9, Poly.eval(Field.of(8), new int[] {1, 2, 3}, 2));
    }

    @Test
    void derivativeDropsConstantsAndEvenPowersInCharacteristicTwo() {
        assertArrayEquals(new int[] {}, Poly.derivative(new int[] {}));
        assertArrayEquals(new int[] {}, Poly.derivative(new int[] {7}));
        assertArrayEquals(new int[] {2}, Poly.derivative(new int[] {0, 2}));
        assertArrayEquals(new int[] {}, Poly.derivative(new int[] {1, 0, 0}));
        assertArrayEquals(new int[] {2, 0, 4},
                Poly.derivative(new int[] {1, 2, 3, 4, 0}));
        assertArrayEquals(new int[] {2}, Poly.derivative(new int[] {5, 2, 0, 0, 0}));
    }
}
