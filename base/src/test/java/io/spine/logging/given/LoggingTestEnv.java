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

package io.spine.logging.given;

import io.spine.logging.Logging;
import org.slf4j.Logger;

import java.util.function.Supplier;

public class LoggingTestEnv {

    /** Prevents instantiation of this utility class. */
    private LoggingTestEnv() {}

    /** The root of the class hierarchy with the logger supplier. */
    public static class Base {

        private final Supplier<Logger> loggerSupplier = Logging.supplyFor(getClass());

        public Logger log() {
            return loggerSupplier.get();
        }
    }

    @SuppressWarnings("EmptyClass") // We need the class only to build the hierarchy.
    public static class ChildOne extends Base {
    }

    @SuppressWarnings("EmptyClass") // Same as for `ChildOne`.
    public static class ChildTwo extends Base {
    }
}
