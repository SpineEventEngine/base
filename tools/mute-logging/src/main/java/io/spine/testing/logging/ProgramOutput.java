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

import java.io.PrintStream;

/**
 * The output of a software component.
 */
final class ProgramOutput {

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    private static final ProgramOutput SYSTEM = new ProgramOutput(System.out, System.err);

    private final PrintStream out;
    private final PrintStream err;

    ProgramOutput(PrintStream out, PrintStream err) {
        this.out = out;
        this.err = err;
    }

    PrintStream err() {
        return err;
    }

    /**
     * Creates an {@code ProgramOutput} into the given stream.
     *
     * <p>Both the output and error streams are represented with the given target stream.
     *
     * @param stream the target stream
     * @return new instance of {@code ProgramOutput}
     */
    static ProgramOutput into(PrintStream stream) {
        return new ProgramOutput(stream, stream);
    }

    /**
     * Obtains an instance of {@code ProgramOutput} from the standard I/O of this process.
     *
     * @return the standard I/O output
     */
    static ProgramOutput fromSystem() {
        return SYSTEM;
    }

    /**
     * Installs this output for the current process.
     */
    void install() {
        System.setOut(out);
        System.setErr(err);
    }
}
