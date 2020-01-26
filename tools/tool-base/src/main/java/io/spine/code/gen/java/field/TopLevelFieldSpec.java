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
import io.spine.base.Field;
import io.spine.code.proto.FieldDeclaration;
import io.spine.gen.SubscribableField;

final class TopLevelFieldSpec extends FieldSpec {

    TopLevelFieldSpec(FieldDeclaration field, Class<? extends SubscribableField> fieldSupertype) {
        super(field, fieldSupertype);
    }

    @Override
    CodeBlock returnNestedFieldsContainer() {
        return CodeBlock.of(
                "return new $T($T.named(\"$L\"))",
                returnType().value(), Field.class, fieldName().value()
        );
    }

    @Override
    CodeBlock returnSimpleField() {
        return CodeBlock.of(
                "return new $T($T.named(\"$L\"))",
                fieldSupertype(), Field.class, fieldName().value()
        );
    }
}
