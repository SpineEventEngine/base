/*
 * Copyright 2021, TeamDev. All rights reserved.
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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.testing.NullPointerTester.Visibility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.support.ModifierSupport;

import java.lang.reflect.Constructor;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.testing.NullPointerTester.Visibility.PUBLIC;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Abstract base for testing classes that implement a singleton pattern.
 *
 * @param <S> the type of the singleton
 */
@SuppressWarnings("UnstableApiUsage") // NPE testing utils from Guava.
public abstract class SingletonTest<S> extends ClassTest<S> {

    private final Supplier<S> accessor;

    /**
     * Creates new test suite.
     *
     * @param subject
     *          the class under the tests
     * @param minimalStaticMethodVisibility
     *          the minimal level of visibility of static methods for testing null parameters
     * @param accessor
     *          method reference to obtains the singleton
     */
    protected SingletonTest(Class<S> subject,
                            Visibility minimalStaticMethodVisibility,
                            Supplier<S> accessor) {
        super(subject, minimalStaticMethodVisibility);
        this.accessor = checkNotNull(accessor);
    }

    /**
     * Creates new test suite.
     *
     * @param subject
     *          the class under the tests
     * @param accessor
     *          method reference to obtains the singleton
     */
    protected SingletonTest(Class<S> subject, Supplier<S> accessor) {
        this(subject, PUBLIC, accessor);
    }

    @Test
    @DisplayName("return the same instance")
    void sameInstance() {
        assertSame(accessor.get(), accessor.get());
    }

    @Nested
    @DisplayName("prevent direct instantiation")
    class CheckConstructors {

        private final ImmutableList<Constructor<?>> constructors = ImmutableList.copyOf(
                subject().getDeclaredConstructors()
        );

        @Test
        @DisplayName("prohibiting non-private constructors")
        void prohibitNonPrivate() {
            var nonPrivateConstructors = constructors(ModifierSupport::isNotPrivate);

            assertThat(nonPrivateConstructors).isEmpty();
        }

        @Test
        @DisplayName("requiring at least one private constructor")
        void requirePrivate() {
            var privateConstructors = constructors(ModifierSupport::isPrivate);

            assertThat(privateConstructors).isNotEmpty();
        }

        private ImmutableList<Constructor<?>> constructors(Predicate<Constructor<?>> filter) {
            return constructors.stream()
                    .filter(filter)
                    .collect(toImmutableList());
        }
    }

    /**
     * Shortcut method to call method of {@link CheckConstructors} from a test suite which
     * tests this class.
     */
    @VisibleForTesting
    void ctorCheck() {
        var check = new CheckConstructors();
        check.prohibitNonPrivate();
        check.requirePrivate();
    }
}
