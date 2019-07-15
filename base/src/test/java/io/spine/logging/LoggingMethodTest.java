/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.logging;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This test suite tests default methods of the {@link Logging} interface.
 */
@DisplayName("Logging should provide a shortcut methods")
@SuppressWarnings("deprecation") // ... and remove the class once deprecated API is removed.
class LoggingMethodTest {

    @Nested
    @DisplayName("with one message parameter")
    class WithMessage extends MethodGroupTest<Consumer<String>> {

        WithMessage() {
            super(0, false);
        }

        @Test
        void _trace() {
            assertMethod(object()::_trace, Level.TRACE);
        }

        /**
         * Tests debug level.
         *
         * @implNote This method passes {@code TRACE} instead of {@code DEBUG}
         * because of the <a href="https://jira.qos.ch/browse/SLF4J-376">bug</a>
         * in {@link org.slf4j.event.EventRecodingLogger#debug(String)
         * EventRecodingLogger.debug(String)}.
         *
         * <p>This issue is not yet closed, but the
         * <a href="https://github.com/qos-ch/slf4j/blob/master/slf4j-api/src/main/java/org/slf4j/event/EventRecodingLogger.java">
         * EventRecodingLogger</a> class in the master branch of the coming v1.8 of Slf4J
         * already fixes this issue.
         */
        @Test
        void _debug() {
            assertMethod(object()::_debug, Level.TRACE);
        }

        @Test
        void _info() {
            assertMethod(object()::_info, Level.INFO);
        }

        @Test
        void _warn() {
            assertMethod(object()::_warn, Level.WARN);
        }

        @Test
        void _error() {
            assertMethod(object()::_error, Level.ERROR);
        }

        @Override
        void call(Consumer<String> method, String message,
                  @Nullable Object @Nullable... params) {
            method.accept(message);
        }
    }

    @Nested
    @DisplayName("with format and one argument")
    class SingleArgument extends MethodGroupTest<BiConsumer<String, Object>> {

        SingleArgument() {
            super(1, false);
        }

        @Test
        void _trace() {
            assertMethod(object()::_trace, Level.TRACE);
        }

        @Test
        void _debug() {
            assertMethod(object()::_debug, Level.DEBUG);
        }

        @Test
        void _info() {
            assertMethod(object()::_info, Level.INFO);
        }

        @Test
        void _warn() {
            assertMethod(object()::_warn, Level.WARN);
        }

        @Test
        void _error() {
            assertMethod(object()::_error, Level.ERROR);
        }

        @Override
        void call(BiConsumer<String, Object> method, String format,
                  @Nullable Object @Nullable... params) {
            checkNotNull(params);
            Object[] arg = Arrays.copyOf(params, 1, Object[].class);
            method.accept(format, arg[0]);
        }
    }

    @Nested
    @DisplayName("with format and two arguments")
    class TwoArguments extends MethodGroupTest<TriConsumer<String, Object, Object>>{

        TwoArguments() {
            super(2, false);
        }

        @Test
        void _trace() {
            assertMethod(object()::_trace, Level.TRACE);
        }

        @Test
        void _debug() {
            assertMethod(object()::_debug, Level.DEBUG);
        }

        @Test
        void _info() {
            assertMethod(object()::_info, Level.INFO);
        }

        @Test
        void _warn() {
            assertMethod(object()::_warn, Level.WARN);
        }

        @Test
        void _error() {
            assertMethod(object()::_error, Level.ERROR);
        }

        @Override
        void call(TriConsumer<String, Object, Object> method,
                  String fmt,
                  @Nullable Object @Nullable... params) {
            checkNotNull(params);
            Object[] arg = Arrays.copyOf(params, 2, Object[].class);
            method.accept(fmt, arg[0], arg[1]);
        }
    }

    @Nested
    @DisplayName("with format and three arguments")
    class ThreeArguments extends MethodGroupTest<QuadriConsumer<String, Object, Object, Object>> {

        ThreeArguments() {
            super(3, false);
        }

        @Test
        void _trace() {
            assertMethod(object()::_trace, Level.TRACE);
        }

        @Test
        void _debug() {
            assertMethod(object()::_debug, Level.DEBUG);
        }

        @Test
        void _info() {
            assertMethod(object()::_info, Level.INFO);
        }

        @Test
        void _warn() {
            assertMethod(object()::_warn, Level.WARN);
        }

        @Test
        void _error() {
            assertMethod(object()::_error, Level.ERROR);
        }

        @Override
        void call(QuadriConsumer<String, Object, Object, Object> method,
                  String fmt,
                  @Nullable Object @Nullable... params) {
            checkNotNull(params);
            Object[] arg = Arrays.copyOf(params, 3, Object[].class);
            method.accept(fmt, arg[0], arg[1], arg[2]);
        }
    }

    @Nested
    @DisplayName("with Throwable, format, and arguments")
    class ThrowableWithFormat extends MethodGroupTest<TriConsumer<Throwable, String, Object[]>> {

        ThrowableWithFormat() {
            // Pass one which in combination with `withThrowable = true` means `Object[]`.
            super(1, true);
        }

        @Test
        void _warn() {
            assertMethod(object()::_warn, Level.WARN);
        }

        @Test
        void _error() {
            assertMethod(object()::_error, Level.ERROR);
        }

        @Override
        void call(TriConsumer<Throwable, String, Object[]> method,
                  String fmt,
                  @Nullable Object @Nullable ... params) {
            checkNotNull(params);
            Object[] arg = Arrays.copyOf(params, 3, Object[].class);
            method.accept((Throwable)arg[0], fmt, (Object[]) arg[1]);
        }
    }
}
