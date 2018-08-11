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

package io.spine.logging;

import com.google.common.collect.Queues;
import io.spine.logging.given.LoggingObject;
import io.spine.testing.logging.LogEventSubject;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.event.Level;
import org.slf4j.event.SubstituteLoggingEvent;
import org.slf4j.helpers.SubstituteLogger;

import java.util.Queue;

import static com.google.common.base.Preconditions.checkArgument;
import static io.spine.logging.Logging.redirect;
import static io.spine.testing.TestValues.random;
import static io.spine.testing.logging.LogTruth.assertThat;

/**
 * Abstract base for testing groups of logging methods that accept the same number of
 * arguments.
 *
 * @author Alexander Yevsyukov
 */
abstract class MethodGroupTest<M> {

    private final int numberOfArguments;
    private final boolean isFormat;

    private Logging object;
    private Queue<SubstituteLoggingEvent> queue;

    MethodGroupTest(int numberOfArguments) {
        checkArgument(numberOfArguments >= 0);
        this.numberOfArguments = numberOfArguments;
        this.isFormat = numberOfArguments > 0;
    }

    protected Logging object() {
        return object;
    }

    @BeforeEach
    void redirectLogging() {
        object = new LoggingObject();
        queue = Queues.newArrayDeque();
        SubstituteLogger logger = getLogger();
        redirect(logger, queue);
    }

    /**
     * Casts the log of the object under the test to {@link SubstituteLogger}.
     *
     * @implNote The cast is safe since we run this code under tests.
     * @see LoggingTest#loggerInstance()
     */
    private SubstituteLogger getLogger() {
        return (SubstituteLogger) object.log();
    }

    /**
     * Performs actual invocation of the logging method via the passed message reference.
     *
     * @param method
     *        a reference to the method through a {@code FunctionalInterface}
     * @param messageOfFormat
     *        it is a log message for methods that do not accept other
     *        {@linkplain #numberOfArguments arguments},
     *        otherwise, it's a format string in
     *        <a href="https://www.slf4j.org/faq.html#logging_performance">Slf4J convention</a>
     */
    abstract void call(M method,
                       String messageOfFormat,
                       @Nullable Object @Nullable... params);

    /**
     * Asserts the passed method.
     *
     * @implNote Generates random parameters and asserts that all them are passed to the generated
     *           logging event message
     */
    void assertMethod(M method, Level level) {
        String text = randomText();
        Object[] arguments = randomArguments();
        String stringOrMessage = isFormat ? text + " {}" : text;

        call(method,
             stringOrMessage,
             arguments);

        LogEventSubject subject = assertEvent(stringOrMessage, level);

        if (isFormat) {
            subject.hasArgumentsThat()
                   .asList()
                   .containsExactly(arguments);
        }
    }

    private @Nullable Object[] randomArguments() {
        if (!isFormat) {
            return null;
        }
        Object[] result = new Object[numberOfArguments];
        for (int i = 0; i < numberOfArguments; i++) {
            result[i] = randomArgument();
        }
        return result;
    }

    /**
     * Asserts that the logging event queue has only one non-null event and
     * returns the subject for it.
     */
    private LogEventSubject assertEvent(String msg, Level level) {
        assertThat(queue).hasSize(1);
        LogEventSubject subject = assertThat(queue.poll());
        subject.isNotNull();
        subject.hasMessageThat()
               .isEqualTo(msg);
        subject.hasLevelThat()
               .isEqualTo(level);
        return subject;
    }

    private static String randomText() {
        return "Fmt" + random(100);
    }

    private static String randomArgument() {
        return "Arg" + random(10_000);
    }
}
