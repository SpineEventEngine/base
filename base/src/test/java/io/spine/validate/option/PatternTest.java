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

package io.spine.validate.option;

import com.google.protobuf.StringValue;
import io.spine.test.validate.AllThePatterns;
import io.spine.test.validate.PatternStringFieldValue;
import io.spine.test.validate.SimpleStringValue;
import io.spine.test.validate.WithStringValue;
import io.spine.validate.ValidationOfConstraintTest;
import org.checkerframework.checker.regex.qual.Regex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.validate.ValidationOfConstraintTest.VALIDATION_SHOULD;
import static io.spine.validate.given.MessageValidatorTestEnv.EMAIL;
import static io.spine.validate.given.MessageValidatorTestEnv.MATCH_REGEXP_MSG;
import static java.lang.String.format;

@DisplayName(VALIDATION_SHOULD + "analyze `(pattern)` option and")
class PatternTest extends ValidationOfConstraintTest {

    @Test
    @DisplayName("find out that string matches to regex pattern")
    void findOutThatStringMatchesToRegexPattern() {
        var msg = patternStringFor("valid.email@mail.com");
        assertValid(msg);
    }

    @Test
    @DisplayName("find out that string does not match to regex pattern")
    void findOutThatStringDoesNotMatchToRegexPattern() {
        var msg = patternStringFor("invalid email");
        assertNotValid(msg);
    }

    @Test
    @DisplayName("consider field is valid if `PatternOption` is not set")
    void considerFieldIsValidIfNoPatternOptionSet() {
        var msg = StringValue.getDefaultInstance();
        assertValid(msg);
    }

    @Test
    @DisplayName("provide one valid violation if string does not match to regex pattern")
    void provideOneValidViolationIfStringDoesNotMatchToRegexPattern() {
        var msg = patternStringFor("invalid email");
        @Regex
        String regex = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        var expectedErrMsg = format(MATCH_REGEXP_MSG, regex);
        assertSingleViolation(msg, expectedErrMsg, EMAIL);
    }

    @Test
    @DisplayName("find out that string does not match to external regex pattern")
    void findOutThatStringDoesNotMatchExternalConstraint() {
        var stringValue = SimpleStringValue.newBuilder()
                .setValue("A wordy sentence")
                .build();
        var msg = WithStringValue.newBuilder()
                .setStringValue(stringValue)
                .build();
        assertValid(stringValue);
        assertNotValid(msg);
    }

    @Test
    @DisplayName("validate with `case_insensitive` modifier")
    void caseInsensitive() {
        var message = AllThePatterns.newBuilder()
                .setLetters("AbC")
                .buildPartial();
        assertValid(message);

        var invalid = AllThePatterns.newBuilder()
                .setLetters("12345")
                .buildPartial();
        assertNotValid(invalid);
    }

    @Test
    @DisplayName("validate with `multiline` modifier")
    void multiline() {
        var message = AllThePatterns.newBuilder()
                .setManyLines("text" + System.lineSeparator() + "more text")
                .buildPartial();
        assertValid(message);

        var invalid = AllThePatterns.newBuilder()
                .setManyLines("single line text")
                .buildPartial();
        assertNotValid(invalid);
    }

    @Test
    @DisplayName("validate with `partial` modifier")
    void partial() {
        var message = AllThePatterns.newBuilder()
                .setPartial("Hello World!")
                .buildPartial();
        assertValid(message);

        var invalid = AllThePatterns.newBuilder()
                .setPartial("123456")
                .buildPartial();
        assertNotValid(invalid);
    }

    @Test
    @DisplayName("validate with `unicode` modifier")
    void utf8() {
        var message = AllThePatterns.newBuilder()
                .setUtf8("ґ")
                .buildPartial();
        assertValid(message);

        var invalid = AllThePatterns.newBuilder()
                .setUtf8("\\\\")
                .buildPartial();
        assertNotValid(invalid);
    }

    @Test
    @DisplayName("validate with `dot_all` modifier")
    void dotAll() {
        var message = AllThePatterns.newBuilder()
                .setDotAll("ab" + System.lineSeparator() + "cd")
                .buildPartial();
        assertValid(message);
    }

    private static PatternStringFieldValue patternStringFor(String email) {
        return PatternStringFieldValue.newBuilder()
                .setEmail(email)
                .build();
    }
}
