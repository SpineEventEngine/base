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

package io.spine.tools.compiler.gen.column;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.spine.base.EntityWithColumns;
import io.spine.code.gen.java.GeneratedTypeSpec;
import io.spine.code.gen.java.JavaPoetName;
import io.spine.code.gen.java.TwoParagraphDoc;
import io.spine.code.java.ClassName;
import io.spine.code.java.PackageName;
import io.spine.code.javadoc.JavadocText;
import io.spine.code.proto.FieldDeclaration;
import io.spine.type.MessageType;

import static io.spine.code.gen.java.Annotations.generatedBySpineModelCompiler;
import static io.spine.code.proto.ColumnOption.columnsOf;
import static io.spine.code.proto.ScalarType.isScalarType;
import static io.spine.code.proto.ScalarType.javaType;
import static java.lang.String.format;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * A type spec of the interface which declares entity columns.
 *
 * <p>The type inherits {@link EntityWithColumns} and is named {@code XWithColumns} where {@code X}
 * is the message type name.
 *
 * <p>The entity columns then become the interface methods which have to be implemented once the
 * entity class inherits from the interface.
 *
 * @see EntityWithColumns
 */
public final class EntityWithColumnsSpec implements GeneratedTypeSpec {

    private static final String NAME_FORMAT = "%sWithColumns";

    private final MessageType messageType;
    private final ImmutableList<FieldDeclaration> columns;

    public EntityWithColumnsSpec(MessageType messageType) {
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
                        .addSuperinterface(EntityWithColumns.class);
        addColumnGetters(builder);
        return builder.build();
    }

    /**
     * Adds a {@code getX()} method for each column of the entity.
     */
    private void addColumnGetters(TypeSpec.Builder spec) {
        columns.forEach(column -> addColumnGetter(spec, column));
    }

    private static void addColumnGetter(TypeSpec.Builder spec, FieldDeclaration fieldDeclaration) {
        MethodSpec getter = getterSpec(fieldDeclaration);
        spec.addMethod(getter);
    }

    /**
     * Obtains a getter spec from the given proto field.
     *
     * @implNote All interface methods in Java Poet have to be marked as {@code PUBLIC} and
     *         {@code ABSTRACT}, these modifiers are actually absent in the generated code.
     */
    private static MethodSpec getterSpec(FieldDeclaration declaration) {
        String methodName = declaration.javaGetterName();
        JavaPoetName fieldTypeName = fieldType(declaration);
        TypeName returnType = fieldTypeName.value();
        String methodDoc = format("Entity column `%s`.", declaration.name());
        JavadocText javadocText = JavadocText.fromEscaped(methodDoc)
                                             .withNewLine();
        MethodSpec result = MethodSpec.methodBuilder(methodName)
                                      .addJavadoc(javadocText.value())
                                      .addModifiers(PUBLIC, ABSTRACT)
                                      .returns(returnType)
                                      .build();
        return result;
    }

    private static JavaPoetName fieldType(FieldDeclaration declaration) {
        if (isScalarType(declaration)) {
            return scalarTypeName(declaration);
        }
        return className(declaration);
    }

    private static JavaPoetName scalarTypeName(FieldDeclaration declaration) {
        FieldDescriptorProto.Type protoType = declaration.descriptor()
                                                         .toProto()
                                                         .getType();
        Class<?> scalarType = javaType(protoType);
        JavaPoetName result = JavaPoetName.of(scalarType);
        return result;
    }

    private static JavaPoetName className(FieldDeclaration declaration) {
        String javaTypeName = declaration.javaTypeName();
        ClassName className = ClassName.of(javaTypeName);
        JavaPoetName result = JavaPoetName.of(className);
        return result;
    }

    /**
     * Obtains a class-level Javadoc.
     */
    private CodeBlock classJavadoc() {
        return new Javadoc().spec();
    }

    private String className() {
        String result = format(NAME_FORMAT, messageType.simpleJavaClassName());
        return result;
    }

    /**
     * The class-level doc.
     */
    private class Javadoc extends TwoParagraphDoc {

        @Override
        protected void addFirstParagraph(CodeBlock.Builder text) {
            text.add("Entity Columns of proto type {@code $L}.", messageType.javaClassName());
        }

        @Override
        protected void addSecondParagraph(CodeBlock.Builder text) {
            text.add("Implement this type to manually override the entity column values.");
        }
    }
}
