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

/**
 * This package contains the classes of the {@link io.spine.testing.logging.mute.MuteLogging}
 * JUnit extension.
 *
 * <p>The extension mutes all output produced by a particular JUnit test or test class.
 *
 * <h3>Usage</h3>
 *
 * <p>To use the extension, add the following dependency to the Gradle project:
 * <pre>
 * testImplementation "io.spine.tools:spine-testlib:$spineBaseVersion"
 * </pre>
 *
 * <p>Then, the extension can be used as follows:
 *
 * <pre>
 * {@literal @}Test
 * {@literal @}MuteLogging
 * void ignoreInvalidClassNames() {
 *     // ...
 * }
 * </pre>
 *
 * <p>or
 *
 * <pre>
 * {@literal @}MuteLogging
 * class ModelVerifierTest {
 *     // ...
 * }
 * </pre>
 *
 * <h3>The problem</h3>
 *
 * <p>A seemingly trivial task of muting the test output can become quite complicated when running
 * multiple tests in a row.
 *
 * <p>Most {@code Logger  implementations are static system-wide tools and in fact have static
 * system-wide state. For example, the <a href="https://docs.oracle.com/javase/8/docs/api/java/util/logging/Logger.html">
 * standard JDK Logger</a> which we use in tests relies on its {@code RootLogger} to handle
 * the console output and system {@code out/err} streams.
 * The {@code RootLogger} instance will be the same for multiple class loggers and is initialized
 * on the first access to a class-level logger.
 *
 * <p>Thus, we cannot just redirect {@code System.out} and {@code System.err} to some temporary
 * output to remove the test logs. By the time we do that, the {@code RootLogger} will already be
 * initialized with the "correct" streams.
 *
 * <p>The {@literal @}{@code MuteLogging} annotation thus uses multiple techniques to mute all
 * console output during the test execution. By now, in addition to redirection of the standard
 * streams, it mutes all the Spine logging, returning the SLF4J {@code NOPLogger} on every
 * logger request.
 *
 * <p>This covers most cases where the program output should be muted, allowing for clean project
 * builds without overly verbose test stack traces.
 *
 * <p>Though, if the project uses some non-Spine logger implementation, there may be
 * more complications.
 */

@CheckReturnValue
@ParametersAreNonnullByDefault
package io.spine.testing.logging.mute;

import com.google.errorprone.annotations.CheckReturnValue;

import javax.annotation.ParametersAreNonnullByDefault;
