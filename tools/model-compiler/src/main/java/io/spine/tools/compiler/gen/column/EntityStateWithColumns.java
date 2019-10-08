/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.tools.compiler.gen.column;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.spine.base.EntityWithColumns;
import io.spine.code.java.PackageName;
import io.spine.code.proto.FieldDeclaration;
import io.spine.code.proto.FieldName;
import io.spine.tools.compiler.gen.GeneratedTypeSpec;
import io.spine.tools.compiler.gen.JavaPoetName;
import io.spine.type.MessageType;

import static io.spine.code.proto.ColumnOption.columnsOf;
import static io.spine.tools.compiler.annotation.Annotations.generatedBySpineModelCompiler;
import static javax.lang.model.element.Modifier.PUBLIC;

public final class EntityStateWithColumns implements GeneratedTypeSpec {

    private final MessageType messageType;
    private final ImmutableList<FieldDeclaration> columns;

    public EntityStateWithColumns(MessageType messageType) {
        this.messageType = messageType;
        this.columns = columnsOf(messageType);
    }

    @Override
    public PackageName packageName() {
        return messageType.javaPackage();
    }

    @Override
    public TypeSpec typeSpec() {
        TypeSpec.Builder builder =
                TypeSpec.interfaceBuilder(className())
                        .addJavadoc(classJavadoc())
                        .addAnnotation(generatedBySpineModelCompiler())
                        .addModifiers(PUBLIC)
                        .superclass(EntityWithColumns.class);
        addColumns(builder);
        return builder.build();
    }

    private void addColumns(TypeSpec.Builder spec) {
        columns.forEach(column -> addColumn(spec, column));
    }

    private static void addColumn(TypeSpec.Builder spec, FieldDeclaration fieldDeclaration) {
        MethodSpec getter = toGetter(fieldDeclaration);
        spec.addMethod(getter);
    }

    private static MethodSpec toGetter(FieldDeclaration declaration) {
        FieldName fieldName = declaration.name();
        String methodName = "get" + fieldName.toCamelCase();
        JavaPoetName fieldType = JavaPoetName.of(declaration.javaTypeName());
        TypeName returnType = fieldType.value();
        MethodSpec result = MethodSpec.methodBuilder(methodName)
                                      .returns(returnType)
                                      .build();
        return result;
    }

    /**
     * A Javadoc content for the rejection.
     *
     * @return the class-level Javadoc content
     */
    private CodeBlock classJavadoc() {
        // TODO:2019-10-08:dmytro.kuzmin:WIP Improve class-level doc.
        CodeBlock value = CodeBlock
                .builder()
                .add("Entity Columns of proto type ")
                .add("{@code $L.$L}.", packageName(), messageType.simpleJavaClassName())
                .build();
        return value;
    }

    private String className() {
        String value = messageType.javaClassName()
                                  .value();
        return value;
    }
}
