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

import com.google.common.collect.ImmutableList;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Redirects the output of the associated JDK Logger to a memory stream.
 */
final class MutingLoggerTap {

    /** The name of the associated logger. */
    private final String name;

    // Previous state of the Logger that we amend.
    private boolean usedParentHandlers;
    private @Nullable MemoizingStream memoizingStream;
    private @Nullable Handler handler;
    private @Nullable ImmutableList<Handler> previousHandlers;

    MutingLoggerTap(String name) {
        this.name = name;
    }

    /**
     * Installs the tap on the logger.
     */
    synchronized void install() {
        memoizingStream = new MemoizingStream();
        createHandler();
        replaceHandlers();
    }

    /**
     * Creates a new handler copying configuration from the first handler of the logger.
     *
     * @see #replaceHandlers()
     */
    private void createHandler() {
        Logger logger = logger();
        Handler firstHandler = logger.getHandlers()[0];
        Formatter formatter = firstHandler.getFormatter();
        handler = new StreamHandler(memoizingStream, formatter);
        try {
            handler.setEncoding(firstHandler.getEncoding());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
        handler.setLevel(firstHandler.getLevel());
    }

    /**
     * Replaces all handlers of the logger with the {@link #handler() tapping handler}.
     *
     * <p>The handlers and the flag for using {@linkplain Logger#getUseParentHandlers()
     * parent handlers} is remembered and then restored when the tap is
     * {@linkplain #remove() removed}.
     *
     * @see #createHandler()
     */
    private void replaceHandlers() {
        // Remember configuration of the logger.
        Logger logger = logger();
        this.usedParentHandlers = logger.getUseParentHandlers();
        this.previousHandlers = ImmutableList.copyOf(logger.getHandlers());
        for (Handler handler : previousHandlers) {
            logger.removeHandler(handler);
        }
        logger.addHandler(handler());
    }

    /**
     * Restores the associated logger to the previous state.
     */
    synchronized void remove() {
        Logger logger = logger();
        logger.removeHandler(handler());
        logger.setUseParentHandlers(usedParentHandlers);
        for (Handler previousHandler : previousHandlers()) {
            logger.addHandler(previousHandler);
        }
        stream().reset();

        memoizingStream = null;
        handler = null;
        previousHandlers = null;
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    synchronized void flushToSystemErr() throws IOException {
        stream().flushTo(System.err);
    }

    private Handler handler() {
        return checkNotNull(handler);
    }

    private MemoizingStream stream() {
        return checkNotNull(memoizingStream);
    }

    private ImmutableList<Handler> previousHandlers() {
        return checkNotNull(previousHandlers);
    }

    private Logger logger() {
        return Logger.getLogger(this.name);
    }
}
