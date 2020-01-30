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

package io.spine.code.gen.java.column;

import com.squareup.javapoet.CodeBlock;
import io.spine.code.gen.java.TwoParagraphDoc;
import io.spine.code.proto.FieldDeclaration;

/**
 * The Javadoc of a method which returns a strongly-typed entity column.
 *
 * @see ColumnSpec
 */
final class ColumnDoc extends TwoParagraphDoc {

    /**
     * The field which is returned.
     */
    private final FieldDeclaration column;

    ColumnDoc(FieldDeclaration column) {
        super();
        this.column = column;
    }

    @Override
    protected void addFirstParagraph(CodeBlock.Builder text) {
        text.add("Returns the $S column.", column.name());
    }

    @Override
    protected void addSecondParagraph(CodeBlock.Builder text) {
        text.add("The column Java type is {@code $L}.", column.javaTypeName());
    }
}
