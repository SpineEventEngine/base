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
import com.google.protobuf.FieldMask;
import io.spine.testing.given.TestsTestEnv;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static io.spine.testing.Tests.assertSecondsEqual;
import static io.spine.testing.Tests.hasPrivateParameterlessCtor;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Alexander Yevsyukov
 */
@SuppressWarnings({"InnerClassMayBeStatic", "ClassCanBeStatic"})
@DisplayName("Tests utility class should")
class TestsTest extends UtilityClassTest<Tests> {

    TestsTest() {
        super(Tests.class);
    }

    @Override
    protected void setDefaults(NullPointerTester tester) {
        tester.setDefault(FieldMask.class, FieldMask.getDefaultInstance());
    }

    @Nested
    @DisplayName("Check private parameterless constructor")
    class ParameterlessCtor {

        @Test
        @DisplayName("returning false if it's public")
        void publicCtor() {
            assertFalse(hasPrivateParameterlessCtor(TestsTestEnv.ClassWithPublicCtor.class));
        }

        @Test
        @DisplayName("return false if no parameterless ctor found")
        void ctorWithArgs() {
            assertFalse(hasPrivateParameterlessCtor(TestsTestEnv.ClassWithCtorWithArgs.class));
        }

        @Test
        @DisplayName("accepting private parameterless ctor")
        void privateCtor() {
            assertTrue(hasPrivateParameterlessCtor(TestsTestEnv.ClassWithPrivateCtor.class));
        }

        @Test
        @DisplayName("ignore exceptions called thrown by the constructor")
        void ignoreExceptions() {
            assertTrue(hasPrivateParameterlessCtor(TestsTestEnv.ClassThrowingExceptionInConstructor.class));
        }
    }



    @Test
    @DisplayName("provide null reference method")
    void nullRef() {
        assertNull(Tests.nullRef());
    }

    @Nested
    @DisplayName("Assert boolean equality")
    class BooleanAssert {

        @Test
        @DisplayName("when true")
        void onTrue() {
            Tests.assertEquals(true, true);
        }

        @Test
        @DisplayName("when false")
        void onFalse() {
            Tests.assertEquals(false, false);
        }

        @Test
        @DisplayName("fail when not equal")
        void failInequality() {
            assertThrows(
                    AssertionError.class,
                    () -> Tests.assertEquals(true, false)
            );
        }
    }

    @Nested
    @DisplayName("Assert true")
    class AssertTrue {
        @Test
        @DisplayName("when true")
        void onTrue() {
            Tests.assertTrue(true);
        }

        @Test
        @DisplayName("fail when false")
        void whenFalse() {
            assertThrows(
                    AssertionError.class,
                    () -> Tests.assertTrue(false)
            );
        }
    }

    @Nested
    @DisplayName("Assert seconds range")
    class SecondsRange {

        private long recentTime;

        @BeforeEach
        void getCurrentTime() {
            recentTime = now();
        }

        private long now() {
            return Instant.now()
                          .toEpochMilli();
        }

        @Test
        @DisplayName("when values are equal")
        void equalValues() {
            assertSecondsEqual(recentTime, recentTime, 0);
        }

        @Test
        @DisplayName("when values are close")
        void closeValues() {
            // This method would be called within 10 seconds.
            Tests.assertSecondsEqual(recentTime, now(), 10);
        }

        @Test
        @DisplayName("throw if condition is not met")
        void failure() {
            assertThrows(
                    AssertionError.class,
                    () -> Tests.assertSecondsEqual(100, 200, 2)
            );
        }
    }
}
