/*
 * Copyright 2020, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import com.google.common.testing.NullPointerTester.Visibility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.testing.NullPointerTester.Visibility.PUBLIC;
import static io.spine.testing.Tests.assertTrue;

/**
 * Abstract base for test suites that test a class (e.g. static methods) rather than an object.
 *
 * @param <C>
 *         the class under the tests
 */
@SuppressWarnings("UnstableApiUsage") // NPE testing utils from Guava.
public abstract class ClassTest<C> {

    private final Class<C> subject;
    private final Visibility minimalStaticMethodVisibility;

    /**
     * Creates a new test suite for the passed class.
     *
     * @param subject
     *          the class to be tested
     * @param minimalStaticMethodVisibility
     *          the minimal level of visibility of static methods for testing
     *          null parameters
     * @see #configure(NullPointerTester)
     */
    protected ClassTest(Class<C> subject, Visibility minimalStaticMethodVisibility) {
        this.subject = checkNotNull(subject);
        this.minimalStaticMethodVisibility = checkNotNull(minimalStaticMethodVisibility);
    }

    /**
     * Creates a new test suite for the passed class.
     *
     * <p>This test suite will
     * {@link com.google.common.testing.NullPointerTester.Visibility#PUBLIC PUBLIC}
     * visibility of static methods for null-pointer testing.
     *
     * @param subject
     *          the class to be tested
     */
    protected ClassTest(Class<C> subject) {
        this(subject, PUBLIC);
    }

    /**
     * Obtains the class under tests.
     */
    protected final Class<C> subject() {
        return subject;
    }

    /**
     * Obtains the minimal level of visibility of static methods included into null-pointer
     * testing of parameters.
     *
     * @see #configure(NullPointerTester)
     */
    protected final Visibility minimalStaticMethodVisibility() {
        return minimalStaticMethodVisibility;
    }

    /**
     * Test handling null parameters of the static methods of the class.
     *
     * @see #configure(NullPointerTester)
     */
    @Test
    @DisplayName("not accept nulls in static methods if a parameter is non-Nullable")
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        /* This test does assert via `NullPointerTester. */
    void nullCheckParamsOfStaticMethods() {
        NullPointerTester tester = new NullPointerTester();
        configure(tester);
        tester.testStaticMethods(subject(), minimalStaticMethodVisibility);
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

    /**
     * Asserts that the class under tests has a {@code private} constructor
     * which accepts no parameters.
     */
    @SuppressWarnings("NewMethodNamingConvention")
    protected final void assertHasPrivateParameterlessCtor() {
        Tests.assertHasPrivateParameterlessCtor(subject());
    }

    /**
     * Asserts that the class under tests is declared as {@code final}.
     */
    protected final void assertFinal() {
        assertTrue(Modifier.isFinal(subject().getModifiers()));
    }
}
