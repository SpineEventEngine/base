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

package io.spine.tools.java.gen.query;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import io.spine.tools.java.gen.GeneratedJavadoc;
import io.spine.tools.java.gen.GeneratedMethodSpec;
import io.spine.tools.java.gen.JavaPoetName;
import io.spine.code.proto.EntityIdField;
import io.spine.code.proto.FieldName;
import io.spine.query.IdCriterion;

import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * Generates the method which allows to restrict querying to certain entity identifiers.
 */
public class IdColumnSpec implements GeneratedMethodSpec {

    private final EntityIdField idField;
    private final TypeName queryBuilderName;
    private final TypeName idType;

    /**
     * Creates the specification for the passed declaration of entity ID field and the type name
     * of the query builder, in scope of which the created method spec exists.
     */
    IdColumnSpec(EntityIdField field, TypeName queryBuilderName) {
        this.idField = field;
        this.queryBuilderName = queryBuilderName;
        this.idType = JavaPoetName.of(idField.declaration())
                                  .value()
                                  .box();
    }

    @Override
    public MethodSpec methodSpec() {
        FieldName name = idName();
        MethodSpec result = MethodSpec
                .methodBuilder(name.javaCase())
                .addJavadoc(javadoc().spec())
                .addModifiers(PUBLIC)
                .returns(idCriterion())
                .addStatement(methodBody())
                .build();
        return result;
    }

    /**
     * Returns the Javadoc for the generated method which would produce the {@link IdCriterion}.
     */
    private static GeneratedJavadoc javadoc() {
        return GeneratedJavadoc.singleParagraph(
                CodeBlock.of("Creates a criterion for the identifier of this entity."));
    }

    /**
     * Returns the name of the Java type of a column.
     */
    private ParameterizedTypeName idCriterion() {
        JavaPoetName result = JavaPoetName.of(IdCriterion.class);
        ParameterizedTypeName parameterizedResult =
                ParameterizedTypeName.get(result.className(), idType, queryBuilderName);
        return parameterizedResult;
    }

    /**
     * Returns the method body which instantiates the {@link IdCriterion}.
     */
    private static CodeBlock methodBody() {
        return CodeBlock.of(
                "return new $T<>(this)",
                IdCriterion.class
        );
    }

    /**
     * Returns the name of the ID field.
     */
    private FieldName idName() {
        return idField.name();
    }
}
