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

package io.spine.testing;

import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.testing.Tests.assertHasPrivateParameterlessCtor;

/**
 * Abstract base for utility classes tests.
 *
 * @param <T> the type of the utility class under the test
 * @author Alexander Yevsyukov
 */
@SuppressWarnings("UnstableApiUsage")
public abstract class UtilityClassTest<T> {

    private final Class<T> utilityClass;

    protected UtilityClassTest(Class<T> aClass) {
        utilityClass = aClass;
    }

    protected Class<T> getUtilityClass() {
        return utilityClass;
    }

    @Test
    @DisplayName("have utility constructor")
    void hasUtilityConstructor() {
        assertHasPrivateParameterlessCtor(getUtilityClass());
    }

    @Test
    @DisplayName("not accept nulls in public static methods if the arg is non-Nullable")
    void nullCheckPublicStaticMethods() {
        NullPointerTester tester = new NullPointerTester();
        setDefaults(tester);
        tester.testAllPublicStaticMethods(getUtilityClass());
    }

    /**
     * A callback to set default values for a passed {@linkplain NullPointerTester}.
     *
     * <p>Does nothing. Override to specify default values in a derived test.
     */
    @SuppressWarnings("NoopMethodInAbstractClass") // We do not force overriding without a need.
    protected void setDefaults(@SuppressWarnings("unused") NullPointerTester tester) {
        // Do nothing.
    }
}
