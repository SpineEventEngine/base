/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import com.google.common.truth.ExpectFailure.SimpleSubjectBuilderCallback;
import com.google.common.truth.Subject;
import io.spine.testing.SubjectTest;
import io.spine.testing.TestValues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import static com.google.common.truth.ExpectFailure.assertThat;
import static io.spine.testing.logging.LogRecordSubject.NO_LOG_RECORD;
import static io.spine.testing.logging.LogRecordSubject.records;

@DisplayName("`LogRecordSubject` should have")
class LogRecordSubjectTest extends SubjectTest<LogRecordSubject, LogRecord> {

    private LogRecord record;
    private String msg;
    private Level level;
    private Throwable throwable;
    private Object[] parameters;

    @Override
    protected Subject.Factory<LogRecordSubject, LogRecord> subjectFactory() {
        return records();
    }

    @BeforeEach
    void createRecord() {
        msg = "Test log message" + TestValues.randomString();
        level = Level.FINE;
        record = new LogRecord(level, msg);
        throwable = new RuntimeException("Testing LogRecordSubject handling of Throwable");
        parameters = new Object[] { '0', "1", 2, 3L, 4.0f, 5.0d, true };
        record.setParameters(parameters);
        record.setThrown(throwable);
    }

    @Test
    @DisplayName("the check for no logged records")
    @SuppressWarnings("ResultOfMethodCallIgnored")  /* Intentionally. */
    void noRecords() {
        checkFails(whenTesting -> whenTesting.that(null).hasLevelThat());
        checkFails(whenTesting -> whenTesting.that(null).hasClassNameThat());
        checkFails(whenTesting -> whenTesting.that(null).hasMethodNameThat());
        checkFails(whenTesting -> whenTesting.that(null).hasMessageThat());
        checkFails(whenTesting -> whenTesting.that(null).hasParametersThat());
        checkFails(whenTesting -> whenTesting.that(null).hasThrowableThat());
        checkFails(whenTesting -> whenTesting.that(null).isDebug());
        checkFails(whenTesting -> whenTesting.that(null).isError());
    }

    private void
    checkFails(SimpleSubjectBuilderCallback<LogRecordSubject, LogRecord> assertionCallback) {
        var failure = expectFailure(assertionCallback);
        assertThat(failure)
                .factKeys()
                .contains(NO_LOG_RECORD);
    }

    @Test
    void hasMessageThat() {
        assertWithSubjectThat(record)
                .hasMessageThat()
                .isEqualTo(msg);

        var notExpected = TestValues.randomString();

        var failure = expectFailure(
                whenTesting -> whenTesting.that(record)
                                          .hasMessageThat()
                                          .isEqualTo(notExpected)
        );
        var assertFailure = assertThat(failure);
        assertFailure.factKeys()
                     .containsAnyOf(EXPECTED, BUT_WAS);
    }

    @Test
    void hasLevelThat() {
        assertWithSubjectThat(record)
                .hasLevelThat()
                .isEqualTo(level);

        expectSomeFailure(
                whenTesting -> whenTesting.that(record)
                                          .hasLevelThat()
                                          .isEqualTo(Level.OFF)
        );
    }

    @Test
    void isDebug() {
        assertWithSubjectThat(record)
                .isDebug();

        expectSomeFailure(whenTesting -> whenTesting.that(record)
                                                    .isError());
    }

    @Test
    void isError() {
        record.setLevel(Level.SEVERE);
        assertWithSubjectThat(record)
                .isError();

        expectSomeFailure(whenTesting -> whenTesting.that(record)
                                                    .isDebug());
    }

    @Test
    void hasParametersThat() {
        assertWithSubjectThat(record)
                .hasParametersThat()
                .asList()
                .containsExactlyElementsIn(parameters);

        expectSomeFailure(whenTesting -> whenTesting.that(record)
                                                    .hasParametersThat()
                                                    .isEmpty());
    }

    @Test
    void hasThrowableThat() {
        assertWithSubjectThat(record)
                .hasThrowableThat()
                .isInstanceOf(throwable.getClass());

        expectSomeFailure(whenTesting -> whenTesting.that(record)
                                                    .hasThrowableThat()
                                                    .isNull());
    }

    @Test
    void hasMethodThat() {
        var method = "hasMethodNameThat";
        record.setSourceMethodName(method);
        assertWithSubjectThat(record)
                .hasMethodNameThat()
                .isEqualTo(method);
        expectSomeFailure(whenTesting -> whenTesting.that(record)
                                                    .hasMethodNameThat()
                                                    .isEmpty());
    }

    @Test
    void hasClassThat() {
        var className = LogRecordSubjectTest.class.getName();
        record.setSourceClassName(className);
        assertWithSubjectThat(record)
                .hasClassNameThat()
                .isEqualTo(className);
        expectSomeFailure(whenTesting -> whenTesting.that(record)
                                                    .hasClassNameThat()
                                                    .isEmpty());
    }
}
