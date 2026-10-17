package io.github.gulliby.fecsmith.gf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FieldTest {
    @Test
    void accessorsExposeEachSupportedFieldSizeAndOrder() {
        // m | order
        assertEquals(3, Field.of(3).m());
        assertEquals(7, Field.of(3).order());
        assertEquals(4, Field.of(4).m());
        assertEquals(15, Field.of(4).order());
        assertEquals(5, Field.of(5).m());
        assertEquals(31, Field.of(5).order());
        assertEquals(6, Field.of(6).m());
        assertEquals(63, Field.of(6).order());
        assertEquals(7, Field.of(7).m());
        assertEquals(127, Field.of(7).order());
        assertEquals(8, Field.of(8).m());
        assertEquals(255, Field.of(8).order());
    }

    @Test
    void multiplicationUsesEverySpecifiedPrimitivePolynomial() {
        // Field.of(m).mul(2^(m-1), 2)
        assertEquals(3, Field.of(3).mul(4, 2));
        assertEquals(3, Field.of(4).mul(8, 2));
        assertEquals(5, Field.of(5).mul(16, 2));
        assertEquals(3, Field.of(6).mul(32, 2));
        assertEquals(9, Field.of(7).mul(64, 2));
        assertEquals(29, Field.of(8).mul(128, 2));
    }

    @Test
    void multiplicationHandlesZeroIdentityAndNontrivialProducts() {
        Field field = Field.of(3);

        assertEquals(0, field.mul(0, 0));
        assertEquals(0, field.mul(0, 6));
        assertEquals(0, field.mul(5, 0));
        assertEquals(5, field.mul(1, 5));
        assertEquals(5, field.mul(5, 1));
        assertEquals(4, field.mul(3, 5));
        assertEquals(1, field.mul(3, 6));
    }

    @Test
    void divisionAndInversionUseMultiplicativeInverses() {
        Field field = Field.of(3);

        assertEquals(0, field.div(0, 3));
        assertEquals(1, field.div(3, 3));
        assertEquals(2, field.div(6, 3));
        assertEquals(6, field.inv(3));
        assertEquals(2, field.inv(5));
        assertEquals(3, field.div(6, 2));
    }

    @Test
    void exponentReducesNegativeAndPositivePowersAtPeriodBoundaries() {
        Field field = Field.of(3);

        assertEquals(5, field.exp(-1));
        assertEquals(1, field.exp(-7));
        assertEquals(1, field.exp(7));
        assertEquals(2, field.exp(8));
        assertEquals(7, field.exp(Integer.MIN_VALUE));
        assertEquals(2, field.exp(Integer.MAX_VALUE));
    }

    @Test
    void exponentWrapsAtTheOrderForEveryFieldSize() {
        // m | order | exp(order) | exp(order + 1) | exp(-order)
        assertEquals(1, Field.of(3).exp(7));
        assertEquals(2, Field.of(3).exp(8));
        assertEquals(1, Field.of(3).exp(-7));

        assertEquals(1, Field.of(4).exp(15));
        assertEquals(2, Field.of(4).exp(16));
        assertEquals(1, Field.of(4).exp(-15));

        assertEquals(1, Field.of(5).exp(31));
        assertEquals(2, Field.of(5).exp(32));
        assertEquals(1, Field.of(5).exp(-31));

        assertEquals(1, Field.of(6).exp(63));
        assertEquals(2, Field.of(6).exp(64));
        assertEquals(1, Field.of(6).exp(-63));

        assertEquals(1, Field.of(7).exp(127));
        assertEquals(2, Field.of(7).exp(128));
        assertEquals(1, Field.of(7).exp(-127));

        assertEquals(1, Field.of(8).exp(255));
        assertEquals(2, Field.of(8).exp(256));
        assertEquals(1, Field.of(8).exp(-255));
    }
}
