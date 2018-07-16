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
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.spine.base.ThrowableMessage;
import io.spine.tools.compiler.fieldtype.FieldType;
import io.spine.tools.compiler.fieldtype.FieldTypeFactory;
import io.spine.code.proto.FieldName;
import io.spine.code.proto.RejectionDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static io.spine.code.java.Annotations.generatedBySpineModelCompiler;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Generates Java code for a rejection based on its Protobuf descriptor.
 *
 * @author Mikhail Mikhaylov
 * @author Alexander Yevsyukov
 * @author Alexander Litus
 * @author Alex Tymchenko
 */
public class RejectionWriter {

    private static final NoArgMethod getMessageThrown = new NoArgMethod("getMessageThrown");

    private final RejectionDeclaration declaration;
    private final File outputDirectory;

    private final FieldTypeFactory fieldTypeFactory;
    private final RejectionJavadoc javadoc;

    /**
     * Creates a new instance.
     *
     * @param metadata        a rejection metadata
     * @param outputDirectory a directory to write a Rejection
     * @param messageTypeMap  pre-scanned map with proto types and their appropriate Java classes
     */
    public RejectionWriter(RejectionDeclaration metadata,
                           File outputDirectory,
                           Map<String, String> messageTypeMap) {
        this.declaration = metadata;
        this.outputDirectory = outputDirectory;
        this.fieldTypeFactory = new FieldTypeFactory(metadata.getMessage(), messageTypeMap);
        this.javadoc = new RejectionJavadoc(metadata);
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
        MethodSpec.Builder builder = constructorBuilder()
                .addJavadoc(javadoc.forConstructor())
                .addModifiers(PUBLIC);
        for (Map.Entry<String, FieldType> field : fieldDeclarations().entrySet()) {
            TypeName parameterType = field.getValue()
                                          .getTypeName();
            String parameterName = FieldName.of(field.getKey())
                                            .javaCase();
            builder.addParameter(parameterType, parameterName);
        }

        return builder.addStatement(superStatement())
                      .build();
    }

    private String superStatement() {
        StringBuilder superStatement = new StringBuilder("super(");
        superStatement.append(declaration.getOuterJavaClass())
                      .append('.')
                      .append(declaration.getSimpleJavaClassName())
                      .append(".newBuilder()");

        for (Map.Entry<String, FieldType> field : fieldDeclarations().entrySet()) {
            FieldName fieldName = FieldName.of(field.getKey());
            superStatement.append('.')
                          .append(field.getValue()
                                       .getSetterPrefix())
                          .append(fieldName.toCamelCase())
                          .append('(')
                          .append(fieldName.javaCase())
                          .append(')');
        }
        superStatement.append(".build())");

        return superStatement.toString();
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

    private static Logger log() {
        return LoggerSingleton.INSTANCE.logger;
    }

    private enum LoggerSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger logger = LoggerFactory.getLogger(RejectionWriter.class);
    }
}
