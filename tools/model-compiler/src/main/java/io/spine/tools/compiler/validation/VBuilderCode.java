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
import static io.spine.tools.compiler.validation.VBuilderMethods.methodsOf;
import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * Generates source code for a Java class with Validating Builder for a message type.
 */
final class VBuilderCode implements Logging {

    private final File rootDirectory;
    private final Indent indent;
    private final MessageType type;
    private final SimpleClassName vbClass;

    private final TypeSpec.Builder classBuilder;
    private final String javaPackage;

    VBuilderCode(String targetDir, Indent indent, MessageType type) {
        this.rootDirectory = new File(targetDir);
        this.indent = indent;
        this.type = type;
        this.vbClass = type.getValidatingBuilderClass();
        this.classBuilder = TypeSpec.classBuilder(vbClass.value());
        this.javaPackage = type.javaPackage()
                               .value();
    }

    /**
     * Writes the generated validating builders to Java file.
     */
    void write() {
        _debug("Creating spec. for class: {}", vbClass);

        TypeSpec javaClassSpec = defineClass()
                .addAnnotation(generatedBySpineModelCompiler())
                .build();

        writeClass(javaPackage, javaClassSpec);
    }

    private TypeSpec.Builder defineClass() {
        ClassName baseClass = ClassName.get(AbstractValidatingBuilder.class);
        ClassName messageClass = messageClass();
        ClassName messageBuilderClass = builderClass();
        Collection<MethodSpec> methods = methodsOf(type);

        ParameterizedTypeName superClass =
                ParameterizedTypeName.get(baseClass, messageClass, messageBuilderClass);

        classBuilder.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .superclass(superClass)
                    .addMethods(methods);
        return classBuilder;
    }

    private ClassName messageClass() {
        return ClassName.get(javaPackage, type.javaClassName()
                                              .toNested()
                                              .value());
    }

    private ClassName builderClass() {
        return messageClass().nestedClass(SimpleClassName.ofBuilder()
                                                         .value());
    }

    private void writeClass(String javaPackage, TypeSpec classToCreate) {
        _debug("Writing the {} class", vbClass);
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
        _debug("The {} class created.", vbClass);
    }
}
