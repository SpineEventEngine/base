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

package io.spine.tools.java.javadoc;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.java.javadoc.JavadocEscaper.EscapeSequence.fromBeginningOf;

/**
 * Escaper for a Javadoc text.
 *
 * <p>If a Javadoc text received from a source, that has no knowledge of
 * the Javadoc text restrictions, then such text should be escaped using {@link #escape(String)}.
 *
 * <p>Example of such source is a comment from ".proto" file. In particular,
 * a person that writes a comment in ".proto" file does not know,
 * that {@literal "<"} and {@literal ">"} should not appear in Javadoc.
 */
public final class JavadocEscaper {

    /** Prevents instantiation of this utility class. */
    private JavadocEscaper() {
    }

    /**
     * Escapes the {@link EscapeSequence} from a Javadoc text.
     *
     * <p>If a Javadoc text starts with a slash, it will be interpreted like a comment ending.
     * To handle this case, we should add "*" before a Javadoc text.
     *
     * @param javadocText the unescaped Javadoc text
     * @return the escaped Javadoc text
     */
    public static String escape(String javadocText) {
        checkNotNull(javadocText);
        StringBuilder escapedJavadocBuilder = new StringBuilder(javadocText.length() * 2);

        StringBuilder unescapedPartBuilder = new StringBuilder('*' + javadocText);
        while (unescapedPartBuilder.length() != 0) {
            EscapeSequence escapedString = fromBeginningOf(unescapedPartBuilder.toString());

            if (escapedString != null) {
                escapedJavadocBuilder.append(escapedString.getEscaped());
                unescapedPartBuilder.delete(0, escapedString.getUnescaped()
                                                            .length());
            } else {
                escapedJavadocBuilder.append(unescapedPartBuilder.charAt(0));
                unescapedPartBuilder.deleteCharAt(0);
            }
        }

        // Remove added "*" in the beginning.
        return escapedJavadocBuilder.toString()
                                    .substring(1);
    }

    /**
     * Enumeration of the sequences, that should be escaped in a Javadoc text.
     */
    public enum EscapeSequence {
        COMMENT_BEGINNING("/*", "/&#42;"),
        COMMENT_ENDING("*/", "*&#47;"),
        BACK_SLASH("\\", "&#92;"),
        AT_MARK("@", "&#64;"),
        AMPERSAND("&", "&amp;"),
        LESS_THAN("<", "&lt;"),
        GREATER_THAN(">", "&gt;");

        /**
         * A sequence that should be escaped in a Javadoc text.
         */
        private final String unescaped;

        /**
         * The escaped string for the unescaped string.
         */
        private final String escaped;

        EscapeSequence(String unescaped, String escaped) {
            this.unescaped = unescaped;
            this.escaped = escaped;
        }

        /**
         * Returns an {@code EscapeSequence} element if the beginning of the Javadoc
         * text starts with an unescaped version of one of {@code EscapeSequence}.
         *
         * @param javadocText the Javadoc text
         * @return the {@code EscapeSequence} element
         */
        public static EscapeSequence fromBeginningOf(String javadocText) {
            checkNotNull(javadocText);

            for (EscapeSequence escapedCharacter : values()) {
                if (javadocText.startsWith(escapedCharacter.unescaped)) {
                    return escapedCharacter;
                }
            }

            return null;
        }

        public String getEscaped() {
            return escaped;
        }

        public String getUnescaped() {
            return unescaped;
        }
    }
}
