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

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import io.spine.code.Indent;
import io.spine.code.java.SimpleClassName;
import io.spine.logging.Logging;
import io.spine.tools.compiler.TypeCache;
import io.spine.validate.AbstractValidatingBuilder;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static io.spine.tools.compiler.annotation.Annotations.generatedBySpineModelCompiler;
import static io.spine.tools.compiler.validation.ClassNames.getValidatorMessageClassName;
import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * Class which writes generated validating builders to Java files.
 *
 * @author Illia Shepilov
 */
class ValidatingBuilderWriter implements Logging {

    private final String targetDir;
    private final Indent indent;
    private final TypeCache typeCache;

    ValidatingBuilderWriter(String targetDir, Indent indent, TypeCache typeCache) {
        this.targetDir = targetDir;
        this.indent = indent;
        this.typeCache = typeCache;
    }

    /**
     * Writes the generated validating builders to Java file.
     */
    void write(VBType type) {
        log().debug("Preparing to writing the {} class under the {} package",
                    type.getJavaClass(), type.getJavaPackage());

        MethodGenerator methodsAssembler =
                new MethodGenerator(type, typeCache);
        String javaClass = type.getJavaClass();
        String javaPackage = type.getJavaPackage();
        DescriptorProto descriptor = type.getDescriptor();
        ClassName messageClassName =
                getValidatorMessageClassName(javaPackage,
                                             typeCache,
                                             descriptor.getName());
        ClassName messageBuilderClassName =
                messageClassName.nestedClass(SimpleClassName.ofBuilder()
                                                            .value());

        File rootDirectory = new File(targetDir);
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(javaClass);
        TypeSpec javaClassToWrite =
                setupClassContract(classBuilder,
                                   messageClassName,
                                   messageBuilderClassName,
                                   methodsAssembler.createMethods())
                        .addAnnotation(generatedBySpineModelCompiler())
                        .build();

        log().debug("Writing the {} class under the {} package",
                    type.getJavaClass(), type.getJavaPackage());
        writeClass(rootDirectory, javaClassToWrite, javaPackage, indent);

        log().debug("The {} class  was written under the {} package.", javaClass, javaPackage);
    }

    private static TypeSpec.Builder setupClassContract(TypeSpec.Builder typeBuilder,
                                                       ClassName messageClassParam,
                                                       ClassName messageBuilderParam,
                                                       Iterable<MethodSpec> methodSpecs) {
        ClassName abstractBuilderTypeName = ClassName.get(AbstractValidatingBuilder.class);

        ParameterizedTypeName superClass =
                ParameterizedTypeName.get(abstractBuilderTypeName,
                                          messageClassParam,
                                          messageBuilderParam);
        typeBuilder.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                   .superclass(superClass)
                   .addMethods(methodSpecs);
        return typeBuilder;
    }

    private void writeClass(File rootFolder, TypeSpec validator,
                                   String javaPackage, Indent indent) {
        try {
            Files.createDirectories(rootFolder.toPath());
            JavaFile.builder(javaPackage, validator)
                    .skipJavaLangImports(true)
                    .indent(indent.toString())
                    .build()
                    .writeTo(rootFolder);
        } catch (IOException e) {
            String exMessage = String.format("%s was not written.", rootFolder);
            log().warn(exMessage, e);
            throw newIllegalArgumentException(exMessage, e);
        }
    }
}
