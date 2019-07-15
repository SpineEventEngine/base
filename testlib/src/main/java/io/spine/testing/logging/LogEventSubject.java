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

import com.google.common.truth.ComparableSubject;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.ObjectArraySubject;
import com.google.common.truth.StandardSubjectBuilder;
import com.google.common.truth.StringSubject;
import com.google.common.truth.Subject;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.event.Level;
import org.slf4j.event.SubstituteLoggingEvent;

/**
 * Propositions for {@link SubstituteLoggingEvent} subjects.
 */
@SuppressWarnings("DuplicateStringLiteralInspection") // method names specific to Slf4J
public class LogEventSubject extends Subject<LogEventSubject, SubstituteLoggingEvent> {

    private LogEventSubject(FailureMetadata metadata, @Nullable SubstituteLoggingEvent actual) {
        super(metadata, actual);
    }

    /** Returns a {@code StringSubject} to make assertions about the logging event message. */
    public StringSubject hasMessageThat() {
        StandardSubjectBuilder check = check("getMessage()");
        return check.that(actual().getMessage());
    }

    /** Obtains subject for the logging level. */
    public ComparableSubject<?, Level> hasLevelThat() {
        StandardSubjectBuilder check = check("getLevel()");
        return check.that(actual().getLevel());
    }

    /** Obtains subject for the logging event arguments. */
    public ObjectArraySubject hasArgumentsThat() {
        StandardSubjectBuilder check = check("getArgumentArray()");
        return check.that(actual().getArgumentArray());
    }

    /** Obtains factory for creating logging event subjects for actual values. */
    public static Subject.Factory<LogEventSubject, SubstituteLoggingEvent> events() {
        return (LogEventSubject::new);
    }
}
