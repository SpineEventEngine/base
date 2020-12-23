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

package io.spine.base;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;

/**
 * Testing environment.
 *
 * <p>Detected by checking stack trace for mentions of the known testing frameworks.
 *
 * <p>This option is mutually exclusive with {@link Production}, i.e. one of them is always enabled.
 */
@Immutable
public final class Tests extends StandardEnvironmentType {

    private static final Tests INSTANCE = new Tests();

    @SuppressWarnings("DuplicateStringLiteralInspection" /* Used in another context. */)
    private static final ImmutableList<String> KNOWN_TESTING_FRAMEWORKS =
            ImmutableList.of("org.junit", "org.testng", "io.spine.testing");

    /**
     * The names of the packages that when discovered in a stacktrace would tell that
     * the code is executed under tests.
     *
     * <p>The returned package names are:
     * <ol>
     *     <li>"org.junit"
     *     <li>"org.testng"
     *     <li>"io.spine.testing"
     * </ol>
     *
     * @see #enabled()
     */
    public static ImmutableList<String> knownTestingFrameworks() {
        return KNOWN_TESTING_FRAMEWORKS;
    }

    /**
     * Obtains the singleton instance.
     */
    static Tests type() {
        return INSTANCE;
    }

    /** Prevents direct instantiation. */
    private Tests() {
        super();
    }

    /**
     * Verifies if the code currently runs under a unit testing framework.
     *
     * <p>The method returns {@code true} if {@linkplain #knownTestingFrameworks()
     * known testing framework packages} are discovered in the stacktrace.
     *
     * @return {@code true} if the code runs under a testing framework, {@code false} otherwise
     * @implNote In addition to checking the stack trace, this method checks the
     *         environment variable value. If you wish to simulate not being in tests, the
     *         variable must be set to {@code false} explicitly. If your framework is not
     *         among the {@linkplain #knownTestingFrameworks() known ones}, make sure to set
     *         the system property explicitly.
     * @see #knownTestingFrameworks()
     */
    @Override
    protected boolean enabled() {
        TestsProperty property = new TestsProperty();
        if (property.isSet()) {
            return property.value();
        }

        String stacktrace = Throwables.getStackTraceAsString(new RuntimeException(""));
        boolean result =
                knownTestingFrameworks().stream()
                                        .anyMatch(stacktrace::contains);
        return result;
    }
}
