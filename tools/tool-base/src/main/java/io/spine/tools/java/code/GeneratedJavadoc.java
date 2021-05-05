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

package io.spine.tools.java.code;

import com.squareup.javapoet.CodeBlock;
import io.spine.tools.java.javadoc.JavadocText;

/**
 * A JavaPoet-based spec of a generated Javadoc text.
 */
public final class GeneratedJavadoc {

    private final CodeBlock spec;

    private GeneratedJavadoc(CodeBlock spec) {
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
     * GeneratedJavadoc.singleParagraph(
     *     CodeBlock.of("The Javadoc text.")
     * );
     * </pre>
     */
    public static GeneratedJavadoc singleParagraph(CodeBlock paragraph) {
        JavadocText paragraphText = JavadocText.fromEscaped(paragraph.toString())
                                               .withNewLine();
        CodeBlock value = CodeBlock
                .builder()
                .add(paragraphText.value())
                .build();
        return new GeneratedJavadoc(value);
    }

    /**
     * Creates a two-paragraph Javadoc.
     *
     * <p>The specified paragraphs should contain only plain Javadoc text, without any additional
     * formatting.
     *
     * <p>Example:
     * <pre>
     * GeneratedJavadoc.twoParagraph(
     *     CodeBlock.of("First paragraph text."),
     *     CodeBlock.of("Second paragraph text.")
     * );
     * </pre>
     */
    public static GeneratedJavadoc twoParagraph(CodeBlock firstParagraph,
                                                CodeBlock secondParagraph) {
        JavadocText firstParagraphText = JavadocText.fromEscaped(firstParagraph.toString())
                                                    .withNewLine()
                                                    .withNewLine();
        JavadocText secondParagraphText = JavadocText.fromEscaped(secondParagraph.toString())
                                                     .withPTag()
                                                     .withNewLine();
        CodeBlock value = CodeBlock
                .builder()
                .add(firstParagraphText.value())
                .add(secondParagraphText.value())
                .build();
        return new GeneratedJavadoc(value);
    }

    /**
     * Creates a three-paragraph Javadoc.
     *
     * <p>The specified paragraphs should contain only plain Javadoc text, without any additional
     * formatting.
     *
     * <p>Example:
     * <pre>
     * GeneratedJavadoc.threeParagraph(
     *     CodeBlock.of("First paragraph text."),
     *     CodeBlock.of("Second paragraph text."),
     *     CodeBlock.of("Third paragraph text.")
     * );
     * </pre>
     */
    public static GeneratedJavadoc threeParagraph(CodeBlock firstParagraph,
                                                  CodeBlock secondParagraph,
                                                  CodeBlock thirdParagraph) {
        JavadocText firstParagraphText = JavadocText.fromEscaped(firstParagraph.toString())
                                                    .withNewLine()
                                                    .withNewLine();
        JavadocText secondParagraphText = JavadocText.fromEscaped(secondParagraph.toString())
                                                     .withPTag()
                                                     .withNewLine()
                                                     .withNewLine();
        JavadocText thirdParagraphText = JavadocText.fromEscaped(thirdParagraph.toString())
                                                    .withPTag()
                                                    .withNewLine();
        CodeBlock value = CodeBlock
                .builder()
                .add(firstParagraphText.value())
                .add(secondParagraphText.value())
                .add(thirdParagraphText.value())
                .build();
        return new GeneratedJavadoc(value);
    }

    /**
     * Returns the generated Javadoc as JavaPoet {@code CodeBlock}.
     */
    public CodeBlock spec() {
        return spec;
    }
}
