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

package io.spine.tools.compiler.validation;

import com.google.common.base.Splitter;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import io.spine.code.fs.java.FileName;
import io.spine.code.gen.Indent;
import io.spine.code.gen.java.NestedClassName;
import io.spine.code.gen.java.VBuilderClassName;
import io.spine.code.java.SimpleClassName;
import io.spine.logging.Logging;
import io.spine.type.MessageType;
import io.spine.validate.AbstractValidatingBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.compiler.annotation.Annotations.generatedBySpineModelCompiler;
import static io.spine.tools.compiler.validation.VBuilderMethods.methodsOf;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static java.lang.String.format;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * Generates source code for a Java class with Validating Builder for a message type.
 */
final class VBuilderCode implements Logging {

    private static final Splitter DOT_SPLITTER = Splitter.on('.');
    private final File targetDir;
    private final Indent indent;
    private final MessageType type;
    private final SimpleClassName vbClass;

    private final TypeSpec.Builder classBuilder;
    private final String javaPackage;

    VBuilderCode(File targetDir, Indent indent, MessageType type) {
        this.targetDir = checkNotNull(targetDir);
        this.indent = checkNotNull(indent);
        this.type = checkNotNull(type);
        this.vbClass = VBuilderClassName.of(type);
        this.classBuilder = TypeSpec.classBuilder(vbClass.value());
        this.javaPackage = type.javaPackage()
                               .value();
    }

    /**
     * Writes the generated validating builders to Java file.
     *
     * @return the name of the generated file, which is used for testing
     */
    @CanIgnoreReturnValue
    File write() {
        _debug("Creating spec. for class: {}", vbClass);

        TypeSpec javaClassSpec = defineClass()
                .addAnnotation(generatedBySpineModelCompiler())
                .build();

        File created = writeClass(javaPackage, javaClassSpec);
        return created;
    }

    private TypeSpec.Builder defineClass() {
        ClassName baseClass = ClassName.get(AbstractValidatingBuilder.class);
        ClassName messageClass = messageClass();
        ClassName messageBuilderClass = builderClass();

        ParameterizedTypeName superClass =
                ParameterizedTypeName.get(baseClass, messageClass, messageBuilderClass);

        Collection<MethodSpec> methods = methodsOf(type);
        classBuilder.addModifiers(PUBLIC, FINAL)
                    .superclass(superClass)
                    .addMethods(methods);
        return classBuilder;
    }

    private ClassName messageClass() {
        return ClassName.get(javaPackage, NestedClassName.from(type.javaClassName())
                                                         .value());
    }

    private ClassName builderClass() {
        return messageClass().nestedClass(SimpleClassName.ofBuilder()
                                                         .value());
    }

    @CanIgnoreReturnValue
    private File writeClass(String javaPackage, TypeSpec classToCreate) {
        _debug("Writing the {} class", vbClass);
        try {
            Path dir = targetDir.toPath();
            Files.createDirectories(dir);
            JavaFile.builder(javaPackage, classToCreate)
                    .skipJavaLangImports(true)
                    .indent(indent.toString())
                    .build()
                    .writeTo(targetDir);

            File createdFile = resolve(javaPackage, classToCreate.name);
            _debug("The {} class created, written to file {}.", vbClass, createdFile);
            return createdFile;

        } catch (IOException e) {
            String exMessage = format("%s was not written.", targetDir);
            _warn(exMessage, e);
            throw newIllegalArgumentException(exMessage, e);
        }
    }

    /**
     * Obtains the name of the generated file.
     */
    private File resolve(String javaPackage, String className) {
        Path dir = targetDir.toPath();
        if (!javaPackage.isEmpty()) {
            for (String packageDir :  DOT_SPLITTER.split(javaPackage)) {
                dir = dir.resolve(packageDir);
            }
        }
        File result = dir.resolve(FileName.forType(className).value()).toFile();
        return result;
    }
}
