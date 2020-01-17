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

package io.spine.code.javadoc;

import io.spine.value.StringTypeValue;

/**
 * A text of a Javadoc.
 */
public class JavadocText extends StringTypeValue {

    private static final String OPENING_PRE = "<pre>";
    private static final String CLOSING_PRE = "</pre>";
    private static final String P_TAG = "<p>";

    private static final long serialVersionUID = 0L;

    /*
      TODO:2017-03-24:dmytro.grankin: Replace hardcoded line separator by system-independent
      after https://github.com/square/javapoet/issues/552 is fixed.
    */
    @SuppressWarnings("HardcodedLineSeparator")
    private static final String LINE_SEPARATOR = "\n";

    private JavadocText(String escapedText) {
        super(escapedText);
    }

    public static JavadocText fromEscaped(String escapedText) {
        return new JavadocText(escapedText);
    }

    public static JavadocText fromUnescaped(String unescapedText) {
        return new JavadocText(JavadocEscaper.escape(unescapedText));
    }

    /**
     * Wraps the text in {@code <pre>} tags.
     *
     * @return the text wrapped in the tags
     */
    public JavadocText inPreTags() {
        String inTags = new StringBuilder(OPENING_PRE)
                .append(LINE_SEPARATOR)
                .append(JavadocEscaper.escape(value()))
                .append(CLOSING_PRE)
                .append(LINE_SEPARATOR)
                .toString();
        return new JavadocText(inTags);
    }

    public JavadocText withNewLine() {
        return new JavadocText(value() + LINE_SEPARATOR);
    }

    public JavadocText withPTag() {
        return new JavadocText(P_TAG + value());
    }
}
