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

import io.spine.testing.given.SingletonTestEnv.EveryTimeNew;
import io.spine.testing.given.SingletonTestEnv.NoConstructor;
import io.spine.testing.given.SingletonTestEnv.PackagePrivateConstructor;
import io.spine.testing.given.SingletonTestEnv.ProtectedConstructor;
import io.spine.testing.given.SingletonTestEnv.SingletonClass;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("`SingletonTest` should")
class SingletonTestTest {

    private SingletonTest<?> subject;

    /**
     * A test suite for correctly implemented singleton class.
     */
    private static SingletonTest<SingletonClass> positiveSuite() {
        return new SingletonTest<SingletonClass>(
                SingletonClass.class, SingletonClass::instance) {
        };
    }

    @Nested
    @DisplayName("check returning the same instance")
    class SameInstance {

        @Test
        @DisplayName("not throwing when the same")
        void correct() {
            subject = positiveSuite();

            assertPass(() -> subject.sameInstance());
        }

        @Test
        @DisplayName("throwing when not the same")
        void incorrect() {
            subject = new SingletonTest<EveryTimeNew>(EveryTimeNew.class, EveryTimeNew::instance) {
            };

            assertFails(() -> subject.sameInstance());
        }
    }

    @Nested
    @DisplayName("check preventing direct instantiation")
    class PreventingInstantiation {

        @Test
        @DisplayName("now throwing when the class has only private constructor(s)")
        void correct() {
            subject = positiveSuite();

            assertPass(() -> subject.ctorCheck());
        }

        @Test
        @DisplayName("throwing when no constructors are declared")
        void noConstructor() {
            subject = new SingletonTest<NoConstructor>(NoConstructor.class, NoConstructor::new) {};

            assertFails();
        }

        @Test
        @DisplayName("throwing when package-private constructor defined")
        void packagePrivateConstructor() {
            subject = new SingletonTest<PackagePrivateConstructor>(
                    PackagePrivateConstructor.class, PackagePrivateConstructor::instance) {
            };

            assertFails();
        }

        @Test
        @DisplayName("throwing when protected constructor defined")
        void protectedConstructor() {
            subject = new SingletonTest<ProtectedConstructor>(
                    ProtectedConstructor.class, ProtectedConstructor::instance) {
            };

            assertFails();
        }

        private void assertFails() {
            SingletonTestTest.assertFails(() -> subject.ctorCheck());
        }
    }

    private static void assertPass(Executable executable) {
        assertDoesNotThrow(executable);
    }

    private static void assertFails(Executable executable) {
        assertThrows(AssertionError.class, executable);
    }
}
