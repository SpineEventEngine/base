/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.code.gen.java.field;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.spine.base.Field;
import io.spine.base.SubscribableField;
import io.spine.code.gen.java.GeneratedJavadoc;
import io.spine.code.gen.java.GeneratedTypeSpec;
import io.spine.code.gen.java.JavaPoetName;
import io.spine.code.java.ClassName;
import io.spine.code.java.PackageName;
import io.spine.code.java.SimpleClassName;
import io.spine.type.MessageType;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * A spec of the generated type which represents
 * a {@link com.google.protobuf.Message Message}-typed field of the message.
 *
 * <p>Such type, being a {@linkplain SubscribableField strongly-typed field} itself, can be both
 * passed to the message filters and used to obtain the more nested message properties.
 *
 * <p>More formally, for the given field type, the spec will define a class which:
 * <ol>
 *     <li>Is named by combining the type Java name and the {@code Field} suffix, for example,
 *         {@code UserIdField}.
 *     <li>Inherits from a {@link SubscribableField} or one of its descendants.
 *     <li>Accepts an initial {@linkplain Field field path} on construction.
 *     <li>Exposes nested message fields through the instance methods which append the name of the
 *         requested field to the enclosed field path.
 * </ol>
 *
 * <p>See the {@link FieldContainerSpec} for the example usage.
 */
@SuppressWarnings("DuplicateStringLiteralInspection") // Random duplication of the generated code.
final class MessageTypedField implements GeneratedTypeSpec {

    private final MessageType fieldType;
    private final ClassName fieldSupertype;

    MessageTypedField(MessageType fieldType, ClassName fieldSupertype) {
        this.fieldType = fieldType;
        this.fieldSupertype = fieldSupertype;
    }

    @Override
    public PackageName packageName() {
        return fieldType.javaPackage();
    }

    @Override
    public TypeSpec typeSpec() {
        TypeSpec result = TypeSpec
                .classBuilder(typeName().value())
                .addJavadoc(javadoc().spec())
                .addModifiers(PUBLIC, STATIC, FINAL)
                .superclass(superclass())
                .addMethod(constructor())
                .addMethods(fields())
                .build();
        return result;
    }

    private SimpleClassName typeName() {
        return fieldType.javaClassName()
                        .toSimple()
                        .with("Field");
    }

    private TypeName superclass() {
        JavaPoetName type = JavaPoetName.of(fieldSupertype);
        TypeName result = type.value();
        return result;
    }

    private static MethodSpec constructor() {
        String argName = "field";
        MethodSpec result = MethodSpec
                .constructorBuilder()
                .addModifiers(PRIVATE)
                .addParameter(Field.class, argName)
                .addStatement("super($L)", argName)
                .build();
        return result;
    }

    private Iterable<MethodSpec> fields() {
        ImmutableList<MethodSpec> result =
                fieldType.fields()
                         .stream()
                         .map(field -> new NestedFieldAccessor(field, fieldSupertype))
                         .map(FieldAccessor::methodSpec)
                         .collect(toImmutableList());
        return result;
    }

    /**
     * Obtains the class Javadoc.
     */
    private static GeneratedJavadoc javadoc() {
        CodeBlock text = CodeBlock.of("The listing of nested fields of the message type.");
        return GeneratedJavadoc.singleParagraph(text);
    }
}
