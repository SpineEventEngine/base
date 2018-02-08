/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

package io.spine.gradle.compiler.validate;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import io.spine.gradle.compiler.Indent;
import io.spine.gradle.compiler.message.MessageTypeCache;
import io.spine.tools.CodeGeneration;
import io.spine.validate.AbstractValidatingBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static io.spine.gradle.compiler.util.JavaCode.constructGeneratedAnnotation;
import static io.spine.gradle.compiler.util.JavaSources.getBuilderClassName;
import static io.spine.gradle.compiler.validate.ClassNames.getValidatorMessageClassName;
import static io.spine.tools.CodeGeneration.generatedAnnotation;
import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * Class which writes generated validating builders to Java files.
 *
 * @author Illia Shepilov
 */
class ValidatingBuilderWriter {

    private final String targetDir;
    private final Indent indent;
    private final MessageTypeCache messageTypeCache;

    ValidatingBuilderWriter(String targetDir, Indent indent, MessageTypeCache messageTypeCache) {
        this.targetDir = targetDir;
        this.indent = indent;
        this.messageTypeCache = messageTypeCache;
    }

    /**
     * Writes the generated validating builders to Java file.
     */
    void write(VBMetadata metadata) {
        log().debug("Preparing to writing the {} class under the {} package",
                    metadata.getJavaClass(), metadata.getJavaPackage());

        final MethodGenerator methodsAssembler =
                new MethodGenerator(metadata, messageTypeCache);
        final String javaClass = metadata.getJavaClass();
        final String javaPackage = metadata.getJavaPackage();
        final DescriptorProto descriptor = metadata.getMsgDescriptor();
        final ClassName messageClassName =
                getValidatorMessageClassName(javaPackage,
                                             messageTypeCache,
                                             descriptor.getName());
        final ClassName messageBuilderClassName =
                messageClassName.nestedClass(getBuilderClassName());

        final File rootDirectory = new File(targetDir);
        final TypeSpec.Builder classBuilder = TypeSpec.classBuilder(javaClass);
        final TypeSpec javaClassToWrite =
                setupClassContract(classBuilder,
                                   messageClassName,
                                   messageBuilderClassName,
                                   methodsAssembler.createMethods())
                        .addAnnotation(generatedAnnotation())
                        .build();

        log().debug("Writing the {} class under the {} package",
                    metadata.getJavaClass(), metadata.getJavaPackage());
        writeClass(rootDirectory, javaClassToWrite, javaPackage, indent);

        log().debug("The {} class  was written under the {} package.", javaClass, javaPackage);
    }

    private static TypeSpec.Builder setupClassContract(TypeSpec.Builder typeBuilder,
                                                       ClassName messageClassParam,
                                                       ClassName messageBuilderParam,
                                                       Iterable<MethodSpec> methodSpecs) {
        final ClassName abstractBuilderTypeName = ClassName.get(AbstractValidatingBuilder.class);

        final ParameterizedTypeName superClass =
                ParameterizedTypeName.get(abstractBuilderTypeName,
                                          messageClassParam,
                                          messageBuilderParam);
        typeBuilder.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
               .superclass(superClass)
               .addMethods(methodSpecs);
        return typeBuilder;
    }

    private static void writeClass(File rootFolder, TypeSpec validator,
                                   String javaPackage, Indent indent) {
        try {
            Files.createDirectories(rootFolder.toPath());
            JavaFile.builder(javaPackage, validator)
                    .skipJavaLangImports(true)
                    .indent(indent.toString())
                    .build()
                    .writeTo(rootFolder);
        } catch (IOException e) {
            final String exMessage = String.format("%s was not written.", rootFolder);
            log().warn(exMessage, e);
            throw newIllegalArgumentException(exMessage, e);
        }
    }

    private enum LogSingleton {
        INSTANCE;

        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(ValidatingBuilderWriter.class);
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }
}
