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

import io.spine.annotation.Experimental;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The utility class containing convenience methods for working with
 * {@link io.spine.money.Money Money}.
 */
@Experimental
public final class MoneyAmount {

    private static final int NANOS_MIN = -999_999_999;
    private static final int NANOS_MAX = 999_999_999;

    /** Prevents instantiation of this utility class. */
    private MoneyAmount() {
    }


    private static boolean isValid(int nanos) {
        return nanos >= NANOS_MIN && nanos <= NANOS_MAX;
    }

    private static boolean isSameSign(long units, int nanos) {
        if (units < 0 || nanos < 0) {
            if (units > 0 || nanos > 0) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValid(Currency currency) {
        return currency != Currency.CURRENCY_UNDEFINED &&
                currency != Currency.UNRECOGNIZED;
    }

    private static boolean isValid(Currency currency, long units, int nanos) {
        boolean result =
                isValid(currency)
                        && isValid(nanos)
                        && isSameSign(units, nanos);

        //TODO:2018-10-12:alexander.yevsyukov: Check that the currency supports minor units.
        // If not, check that nanos is zero.
        return result;
    }

    private static void checkValid(Currency currency, long units, int nanos) {
        checkArgument(isValid(currency), "A currency must be defined.");
        checkArgument(isValid(nanos),
                      "Nanos (%s) must be in range [-999,999,999, +999,999,999].");
        checkArgument(isSameSign(units, nanos),
                      "`units` and `nanos` must be of the same sign.");
        //TODO:2018-10-12:alexander.yevsyukov: Check that the currency supports minor units.
        // If not, check that nanos is zero.
    }

    /**
     * Creates a new {@code Money} instance.
     *
     * @param currency
     *         the currency of the amount of money
     * @param units
     *         the amount of whole currency units
     * @param nanos
     *         the number of (10^-9) units of the amount for representing amounts in
     *         minor currency units (for the currencies that support such amounts).
     */
    public static Money of(Currency currency, long units, int nanos) {
        checkNotNull(currency);
        checkValid(currency, units, nanos);
        Money result = Money
                .newBuilder()
                .setCurrency(currency)
                .setUnits(units)
                .setNanos(nanos)
                .build();
        return result;
    }
}
