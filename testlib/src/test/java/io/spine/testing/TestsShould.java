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
import com.google.protobuf.Any;
import com.google.protobuf.FieldMask;
import com.google.protobuf.Timestamp;
import io.spine.testing.given.TestsTestEnv.ClassThrowingExceptionInConstructor;
import io.spine.testing.given.TestsTestEnv.ClassWithCtorWithArgs;
import io.spine.testing.given.TestsTestEnv.ClassWithPrivateCtor;
import io.spine.testing.given.TestsTestEnv.ClassWithPublicCtor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static io.spine.testing.Tests.assertInDelta;
import static io.spine.testing.Tests.assertMatchesMask;
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
class TestsShould extends UtilityClassTest<Tests> {

    TestsShould() {
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
            assertFalse(hasPrivateParameterlessCtor(ClassWithPublicCtor.class));
        }

        @Test
        @DisplayName("return false if no parameterless ctor found")
        void ctorWithArgs() {
            assertFalse(hasPrivateParameterlessCtor(ClassWithCtorWithArgs.class));
        }

        @Test
        @DisplayName("accepting private parameterless ctor")
        void privateCtor() {
            assertTrue(hasPrivateParameterlessCtor(ClassWithPrivateCtor.class));
        }

        @Test
        @DisplayName("ignore exceptions called thrown by the constructor")
        void ignoreExceptions() {
            assertTrue(hasPrivateParameterlessCtor(ClassThrowingExceptionInConstructor.class));
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
    @DisplayName("Assert matches mask")
    class AssertMatchesMask {

        private Timestamp timestampMsg;

        @BeforeEach
        void setUp() {
            long currentTime = Instant.now()
                                      .toEpochMilli();
            timestampMsg = Timestamp.newBuilder()
                                    .setSeconds(currentTime)
                                    .build();
        }

        @Test
        @DisplayName("when field is matched")
        void fieldIsPresent() {
            String fieldPath = Timestamp.getDescriptor()
                                        .getFields()
                                        .get(0)
                                        .getFullName();
            FieldMask.Builder builder = FieldMask.newBuilder();
            builder.addPaths(fieldPath);
            FieldMask fieldMask = builder.build();

            assertMatchesMask(timestampMsg, fieldMask);
        }

        @Test
        @DisplayName("throws the error when field is not present")
        void fieldIsNotPresent() {
            String fieldPath = Any.getDescriptor()
                                  .getFields()
                                  .get(0)
                                  .getFullName();
            FieldMask.Builder builder = FieldMask.newBuilder();
            builder.addPaths(fieldPath);
            FieldMask fieldMask = builder.build();

            assertThrows(AssertionError.class, () -> assertMatchesMask(timestampMsg, fieldMask));
        }

        @Test
        @DisplayName("throws the error when the field value is not set")
        void fieldIsNotSet() {
            String fieldPath = Timestamp.getDescriptor()
                                        .getFields()
                                        .get(0)
                                        .getFullName();
            FieldMask.Builder builder = FieldMask.newBuilder();
            builder.addPaths(fieldPath);
            FieldMask fieldMask = builder.build();

            assertThrows(AssertionError.class,
                         () -> assertMatchesMask(Timestamp.getDefaultInstance(), fieldMask));
        }
    }

    @Nested
    @DisplayName("Assert values with inaccuracy")
    class AssertWithInaccuracy {

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
            assertInDelta(recentTime, recentTime, 0);
        }

        @Test
        @DisplayName("when values are close")
        void closeValues() {
            // This method would be called within 10 seconds.
            Tests.assertInDelta(recentTime, now(), 10);
        }

        @Test
        @DisplayName("throw if condition is not met")
        void failure() {
            assertThrows(
                    AssertionError.class,
                    () -> Tests.assertInDelta(100, 200, 2)
            );
        }
    }
}
