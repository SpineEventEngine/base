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
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.spine.base.Field;
import io.spine.base.SubscribableField;
import io.spine.code.gen.java.GeneratedTypeSpec;
import io.spine.code.gen.java.JavaPoetName;
import io.spine.code.java.PackageName;
import io.spine.code.java.SimpleClassName;
import io.spine.code.javadoc.JavadocText;
import io.spine.type.MessageType;

import javax.lang.model.element.Modifier;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * A spec for the generated type which represents
 * a {@link com.google.protobuf.Message Message}-typed field of a message.
 *
 * <p>Such type, being a {@linkplain SubscribableField strongly-typed field} itself, can be both
 * passed to the message filters and used to obtain the more nested message properties.
 *
 * <p>More formally, for the given message type, the spec will define a class which:
 * <ol>
 *     <li>Is named by combining the message Java name and the {@code Field} suffix, for example,
 *         {@code UserIdField}.
 *     <li>Inherits from a {@link SubscribableField}.
 *     <li>Takes the initial {@linkplain Field field path} on construction.
 *     <li>Exposes nested message fields through the instance methods which append the name of the
 *         requested field to the wrapped field path.
 * </ol>
 *
 * <p>See the {@link FieldsSpec} for the example usage.
 */
@SuppressWarnings("DuplicateStringLiteralInspection") // Random duplication of the generated code.
final class MessageTypedField implements GeneratedTypeSpec {

    private final MessageType messageType;
    private final Class<? extends SubscribableField> fieldSupertype;

    MessageTypedField(MessageType nestedType, Class<? extends SubscribableField> fieldSupertype) {
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
                .classBuilder(typeName().value())
                .addJavadoc(javadoc())
                .addModifiers(modifiers)
                .superclass(superclass())
                .addMethod(constructor())
                .addMethods(fields())
                .build();
        return result;
    }

    private SimpleClassName typeName() {
        return messageType.javaClassName()
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
                messageType.fields()
                           .stream()
                           .map(field -> new NestedFieldSpec(field, fieldSupertype))
                           .map(spec -> spec.methodSpec(PUBLIC))
                           .collect(toImmutableList());
        return result;
    }

    /**
     * Obtains the class Javadoc.
     */
    private static CodeBlock javadoc() {
        CodeBlock firstParagraphText = CodeBlock
                .builder()
                .add("The listing of nested fields of the message type.")
                .build();
        JavadocText firstParagraph = JavadocText.fromEscaped(firstParagraphText.toString())
                                                .withNewLine();
        CodeBlock value = CodeBlock
                .builder()
                .add(firstParagraph.value())
                .build();
        return value;
    }
}
