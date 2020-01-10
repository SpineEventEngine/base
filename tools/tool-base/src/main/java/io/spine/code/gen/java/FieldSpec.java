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
import io.spine.code.proto.FieldDeclaration;
import io.spine.type.MessageType;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import javax.lang.model.element.Modifier;
import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;

final class FieldSpec {

    private final MessageType messageType;
    private final ImmutableList<FieldDeclaration> fields;

    @LazyInit
    private @MonotonicNonNull List<MessageType> nestedFieldTypes;

    private FieldSpec(MessageType messageType) {
        this.messageType = messageType;
        this.fields = messageType.fields();
    }

    static FieldSpec of(MessageType messageType) {
        return new FieldSpec(messageType);
    }

    // TODO:2019-12-20:dmytro.kuzmin:WIP: Consolidate singular/plural name usage after varargs.
    TypeSpec asTypeSpec(Modifier... modifiers) {
        TypeSpec result = TypeSpec
                .classBuilder("Fields")
                .addModifiers(modifiers)
                .addMethod(PrivateCtor.spec())
                .addMethods(fieldMethods())
                .addTypes(nestedFields())
                .build();
        return result;
    }

    private Iterable<MethodSpec> fieldMethods() {
        ImmutableList.Builder<MethodSpec> result = ImmutableList.builder();
        return result.build();
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
