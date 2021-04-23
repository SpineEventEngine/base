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

package io.spine.tools.java.gen;

import com.squareup.javapoet.CodeBlock;
import io.spine.tools.java.javadoc.JavadocText;

import static io.spine.tools.java.javadoc.JavadocText.fromEscaped;

/**
 * A JavaPoet-based spec of a generated Javadoc text.
 */
public final class JavadocBlock {

    private final CodeBlock spec;

    private JavadocBlock(CodeBlock spec) {
        this.spec = spec;
    }

    /**
     * Creates a single paragraph Javadoc.
     *
     * <p>The specified {@code paragraph} should contain only plain Javadoc text, without any
     * additional formatting.
     *
     * <p>Example:
     * <pre>
     * JavadocBlock.singleParagraph(
     *     p("The Javadoc text.")
     * );
     * </pre>
     *
     * @see #p(String, Object...)
     */
    public static JavadocBlock singleParagraph(CodeBlock paragraph) {
        JavadocText text = fromEscaped(paragraph.toString())
                .withNewLine();
        CodeBlock value = CodeBlock
                .builder()
                .add(text.value())
                .build();
        return new JavadocBlock(value);
    }

    /**
     * Creates a two-paragraph Javadoc.
     *
     * <p>The specified paragraphs should contain only plain Javadoc text, without any additional
     * formatting.
     *
     * <p>Example:
     * <pre>
     * JavadocBlock.twoParagraph(
     *     p("First paragraph text."),
     *     p("Second paragraph text.")
     * );
     * </pre>
     *
     * @see #p(String, Object...)
     */
    public static JavadocBlock twoParagraph(CodeBlock p1, CodeBlock p2) {
        JavadocText firstParagraphText =
                fromEscaped(p1.toString())
                           .withNewLine()
                           .withNewLine();
        JavadocText secondParagraphText =
                fromEscaped(p2.toString())
                           .withPTag()
                           .withNewLine();
        CodeBlock value = CodeBlock.builder()
                .add(firstParagraphText.value())
                .add(secondParagraphText.value())
                .build();
        return new JavadocBlock(value);
    }

    /**
     * Creates a three-paragraph Javadoc.
     *
     * <p>The specified paragraphs should contain only plain Javadoc text, without any additional
     * formatting.
     *
     * <p>Example:
     * <pre>
     * JavadocBlock.threeParagraph(
     *     p("First paragraph text."),
     *     p("Second paragraph text."),
     *     p("Third paragraph text.")
     * );
     * </pre>
     *
     * @see #p(String, Object...)
     */
    public static JavadocBlock threeParagraph(CodeBlock p1, CodeBlock p2, CodeBlock p3) {
        JavadocText t1 = fromEscaped(p1.toString())
                .withNewLine()
                .withNewLine();
        JavadocText t2 = fromEscaped(p2.toString())
                .withPTag()
                .withNewLine()
                .withNewLine();
        JavadocText t3 = fromEscaped(p3.toString())
                .withPTag()
                .withNewLine();
        CodeBlock value = CodeBlock.builder()
                .add(t1.value())
                .add(t2.value())
                .add(t3.value())
                .build();
        return new JavadocBlock(value);
    }

    /**
     * A shortcut method to create one code block with formatted text.
     *
     * @see CodeBlock#of(String, Object...)
     */
    public static CodeBlock p(String format, Object... args) {
        return CodeBlock.of(format, args);
    }

    /**
     * Returns the Javadoc specification as {@code CodeBlock}.
     */
    public CodeBlock spec() {
        return spec;
    }
}
