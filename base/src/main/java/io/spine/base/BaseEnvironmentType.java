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

package io.spine.base;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;

import java.util.regex.Pattern;

/**
 * Environment types provided by the base library.
 *
 * <p>Provides a {@code TESTS} option, that looks into the stack trace to find mentions of
 * known testing frameworks. It also looks at an environment variable.
 *
 * <p>Also provides a {@code PRODUCTION} option. System is considered to be in {@code PRODUCTION}
 * if its not in {@code TESTS}, i.e. they are mutually exclusive.
 */
enum BaseEnvironmentType implements EnvironmentType {

    @SuppressWarnings("AccessOfSystemProperties" /* OK as we need system properties for this class. */)
    TESTS {
        /**
         * Verifies if the code currently runs under a unit testing framework.
         *
         * <p>The method returns {@code true} if the following packages are discovered
         * in the stacktrace:
         * <ul>
         *     <li>{@code org.junit}
         *     <li>{@code org.testng}
         * </ul>
         *
         * @return {@code true} if the code runs under a testing framework, {@code false} otherwise
         */
        @Override
        public boolean currentlyOn() {
            // Check the environment variable. We may run under unknown testing framework or
            // tests may require production-like mode, which they simulate by setting
            // the property to `false`.
            String testProp = System.getProperty(ENV_KEY_TESTS);
            if (testProp != null) {
                testProp = TEST_PROP_PATTERN.matcher(testProp)
                                            .replaceAll("");
                return String.valueOf(true)
                             .equalsIgnoreCase(testProp) || "1".equals(testProp);
            }

            // Check stacktrace for known frameworks.
            String stacktrace = Throwables.getStackTraceAsString(new RuntimeException(""));
            return KNOWN_TESTING_FRAMEWORKS.stream()
                                           .anyMatch(stacktrace::contains);
        }

        @Override
        public void reset() {
            System.clearProperty(ENV_KEY_TESTS);
        }

        @Override
        public void setTo() {
            System.setProperty(ENV_KEY_TESTS, String.valueOf(true));
        }
    }, PRODUCTION {
        @Override
        public boolean currentlyOn() {
            return !TESTS.currentlyOn();
        }

        @Override
        public void reset() {
            // NOP.
        }

        @Override
        public void setTo() {
            // NOP.
        }
    };

    /**
     * The key name of the system property which tells if a code runs under a testing framework.
     *
     * <p>If your testing framework is not among the
     * {@link BaseEnvironmentType#KNOWN_TESTING_FRAMEWORKS}, set this property to {@code true}
     * before running tests.
     */
    public static final String ENV_KEY_TESTS = "io.spine.tests";

    @SuppressWarnings("DuplicateStringLiteralInspection" /* Used in another context. */)
    public static final ImmutableList<String> KNOWN_TESTING_FRAMEWORKS =
            ImmutableList.of("org.junit", "org.testng");

    private static final Pattern TEST_PROP_PATTERN = Pattern.compile("\"' ");
}
