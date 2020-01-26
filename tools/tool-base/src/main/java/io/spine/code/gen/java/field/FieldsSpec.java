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
import com.google.errorprone.annotations.concurrent.LazyInit;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.spine.code.gen.java.GeneratedTypeSpec;
import io.spine.code.java.PackageName;
import io.spine.code.javadoc.JavadocText;
import io.spine.code.proto.FieldDeclaration;
import io.spine.gen.SubscribableField;
import io.spine.type.MessageType;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import javax.lang.model.element.Modifier;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.spine.code.gen.java.Annotations.generatedBySpineModelCompiler;
import static io.spine.code.gen.java.EmptyCtorSpec.privateEmptyCtor;
import static io.spine.code.gen.java.FieldFactory.isEventContext;
import static io.spine.code.gen.java.FieldFactory.isSpineCoreEvent;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static java.lang.String.format;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

public abstract class FieldsSpec implements GeneratedTypeSpec {

    private final MessageType messageType;
    private final ImmutableList<FieldDeclaration> fields;

    @LazyInit
    private @MonotonicNonNull List<MessageType> nestedFieldTypes;

    FieldsSpec(MessageType messageType) {
        this.messageType = messageType;
        this.fields = messageType.fields();
    }

    public static FieldsSpec of(MessageType messageType) {
        checkNotNull(messageType);
        if (messageType.isEntityState() || isSpineCoreEvent(messageType)) {
            return new EntityStateFields(messageType);
        }
        if (messageType.isEvent() || messageType.isRejection()) {
            return new EventMessageFields(messageType);
        }
        if (isEventContext(messageType)) {
            return new EventContextFields(messageType);
        }
        throw newIllegalArgumentException(
                "Unexpected message type during fields generation: %s.", messageType.name()
        );
    }

    @Override
    public PackageName packageName() {
        return messageType.javaPackage();
    }

    @SuppressWarnings("DuplicateStringLiteralInspection") // Random duplication.
    @Override
    public TypeSpec typeSpec(Modifier... modifiers) {
        TypeSpec result = TypeSpec
                .classBuilder("Fields")
                .addJavadoc(javadoc())
                .addModifiers(modifiers)
                .addAnnotation(generatedBySpineModelCompiler())
                .addMethod(privateEmptyCtor())
                .addMethods(fields())
                .addTypes(nestedFieldContainers())
                .build();
        return result;
    }

    private ImmutableList<MethodSpec> fields() {
        ImmutableList<MethodSpec> result =
                fields.stream()
                      .map(this::topLevelFieldSpec)
                      .map(spec -> spec.methodSpec(PUBLIC, STATIC))
                      .collect(toImmutableList());
        return result;
    }

    private ImmutableList<TypeSpec> nestedFieldContainers() {
        ImmutableList<TypeSpec> result =
                nestedFieldTypes().stream()
                                  .map(type -> new NestedFieldContainer(type, fieldSupertype()))
                                  .map(type -> type.typeSpec(PUBLIC, STATIC, FINAL))
                                  .collect(toImmutableList());
        return result;
    }

    private FieldSpec topLevelFieldSpec(FieldDeclaration field) {
        return new TopLevelFieldSpec(field, fieldSupertype());
    }

    private List<MessageType> nestedFieldTypes() {
        if (nestedFieldTypes == null) {
            NestedFieldScanner scanner = new NestedFieldScanner(messageType);
            nestedFieldTypes = scanner.scan();
        }
        return nestedFieldTypes;
    }

    protected abstract Class<? extends SubscribableField> fieldSupertype();

    private static CodeBlock javadoc() {
        CodeBlock firstParagraphText = CodeBlock
                .builder()
                .add("The listing of all fields of the message type.")
                .build();
        JavadocText firstParagraph = JavadocText.fromEscaped(firstParagraphText.toString())
                                                .withNewLine()
                                                .withNewLine();
        String secondParagraphText = format(
                "The fields exposed by this class can be provided to a subscription filter%s" +
                        "on creation.",
                JavadocText.lineSeparator()
        );
        JavadocText secondParagraph = JavadocText.fromEscaped(secondParagraphText)
                                                 .withPTag()
                                                 .withNewLine()
                                                 .withNewLine();
        String thirdParagraphText =
                "Use static methods of this class to access the top-level fields of the message.";
        JavadocText thirdParagraph = JavadocText.fromEscaped(thirdParagraphText)
                                                .withPTag()
                                                .withNewLine()
                                                .withNewLine();
        String fourthParagraphText = format(
                "The nested fields can be accessed using the values returned by the top-level%s" +
                        "field accessors, through method chaining.",
                JavadocText.lineSeparator()
        );
        JavadocText fourthParagraph = JavadocText.fromEscaped(fourthParagraphText)
                                                 .withPTag()
                                                 .withNewLine();
        CodeBlock value = CodeBlock
                .builder()
                .add(firstParagraph.value())
                .add(secondParagraph.value())
                .add(thirdParagraph.value())
                .add(fourthParagraph.value())
                .build();
        return value;
    }
}
