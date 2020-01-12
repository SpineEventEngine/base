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
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.spine.base.Field;
import io.spine.base.SimpleField;
import io.spine.base.SubscribableField;
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
        TypeSpec.Builder typeSpec = TypeSpec
                .classBuilder("Fields")
                .addModifiers(PUBLIC, STATIC, FINAL)
                .addMethod(PrivateCtor.spec());
        generateFields(typeSpec, messageType);
        String generatedCode = typeSpec.build()
                                       .toString();
        GeneratedNestedClass result = new GeneratedNestedClass(generatedCode);
        return ImmutableList.of(result);
    }

    private static void generateFields(TypeSpec.Builder spec, MessageType type) {
        Set<MessageType> nestedTypes = newLinkedHashSet();
        for (FieldDeclaration field : type.fields()) {
            JavaPoetName fieldTypeName = fieldTypeName(field, type);
            MethodSpec fieldGetter = fieldGetter(field, fieldTypeName, type);
            nestedTypes.addAll(nestedTypes(field));
            spec.addMethod(fieldGetter);
        }
        List<TypeSpec> nested = nestedTypeDeclarations(nestedTypes, type);
        spec.addTypes(nested);
    }

    private static JavaPoetName fieldTypeName(FieldDeclaration field, MessageType enclosing) {
        JavaPoetName returnType;
        if (field.isMessage() && !field.isCollection()) {
            String fieldTypeName = field.javaTypeName();
            SimpleClassName simpleClassName = ClassName.of(fieldTypeName)
                                                       .toSimple();
            returnType = JavaPoetName.of(simpleClassName.with("Field"));
        } else {
            com.squareup.javapoet.ClassName name = JavaPoetName.of(SimpleField.class)
                                                               .className();
            JavaPoetName parameterName =
                    JavaPoetName.of(enclosing.javaClassName().toSimple());
            ParameterizedTypeName typeName = ParameterizedTypeName.get(name, parameterName.value());
            returnType = JavaPoetName.of(typeName);
        }
        return returnType;
    }

    private static MethodSpec
    fieldGetter(FieldDeclaration field, JavaPoetName returnType, MessageType enclosingType) {
        FieldName fieldName = field.name();
        MethodSpec.Builder methodSpec = MethodSpec
                .methodBuilder(fieldName.javaCase())
                .addModifiers(PUBLIC, STATIC)
                .returns(returnType.value());
        if (returnType.value().toString().contains("SimpleField")) {
            TypeName enclosingClassName =
                    JavaPoetName.of(enclosingType.javaClassName().toSimple()).value();
            methodSpec.addStatement(
                    // return new SimpleField<>(FieldPath.newBuilder().addFieldName("field_name").build(), EnclosingMessage.class);
                    "return new $T<>($T.named(\"$L\"), $T.class)",
                    SimpleField.class, Field.class, fieldName.value(), enclosingClassName
            );
        } else {
            methodSpec.addStatement("return new $T($T.named(\"$L\"))",
                                    returnType.value(), Field.class, fieldName.value());
        }
        return methodSpec.build();
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

    private static List<TypeSpec>
    nestedTypeDeclarations(Collection<MessageType> nestedTypes, MessageType enclosingType) {
        List<TypeSpec> result = nestedTypes
                .stream()
                .map(nested -> declarationOf(nested, enclosingType))
                .collect(toList());
        return result;
    }

    private static TypeSpec declarationOf(MessageType nested, MessageType enclosing) {
        ClassName enclosingClass = enclosing.javaClassName();
        TypeName enclosingClassName = JavaPoetName.of(enclosingClass.toSimple())
                                                  .value();
        JavaPoetName nestedTypeName = JavaPoetName.of(SubscribableField.class);
        ParameterizedTypeName superclass =
                ParameterizedTypeName.get(nestedTypeName.className(), enclosingClassName);
        TypeSpec.Builder spec = TypeSpec.classBuilder(format("%sField", nested.javaClassName()
                                                                              .toSimple()))
                                        .addModifiers(PUBLIC, STATIC, FINAL)
                                        .superclass(superclass);
        String argName = "field";
        MethodSpec ctor = MethodSpec
                .constructorBuilder()
                .addModifiers(PRIVATE)
                .addParameter(Field.class, argName)
                // TODO:2019-12-20:dmytro.kuzmin:WIP: Check if we can rid of `JavaPoetName.of()`.
                .addStatement("super($L, $T.class)", argName, enclosingClassName)
                .build();
        spec.addMethod(ctor);

        for (FieldDeclaration field : nested.fields()) {
            JavaPoetName returnType = fieldTypeName(field, enclosing);
            MethodSpec.Builder methodSpec = MethodSpec
                    .methodBuilder(field.name()
                                        .javaCase())
                    .addModifiers(PUBLIC)
                    .returns(returnType.value());
            // TODO:2019-12-20:dmytro.kuzmin:WIP: Implement more robust check.
            if (returnType.value().toString().contains("SimpleField")) {
                methodSpec.addStatement("return new $T(getField().nested(\"$L\"), $T.class)",
                                        returnType.value(), field.name().value(),
                                        enclosingClassName);
            } else {
                methodSpec.addStatement("return new $T(getField().nested(\"$L\"))",
                                        returnType.value(), field.name().value());
            }
            spec.addMethod(methodSpec.build());
        }
        return spec.build();
    }

    private static MessageType messageTypeOf(FieldDeclaration field) {
        checkArgument(field.isMessage() && !field.isCollection());
        Descriptor descriptor = field.descriptor()
                                     .getMessageType();
        return new MessageType(descriptor);
    }
}
