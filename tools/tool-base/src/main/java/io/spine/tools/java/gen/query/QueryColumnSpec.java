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
import io.spine.code.proto.FieldDeclaration;
import io.spine.query.EntityColumn;
import io.spine.query.EntityCriterion;
import io.spine.query.EntityQueryBuilder;

import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * Generates the method which produces a column criterion for {@link EntityQueryBuilder
 * EntityQueryBuilder} to restrict the value of the column to some parameter.
 */
final class QueryColumnSpec implements GeneratedMethodSpec {

    private final FieldDeclaration column;
    private final TypeName queryBuilderName;
    private final TypeName entityStateName;
    private final TypeName returningValueName;

    /**
     * Creates a new method specification which serves to generated a criterion method
     * for the passed column in scope of a query builder with the passed type name.
     *
     * @param column
     *         the name of the column, for which the criterion generation is needed
     * @param queryBuilderName
     *         the type name of the query builder in scope of which the generation is performed
     */
    QueryColumnSpec(FieldDeclaration column, TypeName queryBuilderName) {
        this.column = column;
        this.queryBuilderName = queryBuilderName;
        this.entityStateName = JavaPoetName.of(column.declaringType())
                                           .value();
        this.returningValueName = JavaPoetName.of(column)
                                              .value()
                                              .box();
    }

    @Override
    public MethodSpec methodSpec() {
        MethodSpec result = MethodSpec
                .methodBuilder(columnName())
                .addJavadoc(javadoc().spec())
                .addModifiers(PUBLIC)
                .returns(queryCriterion())
                .addStatement(methodBody())
                .build();
        return result;
    }

    /**
     * Returns the column name as it looks in the generated Java code.
     */
    private String columnName() {
        return column.name()
                     .javaCase();
    }

    /**
     * Returns the method Javadoc.
     */
    private GeneratedJavadoc javadoc() {
        String columnName = column.name()
                                  .javaCase();
        return GeneratedJavadoc.singleParagraph(
                CodeBlock.of("Creates a criterion for the {@link Column#$L() Column.$L()} column.",
                             columnName, columnName)
        );
    }

    /**
     * Returns the name of the Java type of a column.
     */
    private ParameterizedTypeName queryCriterion() {
        JavaPoetName result = JavaPoetName.of(EntityCriterion.class);
        ParameterizedTypeName parameterizedResult =
                ParameterizedTypeName.get(result.className(),
                                          entityStateName, returningValueName, queryBuilderName);
        return parameterizedResult;
    }

    /**
     * Returns the method body which instantiates the {@link EntityColumn}.
     */
    private CodeBlock methodBody() {
        return CodeBlock.of(
                "return new $T<>(Column.$L(), this)",
                EntityCriterion.class,
                columnName()
        );
    }
}
