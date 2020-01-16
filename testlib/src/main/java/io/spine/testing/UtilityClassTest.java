/*
 * Copyright 2020, TeamDev. All rights reserved.
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

import java.lang.reflect.Modifier;

import static io.spine.testing.Tests.assertHasPrivateParameterlessCtor;
import static io.spine.testing.Tests.assertTrue;

/**
 * Abstract base for utility classes tests.
 *
 * @param <T> the type of the utility class under the test
 */
public abstract class UtilityClassTest<T> {

    private final Class<T> utilityClass;

    protected UtilityClassTest(Class<T> cls) {
        this.utilityClass = cls;
    }

    /**
     * Obtains the class under the test.
     */
    protected final Class<T> getUtilityClass() {
        return utilityClass;
    }

    @Test
    @DisplayName("have utility constructor")
    void hasUtilityConstructor() {
        assertHasPrivateParameterlessCtor(getUtilityClass());
    }

    @Test
    @DisplayName("not accept nulls in public static methods if the arg is non-Nullable")
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        /* This test does assert via `NullPointerTester. */
    void nullCheckPublicStaticMethods() {
        NullPointerTester tester = new NullPointerTester();
        configure(tester);
        tester.testAllPublicStaticMethods(getUtilityClass());
    }

    @Test
    @DisplayName("be final")
    void checkFinal() {
        assertTrue(Modifier.isFinal(utilityClass.getModifiers()));
    }

    /**
     * A callback to configure a passed {@linkplain NullPointerTester}.
     *
     * <p>Does nothing. Override to specify default values in a derived test.
     */
    @SuppressWarnings("NoopMethodInAbstractClass") // We do not force overriding without a need.
    protected void configure(@SuppressWarnings("unused") NullPointerTester tester) {
        // Do nothing.
    }
}
