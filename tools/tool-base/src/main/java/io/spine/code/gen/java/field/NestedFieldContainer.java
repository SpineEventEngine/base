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

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.spine.base.Field;
import io.spine.code.gen.java.GeneratedTypeSpec;
import io.spine.code.gen.java.JavaPoetName;
import io.spine.code.java.PackageName;
import io.spine.gen.SubscribableField;
import io.spine.type.MessageType;

import javax.lang.model.element.Modifier;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.String.format;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;

@SuppressWarnings("DuplicateStringLiteralInspection") // Random duplication of the generated code.
final class NestedFieldContainer implements GeneratedTypeSpec {

    private final MessageType messageType;
    private final Class<? extends SubscribableField> fieldSupertype;

    NestedFieldContainer(MessageType nestedType,
                         Class<? extends SubscribableField> fieldSupertype) {
        this.messageType = nestedType;
        this.fieldSupertype = fieldSupertype;
    }

    @Override
    public PackageName packageName() {
        return messageType.javaPackage();
    }

    @Override
    public TypeSpec typeSpec(Modifier... modifiers) {
        TypeSpec result = TypeSpec
                .classBuilder(typeName())
                .addModifiers(modifiers)
                .superclass(superclass())
                .addMethod(constructor())
                .addMethods(fields())
                .build();
        return result;
    }

    private String typeName() {
        return format("%sField", messageType.javaClassName().toSimple());
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
                messageType.fields()
                           .stream()
                           .map(field -> new NestedFieldSpec(field, fieldSupertype))
                           .map(spec -> spec.methodSpec(PUBLIC))
                           .collect(toImmutableList());
        return result;
    }
}
