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

import com.google.common.truth.StringSubject;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static java.lang.System.lineSeparator;

/**
 * Test fixture for testing loggers based on JDK Logging.
 */
public class AssertingHandler extends Handler {

    private static final String FACT_NO_RECORDS = "There were no log records";

    private @Nullable List<LogRecord> logRecords = new ArrayList<>();

    @Override
    public void publish(LogRecord record) {
        if (isLoggable(record)) {
            logRecords().add(record);
        }
    }

    /** Sets the level to {@code Level.FINE}. */
    public void setDebugLevel() {
        setLevel(Level.FINE);
    }

    /** Sets the level to {@code Level.SEVERE}. */
    public void setErrorLevel() {
        setLevel(Level.SEVERE);
    }

    private List<LogRecord> logRecords() {
        return checkNotNull(logRecords, "The handler is already closed.");
    }

    /**
     * Obtains the subject for asserting text output of the first log record.
     */
    public StringSubject assertOnlyLog() {
        LogRecord logRecord = firstRecord();
        assertWithMessage(FACT_NO_RECORDS)
                .that(logRecord)
                .isNotNull();
        flush();
        StringSubject subject = assertThat(logRecordToString(logRecord));
        return subject;
    }

    /**
     * Obtains the subject for the only log record placed to the log.
     *
     * @throws AssertionError if the were no records or more than one log record
     */
    public LogRecordSubject assertRecord() {
        LogRecord logRecord = firstRecord();
        flush();
        LogRecordSubject subject = LogTruth.assertThat(logRecord);
        return subject;
    }

    private LogRecord firstRecord() {
        assertThat(logRecords)
                .hasSize(1);
        return logRecords().get(0);
    }

    /**
     * Asserts that there were no log messages.
     */
    public void assertNoLogs() {
        assertWithMessage("unexpected log recorded")
                .that(logRecords)
                .isEmpty();
    }

    private static String logRecordToString(LogRecord logRecord) {
        StringBuilder sb = new StringBuilder();
        String message = new SimpleFormatter().formatMessage(logRecord);
        sb.append(logRecord.getLevel())
          .append(": ")
          .append(message)
          .append(lineSeparator());

        Throwable thrown = logRecord.getThrown();
        if (thrown != null) {
            sb.append(thrown);
        }
        return sb.toString().trim();
    }

    @Override
    public void flush() {
        logRecords().clear();
    }

    @Override
    public void close() {
        logRecords = null;
    }
}
