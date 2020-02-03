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

package io.spine.code.gen.java.field;

import com.squareup.javapoet.CodeBlock;
import io.spine.code.gen.java.TwoParagraphDoc;
import io.spine.code.proto.FieldDeclaration;

/**
 * The Javadoc of a method which returns a strongly-typed proto field.
 *
 * @see FieldSpec
 */
final class FieldDoc extends TwoParagraphDoc {

    /**
     * The field which is returned.
     */
    private final FieldDeclaration field;

    FieldDoc(FieldDeclaration field) {
        super();
        this.field = field;
    }

    @Override
    protected void addFirstParagraph(CodeBlock.Builder text) {
        text.add("Returns the $L$S field.", fieldKind(), field.name());
    }

    @Override
    protected void addSecondParagraph(CodeBlock.Builder text) {
        text.add("The $L Java type is {@code $L}.", elementDescribedByType(), field.javaTypeName());
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
        return "field";
    }
}
