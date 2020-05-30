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
 * Environment types provided by the {@code base} library.
 */
public enum BaseEnvironmentType implements EnvironmentType {

    /**
     * Testing environment.
     *
     * <p>Detected by checking stack trace for mentions of the known testing frameworks.
     *
     * <p>This option is mutually exclusive with {@link #PRODUCTION}, i.e. one of them is always
     * enabled.
     */
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
         * @implNote In addition to checking the stack trace, this method checks the environment
         * variable value. If you wish to simulate not being in tests, the variable must be set
         * to {@code false} explicitly. If your framework is not among the
         * {@linkplain #KNOWN_TESTING_FRAMEWORKS known ones}, make sure to set the system property
         * explicitly.
         */
        @Override
        public boolean enabled() {
            String testProp = System.getProperty(ENV_KEY_TESTS);
            if (testProp != null) {
                testProp = TEST_PROP_PATTERN.matcher(testProp)
                                            .replaceAll("");
                return String.valueOf(true)
                             .equalsIgnoreCase(testProp) || "1".equals(testProp);
            }

            String stacktrace = Throwables.getStackTraceAsString(new RuntimeException(""));
            return KNOWN_TESTING_FRAMEWORKS.stream()
                                           .anyMatch(stacktrace::contains);
        }
    },

    /**
     * A non-testing environment.
     *
     * <p>If the system is not in the {@link #TESTS} environment, it is in the production
     * environment.
     */
    PRODUCTION {
        @Override
        public boolean enabled() {
            return !TESTS.enabled();
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
