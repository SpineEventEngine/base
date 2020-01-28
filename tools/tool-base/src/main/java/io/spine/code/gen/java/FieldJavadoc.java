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

package io.spine.code.gen.java;

import com.squareup.javapoet.CodeBlock;
import io.spine.annotation.Internal;
import io.spine.code.javadoc.JavadocText;
import io.spine.code.proto.FieldDeclaration;

/**
 * The Javadoc of a method which returns a strongly-typed proto field.
 *
 * @see io.spine.base.SubscribableField
 * @see io.spine.base.EntityColumn
 */
@Internal
public final class FieldJavadoc implements GeneratedJavadoc {

    /**
     * The field which is returned.
     */
    private final FieldDeclaration field;

    /**
     * The alias which allows to call the field some other name in doc, e.g. "column".
     */
    private final String fieldAlias;

    public FieldJavadoc(FieldDeclaration field, String fieldAlias) {
        this.field = field;
        this.fieldAlias = fieldAlias;
    }

    @Override
    public CodeBlock spec() {
        CodeBlock firstParagraphText = CodeBlock
                .builder()
                .add("Returns the $L\"$L\" $L.", fieldKind(), field.name(), fieldAlias)
                .build();
        JavadocText firstParagraph = JavadocText.fromEscaped(firstParagraphText.toString())
                                                .withNewLine()
                                                .withNewLine();
        CodeBlock secondParagraphText = CodeBlock
                .builder()
                .add("The $L type is {@code $L}.", elementDescribedByType(), field.javaTypeName())
                .build();
        JavadocText secondParagraph = JavadocText.fromEscaped(secondParagraphText.toString())
                                                 .withPTag()
                                                 .withNewLine();
        CodeBlock value = CodeBlock
                .builder()
                .add(firstParagraph.value())
                .add(secondParagraph.value())
                .build();
        return value;
    }

    private String fieldKind() {
        if (field.isRepeated()) {
            return "{@code repeated} ";
        }
        if (field.isMap()) {
            return "{@code map} ";
        }
        return "";
    }

    @SuppressWarnings("DuplicateStringLiteralInspection") // Random duplication.
    private String elementDescribedByType() {
        if (field.isRepeated()) {
            return "element";
        }
        if (field.isMap()) {
            return "value";
        }
        return fieldAlias;
    }
}
