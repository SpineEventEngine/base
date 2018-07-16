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

package io.spine.code.javadoc;

import com.google.common.testing.NullPointerTester;
import io.spine.code.javadoc.JavadocEscaper.EscapeSequence;
import org.junit.Test;

import static io.spine.code.javadoc.JavadocEscaper.escape;
import static org.junit.Assert.assertEquals;

/**
 * @author Dmytro Grankin
 */
public class JavadocEscaperShould {

    @Test
    public void escape_comment_beginning_and_ending() {
        assertEquals(EscapeSequence.COMMENT_ENDING.getEscaped(),
                     escape(EscapeSequence.COMMENT_ENDING.getUnescaped()));
        assertEquals(' ' + EscapeSequence.COMMENT_BEGINNING.getEscaped(),
                     escape(' ' + EscapeSequence.COMMENT_BEGINNING.getUnescaped()));
    }

    @Test
    public void escape_slash_in_beginning() {
        String remainingJavadoc = "ABC";
        assertEquals("&#47;" + remainingJavadoc, escape('/' + remainingJavadoc));
    }

    @Test
    public void escape_html() {
        assertEquals(EscapeSequence.LESS_THAN.getEscaped(),
                     escape(EscapeSequence.LESS_THAN.getUnescaped()));
        assertEquals(EscapeSequence.GREATER_THAN.getEscaped(),
                     escape(EscapeSequence.GREATER_THAN.getUnescaped()));
        assertEquals(EscapeSequence.AMPERSAND.getEscaped(),
                     escape(EscapeSequence.AMPERSAND.getUnescaped()));
    }

    @Test
    public void escape_at_and_back_slash() {
        assertEquals(EscapeSequence.AT_MARK.getEscaped(),
                     escape(EscapeSequence.AT_MARK.getUnescaped()));
        assertEquals(EscapeSequence.BACK_SLASH.getEscaped(),
                     escape(EscapeSequence.BACK_SLASH.getUnescaped()));
    }

    @Test
    public void pass_the_null_tolerance_check() {
        NullPointerTester nullPointerTester = new NullPointerTester();

        nullPointerTester.testAllPublicStaticMethods(JavadocEscaper.class);
        nullPointerTester.testAllPublicStaticMethods(EscapeSequence.class);
    }
}
