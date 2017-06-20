/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

package io.spine.gradle.compiler.javadoc;

import com.google.common.testing.NullPointerTester;
import org.junit.Test;

import static io.spine.gradle.compiler.javadoc.JavadocEscaper.EscapeSequence.AMPERSAND;
import static io.spine.gradle.compiler.javadoc.JavadocEscaper.EscapeSequence.AT_MARK;
import static io.spine.gradle.compiler.javadoc.JavadocEscaper.EscapeSequence.BACK_SLASH;
import static io.spine.gradle.compiler.javadoc.JavadocEscaper.EscapeSequence.COMMENT_BEGINNING;
import static io.spine.gradle.compiler.javadoc.JavadocEscaper.EscapeSequence.COMMENT_ENDING;
import static io.spine.gradle.compiler.javadoc.JavadocEscaper.EscapeSequence.GREATER_THAN;
import static io.spine.gradle.compiler.javadoc.JavadocEscaper.EscapeSequence.LESS_THAN;
import static io.spine.gradle.compiler.javadoc.JavadocEscaper.escape;
import static org.junit.Assert.assertEquals;

/**
 * @author Dmytro Grankin
 */
public class JavadocEscaperShould {

    @Test
    public void escape_comment_beginning_and_ending() {
        assertEquals(COMMENT_ENDING.getEscaped(), escape(COMMENT_ENDING.getUnescaped()));
        assertEquals(' ' + COMMENT_BEGINNING.getEscaped(),
                     escape(' ' + COMMENT_BEGINNING.getUnescaped()));
    }

    @Test
    public void escape_slash_in_beginning() {
        final String remainingJavadoc = "ABC";
        assertEquals("&#47;" + remainingJavadoc, escape('/' + remainingJavadoc));
    }

    @Test
    public void escape_html() {
        assertEquals(LESS_THAN.getEscaped(), escape(LESS_THAN.getUnescaped()));
        assertEquals(GREATER_THAN.getEscaped(), escape(GREATER_THAN.getUnescaped()));
        assertEquals(AMPERSAND.getEscaped(), escape(AMPERSAND.getUnescaped()));
    }

    @Test
    public void escape_at_and_back_slash() {
        assertEquals(AT_MARK.getEscaped(), escape(AT_MARK.getUnescaped()));
        assertEquals(BACK_SLASH.getEscaped(), escape(BACK_SLASH.getUnescaped()));
    }

    @Test
    public void pass_the_null_tolerance_check() {
        final NullPointerTester nullPointerTester = new NullPointerTester();

        nullPointerTester.testAllPublicStaticMethods(JavadocEscaper.class);
        nullPointerTester.testAllPublicStaticMethods(JavadocEscaper.EscapeSequence.class);
    }
}
