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

import io.spine.logging.Logging;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.io.PrintStream;
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

    private final MemoizingStream memoizingStream = new MemoizingStream();
    private final PrintStream temporaryOutputStream = new PrintStream(memoizingStream);
    private final ProgramOutput temporaryOutput = ProgramOutput.into(temporaryOutputStream);

    @Override
    public void beforeEach(ExtensionContext context) {
        mute();
    }

    @Override
    public void afterEach(ExtensionContext context) throws IOException {
        unmute(context);
    }

    private void mute() {
        Logging.mute();
        temporaryOutput.install();
    }

    private void unmute(ExtensionContext context) throws IOException {
        ProgramOutput standardOutput = ProgramOutput.fromSystem();
        standardOutput.install();
        Logging.unmute();

        Optional<Throwable> exception = context.getExecutionException();
        if (exception.isPresent()) {
            memoizingStream.flushTo(standardOutput.err());
        } else {
            memoizingStream.clear();
        }
    }
}
