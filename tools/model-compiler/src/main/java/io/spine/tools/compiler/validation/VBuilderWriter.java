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

package io.spine.tools.compiler.validation;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import io.spine.code.Indent;
import io.spine.code.java.SimpleClassName;
import io.spine.code.proto.MessageType;
import io.spine.logging.Logging;
import io.spine.validate.AbstractValidatingBuilder;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;

import static io.spine.tools.compiler.annotation.Annotations.generatedBySpineModelCompiler;
import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * Generates source code for a Java class with Validating Builder for a message type.
 */
class VBuilderWriter implements Logging {

    private final File rootDirectory;
    private final Indent indent;

    VBuilderWriter(String targetDir, Indent indent) {
        this.rootDirectory = new File(targetDir);
        this.indent = indent;
    }

    /**
     * Writes the generated validating builders to Java file.
     */
    void write(MessageType type) {
        String validatingBuilderClass = type.getValidatingBuilderClass()
                                            .value();

        log().debug("Creating spec. for class: {}", validatingBuilderClass);

        String javaPackage = type.javaPackage()
                                 .value();
        ClassName messageClass =
                ClassName.get(javaPackage, type.javaClassName()
                                               .toNested()
                                               .value());
        ClassName messageBuilderClassName =
                messageClass.nestedClass(SimpleClassName.ofBuilder()
                                                        .value());
        Collection<MethodSpec> methods = collectMethods(type);
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(validatingBuilderClass);
        TypeSpec javaClassSpec =
                defineClass(classBuilder, messageClass, messageBuilderClassName, methods)
                        .addAnnotation(generatedBySpineModelCompiler())
                        .build();

        log().debug("Writing the {} class", validatingBuilderClass);

        writeClass(javaPackage, javaClassSpec);

        log().debug("The {} class created.", validatingBuilderClass);
    }

    private static Collection<MethodSpec> collectMethods(MessageType type) {
        MethodAssembler methodsAssembler = new MethodAssembler(type);
        return methodsAssembler.createMethods();
    }

    private static TypeSpec.Builder defineClass(TypeSpec.Builder typeBuilder,
                                                ClassName messageClass,
                                                ClassName messageBuilderClass,
                                                Iterable<MethodSpec> methodSpecs) {
        ClassName abstractClassName = ClassName.get(AbstractValidatingBuilder.class);

        ParameterizedTypeName superClass =
                ParameterizedTypeName.get(abstractClassName,
                                          messageClass,
                                          messageBuilderClass);
        typeBuilder.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                   .superclass(superClass)
                   .addMethods(methodSpecs);
        return typeBuilder;
    }

    private void writeClass(String javaPackage, TypeSpec classToCreate) {
        try {
            Files.createDirectories(rootDirectory.toPath());
            JavaFile.builder(javaPackage, classToCreate)
                    .skipJavaLangImports(true)
                    .indent(indent.toString())
                    .build()
                    .writeTo(rootDirectory);
        } catch (IOException e) {
            String exMessage = String.format("%s was not written.", rootDirectory);
            log().warn(exMessage, e);
            throw newIllegalArgumentException(exMessage, e);
        }
    }
}
