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

package io.spine.code.gen.java.query;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.spine.code.gen.java.BuilderSpec;
import io.spine.code.gen.java.GeneratedBy;
import io.spine.code.gen.java.GeneratedJavadoc;
import io.spine.code.proto.FieldDeclaration;
import io.spine.query.EntityQuery;
import io.spine.query.EntityQueryBuilder;
import io.spine.query.IdCriterion;
import io.spine.type.MessageType;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.spine.code.proto.ColumnOption.columnsOf;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Assembles the specification for the {@code QueryBuilder} class generated for entity state types.
 */
public final class EntityQueryBuilderSpec extends AbstractEntityQuerySpec implements BuilderSpec {

    private final ImmutableList<FieldDeclaration> columns;

    public EntityQueryBuilderSpec(MessageType type) {
        super(type);
        this.columns = columnsOf(type);
    }

    @Override
    public TypeSpec toPoet() {
        TypeSpec result = TypeSpec
                .classBuilder(queryBuilderType().className())
                .superclass(entityQueryBuilder())
                .addAnnotation(GeneratedBy.spineModelCompiler())
                .addModifiers(PUBLIC, STATIC, FINAL)
                .addMethod(ctor())
                .addMethod(id())
                .addMethods(columns())
                .addMethod(thisRef())
                .addMethod(build())
                .build();
        return result;
    }

    /**
     * Returns the type name of the superclass, i.e. {@link EntityQueryBuilder}
     * with the generic parameters filled in.
     */
    private ParameterizedTypeName entityQueryBuilder() {
        return ParameterizedTypeName.get(
                ClassName.get(EntityQueryBuilder.class),
                idFieldType(), stateType(), typeOfSelf(), queryType().value()
        );
    }

    /**
     * Generates {@code build()} method.
     */
    @SuppressWarnings("DuplicateStringLiteralInspection")
    private static MethodSpec build() {
        TypeName typeOfQuery = queryType().value();
        return MethodSpec
                .methodBuilder(BUILD_METHOD_NAME)
                .addJavadoc(buildJavadoc().spec())
                .addAnnotation(Override.class)
                .addModifiers(PUBLIC)
                .addStatement("return new $T(this)", typeOfQuery)
                .returns(typeOfQuery)
                .build();
    }

    /**
     * Returns the Javadoc for {@code build()} method.
     */
    private static GeneratedJavadoc buildJavadoc() {
        return GeneratedJavadoc.singleParagraph(
                CodeBlock.of("Creates a new instance of {@link $L} on top of this {@code $L}.",
                             queryType().className()
                                        .simpleName(),
                             queryBuilderType().className()
                                               .simpleName())
        );
    }

    /**
     * Generates the constructor for this query builder.
     */
    private MethodSpec ctor() {
        return MethodSpec
                .constructorBuilder()
                .addJavadoc("Creates an instance of the builder.")
                .addModifiers(PRIVATE)
                .addStatement("super($L.class, $L.class)", idFieldType(), stateType())
                .build();
    }

    /**
     * Generates the method returning the {@link IdCriterion IdCriterion}
     * for this query builder.
     */
    private MethodSpec id() {
        return new IdColumnSpec(idField(), typeOfSelf()).methodSpec();
    }

    /**
     * Generates the methods which allow to specify restrictions put on the entity columns
     * to use in the {@link EntityQuery EntityQuery}.
     */
    private ImmutableList<MethodSpec> columns() {
        ImmutableList<MethodSpec> result =
                columns.stream()
                       .map((c) -> new QueryColumnSpec(c, typeOfSelf()))
                       .map(QueryColumnSpec::methodSpec)
                       .collect(toImmutableList());
        return result;
    }

    /**
     * Generates {@code thisRef()} method.
     */
    private static MethodSpec thisRef() {
        return MethodSpec
                .methodBuilder("thisRef")
                .addAnnotation(Override.class)
                .addModifiers(PROTECTED)
                .addStatement(RETURN_STATEMENT)
                .returns(queryBuilderType().value())
                .build();
    }

    /**
     * Returns the type name of the generated builder.
     */
    private static TypeName typeOfSelf() {
        return queryBuilderType().value();
    }
}
