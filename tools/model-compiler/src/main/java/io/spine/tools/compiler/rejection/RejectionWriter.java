/*
 * Copyright 2018, TeamDev. All rights reserved.
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
package io.spine.tools.compiler.rejection;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import io.spine.base.ThrowableMessage;
import io.spine.code.generate.Indent;
import io.spine.code.java.PackageName;
import io.spine.code.javadoc.JavadocText;
import io.spine.code.proto.RejectionType;
import io.spine.logging.Logging;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static io.spine.code.java.ClassName.OUTER_CLASS_DELIMITER;
import static io.spine.tools.compiler.annotation.Annotations.generatedBySpineModelCompiler;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Generates Java code for a rejection based on its Protobuf descriptor.
 */
public class RejectionWriter implements Logging {

    private static final NoArgMethod getMessageThrown = new NoArgMethod("getMessageThrown");

    private final RejectionType declaration;
    private final ClassName messageClass;
    private final File outputDirectory;

    private final RejectionBuilderWriter builder;
    private final Indent indent;

    /**
     * Creates a new instance.
     *  @param rejection
     *         a rejection declaration
     * @param outputDirectory
     *         a directory to write a Rejection
     * @param indent
     *          the indentation for generated source code
     */
    public RejectionWriter(RejectionType rejection, File outputDirectory, Indent indent) {
        this.declaration = rejection;
        this.messageClass = toJavaPoetName(rejection.messageClass());
        this.outputDirectory = outputDirectory;
        this.builder = new RejectionBuilderWriter(rejection,
                                                  messageClass,
                                                  toJavaPoetName(rejection.throwableClass())
        );
        this.indent = indent;
    }

    /**
     * Initiates writing.
     */
    public void write() {
        try {
            Logger log = log();
            log.debug("Creating the output directory {}", outputDirectory.getPath());
            Files.createDirectories(outputDirectory.toPath());

            String className = declaration.simpleJavaClassName()
                                          .value();
            log.debug("Constructing class {}", className);
            TypeSpec rejection =
                    TypeSpec.classBuilder(className)
                            .addJavadoc(classJavadoc())
                            .addAnnotation(generatedBySpineModelCompiler())
                            .addModifiers(PUBLIC)
                            .superclass(ThrowableMessage.class)
                            .addField(serialVersionUID())
                            .addMethod(constructor())
                            .addMethod(getMessageThrown())
                            .addMethod(builder.newBuilder())
                            .addType(builder.typeDeclaration())
                            .build();
            JavaFile javaFile =
                    JavaFile.builder(declaration.javaPackage()
                                                .value(),
                                     rejection)
                            .skipJavaLangImports(true)
                            .indent(indent.toString())
                            .build();
            log.debug("Writing {}", className);
            javaFile.writeTo(outputDirectory);
            log.debug("Rejection {} written successfully", className);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private MethodSpec constructor() {
        log().debug("Creating the constructor for the type '{}'",
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

    private MethodSpec getMessageThrown() {
        String methodSignature = getMessageThrown.signature();
        log().debug("Constructing method {}", methodSignature);
        ClassName returnType = messageClass;
        return MethodSpec.methodBuilder(getMessageThrown.name())
                         .addAnnotation(Override.class)
                         .addModifiers(PUBLIC)
                         .returns(returnType)
                         .addStatement("return ($T) super.$L", returnType, methodSignature)
                         .build();
    }

    /**
     * Generates a Javadoc content for the rejection.
     *
     * @return the class-level Javadoc content
     */
    private CodeBlock classJavadoc() {
        JavadocText leadingComments =
                declaration.documentation()
                           .leadingComments()
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
     * Generates a Javadoc content for the rejection constructor.
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
                                 io.spine.code.java.FieldName.serialVersionUID()
                                                             .value(),
                                 PRIVATE, STATIC, FINAL)
                        .initializer("0L")
                        .build();
    }

    private static ClassName toJavaPoetName(io.spine.code.java.ClassName className) {
        String noDelimiterName = className.value().replace(OUTER_CLASS_DELIMITER, '.');
        return ClassName.bestGuess(noDelimiterName);
    }
}
