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

import com.google.gson.internal.Primitives;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import io.spine.base.entity.EntityColumn;
import io.spine.base.entity.QueryCriterion;
import io.spine.code.gen.java.GeneratedJavadoc;
import io.spine.code.gen.java.GeneratedMethodSpec;
import io.spine.code.gen.java.JavaPoetName;
import io.spine.code.proto.FieldDeclaration;
import io.spine.code.proto.FieldName;
import io.spine.code.proto.ScalarType;

import java.util.Optional;

import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * Generates setters for {@link io.spine.base.entity.EntityQueryBuilder EntityQueryBuilder}
 * of the Protobuf message representing the {@linkplain io.spine.base.entity.EntityState
 * entity state}.
 */
final class EntityColumnSpec implements GeneratedMethodSpec {

    private final FieldDeclaration column;
    private final TypeName queryBuilderName;
    private final ClassName entityStateName;
    private final ClassName returningValueName;

    EntityColumnSpec(FieldDeclaration column, TypeName queryBuilderName) {
        this.column = column;
        this.queryBuilderName = queryBuilderName;
        this.entityStateName = JavaPoetName.of(column.declaringType().javaClassName()).className();
        this.returningValueName = returningValue(column);
    }

    @Override
    public MethodSpec methodSpec() {
        FieldName name = columnName();
        MethodSpec result = MethodSpec
                .methodBuilder(name.javaCase())
                .addJavadoc(javadoc().spec())
                .addModifiers(PUBLIC)
                .returns(queryCriterion())
                .addStatement(methodBody())
                .build();
        return result;
    }

    /**
     * Returns the column name as defined in Protobuf.
     */
    private FieldName columnName() {
        return column.name();
    }

    /**
     * Returns the name of the Java type of a column.
     */
    private ParameterizedTypeName queryCriterion() {
        JavaPoetName result = JavaPoetName.of(QueryCriterion.class);
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
                "return new $T<>(Column.$S(), this)",
                QueryCriterion.class,
                columnName()
        );
    }

    /**
     * Returns the type of the column value in a form suitable for the code generation.
     *
     * <p>If the type of the column value is a primitive type, its wrapper is used instead.
     */
    private static ClassName returningValue(FieldDeclaration column) {
        String rawTypeName = column.javaTypeName();
        Optional<ScalarType> maybeScalar = ScalarType.of(column.descriptor()
                                                               .toProto());
        io.spine.code.java.ClassName className;
        if (maybeScalar.isPresent()) {
            ScalarType scalar = maybeScalar.get();
            Class<?> javaType = scalar.javaClass();
            if (javaType.isPrimitive()) {
                Class<?> wrapper = Primitives.wrap(javaType);
                className = io.spine.code.java.ClassName.of(wrapper);
            } else {
                className = io.spine.code.java.ClassName.of(javaType);
            }
        } else {
            className = io.spine.code.java.ClassName.of(rawTypeName);
        }
        String packageName = className.packageName()
                                      .value();
        ClassName result = ClassName.get(packageName, className.withoutPackage());
        return result;
    }

    /**
     * Returns the method Javadoc.
     */
    private GeneratedJavadoc javadoc() {
        return GeneratedJavadoc.singleParagraph(
                CodeBlock.of("Creates a criterion for the {@link Column#$S() Column.$S()} column.",
                             column.name())
        );
    }
}
