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

package io.spine.testing.logging;

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
 * @author Dmytro Dashenkov
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
        unMute(context);
    }

    private void mute() {
        temporaryOutput.install();
    }

    private void unMute(ExtensionContext context) throws IOException {
        ProgramOutput standardOutput = ProgramOutput.fromSystem();
        standardOutput.install();
        Optional<Throwable> exception = context.getExecutionException();
        if (exception.isPresent()) {
            memoizingStream.flushTo(standardOutput.err);
        } else {
            memoizingStream.clear();
        }
    }

    /**
     * The output of a software component.
     */
    private static final class ProgramOutput {

        @SuppressWarnings("UseOfSystemOutOrSystemErr")
        private static final ProgramOutput SYSTEM = new ProgramOutput(System.out, System.err);

        private final PrintStream out;
        private final PrintStream err;

        private ProgramOutput(PrintStream out, PrintStream err) {
            this.out = out;
            this.err = err;
        }

        /**
         * Creates an {@code ProgramOutput} into the given stream.
         *
         * <p>Both the output and error streams are represented with the given target stream.
         *
         * @param stream the target stream
         * @return new instance of {@code ProgramOutput}
         */
        private static ProgramOutput into(PrintStream stream) {
            return new ProgramOutput(stream, stream);
        }

        /**
         * Obtains an instance of {@code ProgramOutput} from the standard I/O of this process.
         *
         * @return the standard I/O output
         */
        private static ProgramOutput fromSystem() {
            return SYSTEM;
        }

        /**
         * Installs this output for the current process.
         */
        private void install() {
            System.setOut(out);
            System.setErr(err);
        }
    }
}
