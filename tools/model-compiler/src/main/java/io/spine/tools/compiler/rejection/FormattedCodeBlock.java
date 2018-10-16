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

package io.spine.tools.compiler.rejection;

import com.squareup.javapoet.CodeBlock;
import io.spine.code.javadoc.JavadocEscaper;

/**
 * A wrapper around {@link CodeBlock}, which facilitates formatting.
 */
class FormattedCodeBlock {

    private static final String OPENING_PRE = "<pre>";
    private static final String CLOSING_PRE = "</pre>";
    /*
      TODO:2017-03-24:dmytro.grankin: Replace hardcoded line separator by system-independent
      after https://github.com/square/javapoet/issues/552 is fixed.
    */
    @SuppressWarnings("HardcodedLineSeparator")
    private static final String LINE_SEPARATOR = "\n";

    private final CodeBlock codeBlock;

    private FormattedCodeBlock(CodeBlock codeBlock) {
        this.codeBlock = codeBlock;
    }

    static FormattedCodeBlock from(String text) {
        return new FormattedCodeBlock(CodeBlock.of(text));
    }

    /**
     * Wraps the code block in {@code <pre>} tags and escapes prohibited Javadoc symbols.
     *
     * @return the code block representing a valid Javadoc
     */
    CodeBlock asJavadoc() {
        return CodeBlock.builder()
                        .add(OPENING_PRE)
                        .add(lineSeparator())
                        .add(JavadocEscaper.escape(codeBlock.toString()))
                        .add(CLOSING_PRE)
                        .add(lineSeparator())
                        .build();
    }

    static String lineSeparator() {
        return LINE_SEPARATOR;
    }
}
