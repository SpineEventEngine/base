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

package io.spine.code.gen.java.column;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.spine.code.gen.java.GeneratedTypeSpec;
import io.spine.code.java.PackageName;
import io.spine.code.javadoc.JavadocText;
import io.spine.code.proto.FieldDeclaration;
import io.spine.type.MessageType;

import javax.lang.model.element.Modifier;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.spine.code.gen.java.Annotations.generatedBySpineModelCompiler;
import static io.spine.code.gen.java.EmptyCtorSpec.privateEmptyCtor;
import static io.spine.code.proto.ColumnOption.columnsOf;
import static java.lang.String.format;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

public final class ColumnsSpec implements GeneratedTypeSpec {

    private final MessageType messageType;
    private final ImmutableList<FieldDeclaration> columns;

    private ColumnsSpec(MessageType messageType) {
        this.messageType = messageType;
        this.columns = columnsOf(messageType);
    }

    public static ColumnsSpec of(MessageType messageType) {
        checkNotNull(messageType);
        return new ColumnsSpec(messageType);
    }

    @Override
    public PackageName packageName() {
        return messageType.javaPackage();
    }

    @Override
    public TypeSpec typeSpec(Modifier... modifiers) {
        TypeSpec result = TypeSpec
                .classBuilder("Columns")
                .addJavadoc(javadoc())
                .addAnnotation(generatedBySpineModelCompiler())
                .addModifiers(modifiers)
                .addMethod(privateEmptyCtor())
                .addMethods(columns())
                .build();
        return result;
    }

    private ImmutableList<MethodSpec> columns() {
        ImmutableList<MethodSpec> result =
                columns.stream()
                       .map(ColumnSpec::new)
                       .map(columnSpec -> columnSpec.methodSpec(PUBLIC, STATIC))
                       .collect(toImmutableList());
        return result;
    }

    /**
     * Obtains the class Javadoc.
     */
    private static CodeBlock javadoc() {
        CodeBlock firstParagraphText = CodeBlock
                .builder()
                .add("The listing of all entity columns of the type.")
                .build();
        JavadocText firstParagraph = JavadocText.fromEscaped(firstParagraphText.toString())
                                                .withNewLine()
                                                .withNewLine();

        String secondParagraphText = format(
                "Use static methods of this class to access the columns of the entity%s" +
                        "which can then be used for query filters creation.",
                JavadocText.lineSeparator()
        );
        JavadocText secondParagraph = JavadocText.fromEscaped(secondParagraphText)
                                                 .withPTag()
                                                 .withNewLine();
        CodeBlock value = CodeBlock
                .builder()
                .add(firstParagraph.value())
                .add(secondParagraph.value())
                .build();
        return value;
    }
}
