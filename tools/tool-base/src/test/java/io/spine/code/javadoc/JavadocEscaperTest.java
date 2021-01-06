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

package io.spine.code.javadoc;

import com.google.common.testing.NullPointerTester;
import io.spine.code.javadoc.JavadocEscaper.EscapeSequence;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.code.javadoc.JavadocEscaper.escape;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("JavadocEscaper utility class should")
class JavadocEscaperTest extends UtilityClassTest<JavadocEscaper> {

    JavadocEscaperTest() {
        super(JavadocEscaper.class);
    }

    @Test
    @DisplayName("escape comment beginning and ending")
    void escape_comment_beginning_and_ending() {
        assertEquals(EscapeSequence.COMMENT_ENDING.getEscaped(),
                     escape(EscapeSequence.COMMENT_ENDING.getUnescaped()));
        assertEquals(' ' + EscapeSequence.COMMENT_BEGINNING.getEscaped(),
                     escape(' ' + EscapeSequence.COMMENT_BEGINNING.getUnescaped()));
    }

    @Test
    @DisplayName("escape slash in beginning")
    void escape_slash_in_beginning() {
        String remainingJavadoc = "ABC";
        assertEquals("&#47;" + remainingJavadoc, escape('/' + remainingJavadoc));
    }

    @Test
    @DisplayName("escape HTML")
    void escape_html() {
        assertEquals(EscapeSequence.LESS_THAN.getEscaped(),
                     escape(EscapeSequence.LESS_THAN.getUnescaped()));
        assertEquals(EscapeSequence.GREATER_THAN.getEscaped(),
                     escape(EscapeSequence.GREATER_THAN.getUnescaped()));
        assertEquals(EscapeSequence.AMPERSAND.getEscaped(),
                     escape(EscapeSequence.AMPERSAND.getUnescaped()));
    }

    @Test
    @DisplayName("escape @ and back slash")
    void escape_at_and_back_slash() {
        assertEquals(EscapeSequence.AT_MARK.getEscaped(),
                     escape(EscapeSequence.AT_MARK.getUnescaped()));
        assertEquals(EscapeSequence.BACK_SLASH.getEscaped(),
                     escape(EscapeSequence.BACK_SLASH.getUnescaped()));
    }

    @Override
    protected void configure(NullPointerTester tester) {
        super.configure(tester);
        tester.testAllPublicStaticMethods(EscapeSequence.class);
    }
}
