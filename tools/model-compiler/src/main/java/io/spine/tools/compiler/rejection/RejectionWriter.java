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

import com.google.common.collect.Maps;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.spine.base.ThrowableMessage;
import io.spine.code.proto.RejectionDeclaration;
import io.spine.logging.Logging;
import io.spine.tools.compiler.field.type.FieldType;
import io.spine.tools.compiler.field.type.FieldTypeFactory;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
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

    private final RejectionDeclaration declaration;
    private final File outputDirectory;

    private final FieldTypeFactory fieldTypeFactory;
    private final RejectionJavadoc javadoc;
    private final RejectionBuilder builder;

    /**
     * Creates a new instance.
     *
     * @param metadata
     *         a rejection metadata
     * @param outputDirectory
     *         a directory to write a Rejection
     * @param messageTypeMap
     *         pre-scanned map with proto types and their appropriate Java classes
     */
    public RejectionWriter(RejectionDeclaration metadata,
                           File outputDirectory,
                           Map<String, String> messageTypeMap) {
        this.declaration = metadata;
        this.outputDirectory = outputDirectory;
        this.fieldTypeFactory = new FieldTypeFactory(metadata.getMessage(), messageTypeMap);
        this.javadoc = new RejectionJavadoc(metadata);
        this.builder = new RejectionBuilder(metadata, fieldDeclarations());
    }

    /**
     * Initiates writing.
     */
    public void write() {
        try {
            Logger log = log();
            log.debug("Creating the output directory {}", outputDirectory.getPath());
            Files.createDirectories(outputDirectory.toPath());

            String className = declaration.getSimpleJavaClassName()
                                          .value();
            log.debug("Constructing class {}", className);
            TypeSpec rejection =
                    TypeSpec.classBuilder(className)
                            .addJavadoc(javadoc.forClass())
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
                    JavaFile.builder(declaration.getJavaPackage()
                                                .toString(),
                                     rejection)
                            .skipJavaLangImports(true)
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
                    declaration.getSimpleJavaClassName());
        ParameterSpec builderParameter = builder.asParameter();
        CodeBlock buildRejectionMessage = builder.buildRejectionMessage();
        return constructorBuilder()
                .addJavadoc(javadoc.forConstructor())
                .addModifiers(PRIVATE)
                .addParameter(builderParameter)
                .addStatement("super($L)", buildRejectionMessage.toString())
                .build();
    }

    private MethodSpec getMessageThrown() {
        String methodSignature = getMessageThrown.signature();
        log().debug("Constructing method {}", methodSignature);

        TypeName returnType =
                ClassName.get(declaration.getJavaPackage()
                                         .value(),
                              declaration.getOuterJavaClass()
                                         .value())
                         .nestedClass(declaration.getSimpleJavaClassName()
                                                 .value());
        return MethodSpec.methodBuilder(getMessageThrown.name())
                         .addAnnotation(Override.class)
                         .addModifiers(PUBLIC)
                         .returns(returnType)
                         .addStatement("return (" + returnType + ") super." + methodSignature)
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

    /**
     * Reads all descriptor fields.
     *
     * @return name-to-{@link FieldType} map
     */
    private Map<String, FieldType> fieldDeclarations() {
        Logger log = log();
        log.debug("Reading all the field values from the descriptor: {}", declaration.getMessage());

        Map<String, FieldType> result = Maps.newLinkedHashMap();
        for (FieldDescriptorProto field : declaration.getMessage()
                                                     .getFieldList()) {
            result.put(field.getName(), fieldTypeFactory.create(field));
        }
        log.debug("Read fields: {}", result);

        return result;
    }
}
