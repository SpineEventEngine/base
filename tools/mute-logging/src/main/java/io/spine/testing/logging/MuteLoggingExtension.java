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

package io.spine.testing.logging;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.util.Optional;

/**
 * A JUnit {@link org.junit.jupiter.api.extension.Extension Extension} which mutes all the logs
 * for a test case.
 *
 * <p>Do not use this extension directly. Mark the target test method or class with
 * the {@link MuteLogging} annotation.
 *
 * @see MuteLogging
 */
public final class MuteLoggingExtension implements BeforeEachCallback, AfterEachCallback {

    private static final String ROOT = "";
    private final MutingLoggerTap loggerTap;
    /**
     * Creates new instance of the extension, redirecting to the stream which stores the output
     * into memory.
     */
    public MuteLoggingExtension() {
        this.loggerTap = new MutingLoggerTap(ROOT);
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        loggerTap.install();
    }

    @Override
    public void afterEach(ExtensionContext context) throws IOException {
        Optional<Throwable> exception = context.getExecutionException();
        if (exception.isPresent()) {
            loggerTap.flushToSystemErr();
        }
        loggerTap.restore();
    }
}
