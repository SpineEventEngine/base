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

package io.spine.code.gen.java;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.concurrent.LazyInit;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.spine.code.java.PackageName;
import io.spine.code.proto.FieldDeclaration;
import io.spine.type.MessageType;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import javax.lang.model.element.Modifier;
import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Lists.newLinkedList;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

final class FieldsSpec implements GeneratedTypeSpec {

    private final MessageType messageType;
    private final ImmutableList<FieldDeclaration> fields;

    @LazyInit
    private @MonotonicNonNull List<MessageType> nestedFieldTypes;

    private FieldsSpec(MessageType messageType) {
        this.messageType = messageType;
        this.fields = messageType.fields();
    }

    static FieldsSpec of(MessageType messageType) {
        return new FieldsSpec(messageType);
    }

    @Override
    public PackageName packageName() {
        return messageType.javaPackage();
    }

    @Override
    public TypeSpec typeSpec(Modifier... modifiers) {
        TypeSpec result = TypeSpec
                .classBuilder("Fields")
                .addModifiers(modifiers)
                .addMethod(PrivateCtor.spec())
                .addMethods(fieldMethods())
                .addTypes(nestedFields())
                .build();
        return result;
    }

    private ImmutableList<MethodSpec> fieldMethods() {
        ImmutableList<MethodSpec> result =
                fields.stream()
                      .map(this::fieldSpec)
                      .map(spec -> spec.methodSpec(PUBLIC, STATIC))
                      .collect(toImmutableList());
        return result;
    }

    private Iterable<TypeSpec> nestedFields() {
        return ImmutableList.of();
    }

    private List<MessageType> nestedFieldTypes() {
        if (nestedFieldTypes == null) {
            nestedFieldTypes = collectNestedFieldTypes();
        }
        return nestedFieldTypes;
    }

    private FieldSpec fieldSpec(FieldDeclaration field) {
        return new FieldSpec(field, messageType.simpleJavaClassName());
    }

    private List<MessageType> collectNestedFieldTypes() {
        List<MessageType> result = newLinkedList();
        int index = -1;
        while (index < result.size()) {
            if (index == -1) {
                addMessageFields(this.messageType, result);
            } else {
                MessageType messageType = result.get(index);
                addMessageFields(messageType, result);
            }
            index++;
        }
        return result;
    }

    private static void addMessageFields(MessageType messageType, List<MessageType> result) {
        messageType.fields()
                   .stream()
                   .filter(FieldDeclaration::isMessage)
                   .filter(field -> !field.isCollection())
                   .map(FieldDeclaration::messageType)
                   .forEach(result::add);
    }
}
