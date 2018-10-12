/*
 * Copyright 2018, TeamDev. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.money;

import com.google.common.testing.NullPointerTester;
import io.spine.testing.UtilityClassTest;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("MoneyAmount utility class should")
class MoneyAmountTest extends UtilityClassTest<MoneyAmount> {

    MoneyAmountTest() {
        super(MoneyAmount.class);
    }

    @Override
    protected void configure(NullPointerTester tester) {
        tester.setDefault(Currency.class, Currency.EUR);
    }

    @Test
    @DisplayName("create positive amounts")
    void createPositiveValue() {
        long units = 639_100_000_000L;

        Money money = MoneyAmount.of(Currency.USD, units, 0);

        assertEquals(Currency.USD, money.getCurrency());
        assertEquals(units, money.getUnits());
        assertEquals(units, money.getNanos());
    }

    @Test
    @DisplayName("create negative value")
    void createNegativeValue() {
        long units = -1;
        int nanos = 750_000_000;

        Money money = MoneyAmount.of(Currency.EUR, units, nanos);

        assertEquals(Currency.EUR, money.getCurrency());
        assertEquals(units, money.getUnits());
        assertEquals(units, money.getNanos());
    }

    @Test
    @DisplayName("reject values of different signs")
    void differentSigns() {
        assertThrows(
                IllegalArgumentException.class,
                () -> MoneyAmount.of(Currency.EUR, -1, 999_999)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> MoneyAmount.of(Currency.EUR, 10, -999_999)
        );
    }
}
