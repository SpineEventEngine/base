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

package io.spine.util;

import com.google.common.testing.NullPointerTester;
import io.spine.testing.TestValues;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.testing.Assertions.assertIllegalArgument;
import static io.spine.testing.Assertions.assertIllegalState;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static io.spine.util.Exceptions.newIllegalStateException;
import static io.spine.util.Exceptions.unsupported;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Exceptions utility class should")
class ExceptionsTest extends UtilityClassTest<Exceptions> {

    ExceptionsTest() {
        super(Exceptions.class);
    }

    @Override
    protected void configure(NullPointerTester tester) {
        tester.setDefault(Exception.class, new RuntimeException(""))
              .setDefault(Throwable.class, new Error())
              .testAllPublicStaticMethods(Exceptions.class);
    }

    @Nested
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @DisplayName("throw UnsupportedOperationException")
    class ThrowsUnsupported {

        @Test
        @DisplayName("without message")
        void noParams() {
            assertThrows(UnsupportedOperationException.class, Exceptions::unsupported);
        }

        @Test
        @DisplayName("with message")
        void withMessage() {
            assertThrows(
                    UnsupportedOperationException.class,
                    () -> unsupported(TestValues.randomString())
            );
        }

        @Test
        @DisplayName("with formatted message")
        void formattedMessage() {
            String arg1 = getClass().getCanonicalName();
            long arg2 = 100500L;
            UnsupportedOperationException exception =
                    assertThrows(
                            UnsupportedOperationException.class,
                            () -> unsupported("%s %d", arg1, arg2));
            String exceptionMessage = exception.getMessage();
            assertTrue(exceptionMessage.contains(arg1));
            assertTrue(exceptionMessage.contains(String.valueOf(arg2)));
        }
    }

    @Nested
    @DisplayName("throw `IllegalArgumentException` with")
    @SuppressWarnings({"ResultOfMethodCallIgnored", "ThrowableNotThrown"})
    class ThrowIAE {

        @Test
        @DisplayName("formatted message")
        void formattedMessage() {
            assertIllegalArgument(
                    () -> newIllegalArgumentException("%d, %d, %s kaboom", 1, 2, "three"));
        }

        @Test
        @DisplayName("formatted message with cause")
        void messageAndCause() {
            assertIllegalArgument(() -> newIllegalArgumentException(
                    new RuntimeException("checking"), "%s", "stuff")
            );
        }
    }

    @Nested
    @DisplayName("throw IllegalStateException with")
    @SuppressWarnings({"ResultOfMethodCallIgnored", "ThrowableNotThrown"})
    class ThrowISE {

        @Test
        @DisplayName("formatted message")
        void formattedMessage() {
            assertIllegalState(() -> newIllegalStateException("%s check %s", "state", "failed"));
        }

        @Test
        @DisplayName("formatted message with cause")
        void messageWithCause() {
            assertIllegalState(() -> newIllegalStateException(
                    new RuntimeException(getClass().getSimpleName()), "%s %s", "taram", "param")
            );
        }
    }
}
