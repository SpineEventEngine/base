/*
 * Copyright 2020, TeamDev. All rights reserved.
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

import com.google.common.flogger.FluentLogger;
import com.google.common.truth.Subject;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.common.truth.Truth.assert_;

/**
 * A set of static methods to begin a Truth assertion chain for logging types.
 */
public final class LogTruth {

    /** Prevents instantiation of this utility class. */
    private LogTruth() {
    }

    /** Creates a subject for the passed logger. */
    public static Subject assertThat(@Nullable FluentLogger actual) {
        return assert_().that(actual);
    }

    /** Creates a subject for the passed record. */
    public static LogRecordSubject assertThat(@Nullable LogRecord record) {
        return assertAbout(LogRecordSubject.records()).that(record);
    }

    /** Creates a subject for the logging level. */
    public static Subject assertThat(@Nullable Level actual) {
        return assert_().that(actual);
    }

    /** Creates a subject for the logging API. */
    public static Subject assertThat(@NullableDecl FluentLogger.Api actual) {
        return assert_().that(actual);
    }
}
