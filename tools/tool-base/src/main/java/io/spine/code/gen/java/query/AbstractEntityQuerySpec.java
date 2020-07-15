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

package io.spine.code.gen.java.query;

import com.squareup.javapoet.TypeName;
import io.spine.code.gen.java.GeneratedTypeSpec;
import io.spine.code.gen.java.JavaPoetName;
import io.spine.code.java.PackageName;
import io.spine.code.java.SimpleClassName;
import io.spine.code.proto.EntityIdField;
import io.spine.type.MessageType;

/**
 * Abstract base for code specifications related to generation of entity queries.
 */
abstract class AbstractEntityQuerySpec implements GeneratedTypeSpec {

    private static final JavaPoetName queryBuilderType =
            JavaPoetName.of(SimpleClassName.create("QueryBuilder"));

    @SuppressWarnings("DuplicateStringLiteralInspection")   // irrelevant coincidence.
    private static final JavaPoetName queryType =
            JavaPoetName.of(SimpleClassName.create("Query"));


    private final MessageType messageType;
    private final EntityIdField idField;

    AbstractEntityQuerySpec(MessageType type) {
        this.messageType = type;
        this.idField = EntityIdField.of(type);
    }

    @Override
    public PackageName packageName() {
        return messageType.javaPackage();
    }

    final EntityIdField idField() {
        return idField;
    }

    /**
     * Returns the {@code TypeName} of the identifier type.
     *
     * <p>If the type is a primitive, it is boxed.
     */
    final TypeName idFieldType() {
        return JavaPoetName.of(idField.declaration()).value().box();
    }

    final TypeName stateType() {
        return JavaPoetName.of(messageType).value();
    }

    static JavaPoetName queryBuilderType() {
        return queryBuilderType;
    }

    static JavaPoetName queryType() {
        return queryType;
    }
}
