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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.spine.base.query.EntityQuery;
import io.spine.code.gen.java.GeneratedJavadoc;
import io.spine.code.gen.java.GeneratedMethodSpec;
import io.spine.type.MessageType;

import static io.spine.code.gen.java.Annotations.generatedBySpineModelCompiler;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Assembles the specification for the {@code Query} class generated for entities state types.
 *
 * <p>Additionally, generates the {@code newQuery()} method, which would be a static member
 * of the Java class of the entity state.
 */
public final class EntityQuerySpec extends AbstractEntityQuerySpec implements GeneratedMethodSpec {

    public EntityQuerySpec(MessageType type) {
        super(type);
    }

    @Override
    public TypeSpec typeSpec() {
        TypeSpec result = TypeSpec
                .classBuilder(queryType().className())
                .superclass(entityQuery())
                .addMethod(constructor())
                .addAnnotation(generatedBySpineModelCompiler())
                .addModifiers(PUBLIC, STATIC, FINAL)
                .build();
        return result;
    }

    @SuppressWarnings("DuplicateStringLiteralInspection")
    private static MethodSpec constructor() {
        String paramName = "builder";
        return MethodSpec
                .constructorBuilder()
                .addModifiers(PROTECTED)
                .addParameter(queryBuilderType().value(), paramName)
                .addStatement("super($L)", paramName)
                .build();
    }

    private ParameterizedTypeName entityQuery() {
        return ParameterizedTypeName.get(
                ClassName.get(EntityQuery.class),
                idFieldType(), stateType(), queryBuilderType().value()
        );
    }

    /**
     * Generates {@code newQuery()} method.
     */
    @Override
    public MethodSpec methodSpec() {
        TypeName typeOfBuilder = queryBuilderType().value();
        return MethodSpec
                .methodBuilder("newQuery")
                .addJavadoc(newQueryJavadoc().spec())
                .addModifiers(PUBLIC, STATIC)
                .addStatement("return new $T()", typeOfBuilder)
                .returns(typeOfBuilder)
                .build();
    }

    /**
     * Returns the Javadoc for {@code newQuery()} method.
     */
    private static GeneratedJavadoc newQueryJavadoc() {
        return GeneratedJavadoc.singleParagraph(
                CodeBlock.of("Creates a new instance of {@link $L}.",
                             queryBuilderType().className()
                                               .simpleName())
        );
    }
}
