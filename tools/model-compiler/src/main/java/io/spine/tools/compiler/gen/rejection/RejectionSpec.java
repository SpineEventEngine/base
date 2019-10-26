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
package io.spine.tools.compiler.gen.rejection;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.spine.base.ThrowableMessage;
import io.spine.code.gen.java.FieldName;
import io.spine.code.java.PackageName;
import io.spine.code.javadoc.JavadocText;
import io.spine.logging.Logging;
import io.spine.tools.compiler.gen.GeneratedTypeSpec;
import io.spine.tools.compiler.gen.JavaPoetName;
import io.spine.tools.compiler.gen.NoArgMethod;
import io.spine.type.RejectionType;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static io.spine.tools.compiler.annotation.Annotations.generatedBySpineModelCompiler;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * A spec for a rejection type.
 *
 * <p>The generated rejection types extend {@link ThrowableMessage} and enclose an instance of the
 * corresponding proto message.
 */
public final class RejectionSpec implements GeneratedTypeSpec, Logging {

    private static final NoArgMethod messageThrown = new NoArgMethod("messageThrown");

    private final RejectionType declaration;
    private final JavaPoetName messageClass;

    private final RejectionBuilderSpec builder;

    /**
     * Creates a new instance.
     *
     *  @param rejectionType
     *         a rejection declaration
     */
    public RejectionSpec(RejectionType rejectionType) {
        this.declaration = rejectionType;
        this.messageClass = JavaPoetName.of(rejectionType.messageClass());
        this.builder = new RejectionBuilderSpec(
                rejectionType, messageClass, JavaPoetName.of(rejectionType.throwableClass())
        );
    }

    @Override
    public PackageName packageName() {
        PackageName packageName = declaration.javaPackage();
        return packageName;
    }

    @Override
    public TypeSpec typeSpec() {
        String className = declaration.simpleJavaClassName()
                                      .value();
        TypeSpec rejection =
                TypeSpec.classBuilder(className)
                        .addJavadoc(classJavadoc())
                        .addAnnotation(generatedBySpineModelCompiler())
                        .addModifiers(PUBLIC)
                        .superclass(ThrowableMessage.class)
                        .addField(serialVersionUID())
                        .addMethod(constructor())
                        .addMethod(messageThrown())
                        .addMethod(builder.newBuilder())
                        .addType(builder.typeSpec())
                        .build();
        return rejection;
    }

    private MethodSpec constructor() {
        _debug().log("Creating the constructor for the type `%s`.",
                    declaration.simpleJavaClassName());
        ParameterSpec builderParameter = builder.asParameter();
        CodeBlock buildRejectionMessage = builder.buildRejectionMessage();
        return constructorBuilder()
                .addJavadoc(constructorJavadoc(builderParameter))
                .addModifiers(PRIVATE)
                .addParameter(builderParameter)
                .addStatement("super($L)", buildRejectionMessage.toString())
                .build();
    }

    private MethodSpec messageThrown() {
        String methodSignature = messageThrown.signature();
        _debug().log("Constructing method `%s`.", methodSignature);
        TypeName returnType = messageClass.value();
        return MethodSpec.methodBuilder(messageThrown.name())
                         .addAnnotation(Override.class)
                         .addModifiers(PUBLIC)
                         .returns(returnType)
                         .addStatement("return ($T) super.$L", returnType, methodSignature)
                         .build();
    }

    /**
     * A Javadoc content for the rejection.
     *
     * @return the class-level Javadoc content
     */
    private CodeBlock classJavadoc() {
        JavadocText leadingComments =
                declaration.leadingComments()
                           .map(text -> JavadocText.fromUnescaped(text)
                                                   .inPreTags()
                                                   .withNewLine())
                           .orElse(JavadocText.fromEscaped(""));
        PackageName rejectionPackage = declaration.javaPackage();
        CodeBlock sourceProtoNote = CodeBlock
                .builder()
                .add("Rejection based on proto type ")
                .add("{@code $L.$L}", rejectionPackage, declaration.simpleJavaClassName())
                .build();
        return CodeBlock
                .builder()
                .add(leadingComments.value())
                .add(JavadocText.fromEscaped(sourceProtoNote.toString())
                                .withNewLine()
                                .value())
                .build();
    }

    /**
     * A Javadoc content for the rejection constructor.
     *
     * @param builderParameter
     *         the name of a rejection builder parameter
     * @return the constructor Javadoc content
     */
    private static CodeBlock constructorJavadoc(ParameterSpec builderParameter) {
        JavadocText generalPart = JavadocText.fromUnescaped("Creates a new instance.")
                                             .withNewLine()
                                             .withNewLine();
        CodeBlock paramsBlock = CodeBlock.of("@param $N the builder for the rejection",
                                             builderParameter);
        JavadocText paramsPart = JavadocText.fromEscaped(paramsBlock.toString())
                                            .withNewLine();
        return CodeBlock.builder()
                        .add(generalPart.value())
                        .add(paramsPart.value())
                        .build();
    }

    private static FieldSpec serialVersionUID() {
        return FieldSpec.builder(long.class,
                                 FieldName.serialVersionUID()
                                          .value(),
                                 PRIVATE, STATIC, FINAL)
                        .initializer("0L")
                        .build();
    }
}
