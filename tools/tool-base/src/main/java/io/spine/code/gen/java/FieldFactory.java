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

package io.spine.code.gen.java;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Descriptors.Descriptor;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.spine.base.SimpleField;
import io.spine.code.java.ClassName;
import io.spine.code.java.SimpleClassName;
import io.spine.code.proto.FieldDeclaration;
import io.spine.code.proto.FieldName;
import io.spine.logging.Logging;
import io.spine.tools.protoc.nested.GeneratedNestedClass;
import io.spine.tools.protoc.nested.NestedClassFactory;
import io.spine.type.MessageType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

@Immutable
public final class FieldFactory implements NestedClassFactory, Logging {

    @Override
    public List<GeneratedNestedClass> createFor(MessageType messageType) {
        MethodSpec privateCtor = MethodSpec
                .constructorBuilder()
                .addModifiers(PRIVATE)
                .build();
        TypeSpec.Builder typeSpec = TypeSpec
                .classBuilder("Fields")
                .addModifiers(PUBLIC, STATIC, FINAL)
                .addMethod(privateCtor);
        generateFields(typeSpec, messageType);
        String generatedCode = typeSpec.build()
                                       .toString();
        GeneratedNestedClass result = new GeneratedNestedClass(generatedCode);
        return ImmutableList.of(result);
    }

    private static void generateFields(TypeSpec.Builder spec, MessageType type) {
        Set<MessageType> nestedTypes = newLinkedHashSet();
        for (FieldDeclaration field : type.fields()) {
            JavaPoetName fieldTypeName = fieldTypeName(field);
            MethodSpec fieldGetter = fieldGetter(field, fieldTypeName);
            nestedTypes.addAll(nestedTypes(field));
            spec.addMethod(fieldGetter);
        }
        List<TypeSpec> nested = nestedTypeDeclarations(nestedTypes);
        spec.addTypes(nested);
    }

    private static JavaPoetName fieldTypeName(FieldDeclaration field) {
        JavaPoetName returnType;
        if (field.isMessage() && !field.isCollection()) {
            ClassName className = field.declaringType()
                                       .javaClassName();
            JavaPoetName javaPoetName = JavaPoetName.of(className);
            String fieldTypeName = field.javaTypeName();
            SimpleClassName simple = ClassName.of(fieldTypeName)
                                              .toSimple();
            returnType = javaPoetName.nested("Fields")
                                     .nested(format("%sField", simple));
        } else {
            returnType = JavaPoetName.of(SimpleField.class);
        }
        return returnType;
    }

    private static MethodSpec fieldGetter(FieldDeclaration field, JavaPoetName returnType) {
        FieldName fieldName = field.name();
        MethodSpec result = MethodSpec
                .methodBuilder(fieldName.javaCase())
                .addModifiers(PUBLIC, STATIC)
                .returns(returnType.value())
                .addStatement("return new $T()", returnType.value())
                .build();
        return result;
    }

    private static List<MessageType> nestedTypes(FieldDeclaration field) {
        if (!field.isMessage() || field.isCollection()) {
            return new ArrayList<>();
        }
        MessageType messageType = messageTypeOf(field);
        List<MessageType> allTypes = new ArrayList<>();
        allTypes.add(messageType);
        int index = 0;
        while (index < allTypes.size()) {
            MessageType type = allTypes.get(index);
            for (FieldDeclaration declaration : type.fields()) {
                if (declaration.isMessage() && !declaration.isCollection()) {
                    MessageType nestedType = messageTypeOf(declaration);
                    if (!allTypes.contains(nestedType)) {
                        allTypes.add(nestedType);
                    }
                }
            }
            index++;
        }
        return allTypes;
    }

    private static List<TypeSpec> nestedTypeDeclarations(Collection<MessageType> nestedTypes) {
        List<TypeSpec> result = nestedTypes
                .stream()
                .map(FieldFactory::declarationOf)
                .collect(toList());
        return result;
    }

    private static TypeSpec declarationOf(MessageType type) {
        TypeSpec fieldTypeSpec = TypeSpec
                .classBuilder(format("%sField", type.javaClassName().toSimple()))
                .addModifiers(PUBLIC, STATIC, FINAL)
                .build();
        return fieldTypeSpec;
    }

    private static MessageType messageTypeOf(FieldDeclaration field) {
        checkArgument(field.isMessage() && !field.isCollection());
        Descriptor descriptor = field.descriptor()
                                     .getMessageType();
        return new MessageType(descriptor);
    }
}
