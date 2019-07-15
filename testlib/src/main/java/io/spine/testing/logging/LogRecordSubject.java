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

import com.google.common.truth.DefaultSubject;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.ObjectArraySubject;
import com.google.common.truth.StandardSubjectBuilder;
import com.google.common.truth.StringSubject;
import com.google.common.truth.Subject;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Propositions for {@link LogRecord} subjects.
 */
@SuppressWarnings("DuplicateStringLiteralInspection") // method names specific to Java Logging
public class LogRecordSubject extends Subject<LogRecordSubject, LogRecord> {

    /** Obtains factory for creating log record subjects for actual values. */
    public static Subject.Factory<LogRecordSubject, LogRecord> records() {
        return LogRecordSubject::new;
    }

    private LogRecordSubject(FailureMetadata metadata, @Nullable LogRecord actual) {
        super(metadata, actual);
    }

    /** Returns a {@code StringSubject} to make assertions about the log record message. */
    public StringSubject hasMessageThat() {
        StandardSubjectBuilder check = check("getMessage()");
        return check.that(actual().getMessage());
    }

    /** Obtains subject for the logging level. */
    public Subject<DefaultSubject, Object> hasLevelThat() {
        StandardSubjectBuilder check = check("getLevel()");
        Subject<DefaultSubject, Object> that = check.that(actual().getLevel());
        return that;
    }

    /** Asserts that the level of the record is {@code Level.FINE}. */
    public void isDebug() {
        hasLevelThat().isEqualTo(Level.FINE);
    }

    /** Obtains subject for the logging event arguments. */
    public ObjectArraySubject hasParametersThat() {
        StandardSubjectBuilder check = check("getParameters()");
        return check.that(actual().getParameters());
    }
}
