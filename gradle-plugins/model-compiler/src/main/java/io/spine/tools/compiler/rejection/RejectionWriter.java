/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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
import io.spine.gradle.compiler.message.fieldtype.FieldType;
import io.spine.gradle.compiler.message.fieldtype.FieldTypeFactory;
import io.spine.tools.proto.RejectionDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static io.spine.tools.java.Annotations.generatedBySpineModelCompiler;
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
@SuppressWarnings("HardcodedLineSeparator")
public class RejectionWriter {

    private final RejectionDeclaration declaration;
    private final File outputDirectory;

    private final FieldTypeFactory fieldTypeFactory;
    private final RejectionJavadocGenerator javadocGenerator;

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
        this.fieldTypeFactory = new FieldTypeFactory(metadata.getDescriptor(), messageTypeMap);
        this.javadocGenerator = new RejectionJavadocGenerator(metadata);
    }

    /**
     * Initiates writing.
     */
    public void write() {
        try {
            final Logger log = log();
            log.debug("Creating the output directory {}", outputDirectory.getPath());
            Files.createDirectories(outputDirectory.toPath());

            final String className = declaration.getClassName();
            log.debug("Constructing {}", className);
            final TypeSpec rejection =
                    TypeSpec.classBuilder(className)
                            .addJavadoc(javadocGenerator.generateClassJavadoc())
                            .addAnnotation(generatedBySpineModelCompiler())
                            .addModifiers(PUBLIC)
                            .superclass(ThrowableMessage.class)
                            .addField(constructSerialVersionUID())
                            .addMethod(constructConstructor())
                            .addMethod(constructGetMessageThrown())
                            .build();
            final JavaFile javaFile =
                    JavaFile.builder(declaration.getJavaPackage(), rejection)
                            .skipJavaLangImports(true)
                            .build();
            log.debug("Writing {}", className);
            javaFile.writeTo(outputDirectory);
            log.debug("Rejection {} written successfully", className);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private MethodSpec constructConstructor() {
        log().trace("Constructing the constructor of type '{}'", declaration.getDescriptor()
                                                                            .getName());
        final MethodSpec.Builder builder = constructorBuilder()
                .addJavadoc(javadocGenerator.generateConstructorJavadoc())
                .addModifiers(PUBLIC);
        for (Map.Entry<String, FieldType> field : readFieldValues().entrySet()) {
            final TypeName parameterTypeName = field.getValue()
                                                    .getTypeName();
            final String parameterName = getJavaFieldName(field.getKey());
            builder.addParameter(parameterTypeName, parameterName);
        }

        return builder.addStatement(getSuperStatement())
                      .build();
    }

    private String getSuperStatement() {
        final StringBuilder superStatement = new StringBuilder("super(");
        superStatement.append(declaration.getOuterClassName())
                      .append('.')
                      .append(declaration.getClassName())
                      .append(".newBuilder()");

        for (Map.Entry<String, FieldType> field : readFieldValues().entrySet()) {
            final String upperCaseName = getJavaFieldCapitalizedName(field.getKey());
            superStatement.append('.')
                          .append(field.getValue()
                                       .getSetterPrefix())
                          .append(upperCaseName)
                          .append('(')
                          .append(getJavaFieldName(field.getKey()))
                          .append(')');
        }
        superStatement.append(".build())");

        return superStatement.toString();
    }

    private MethodSpec constructGetMessageThrown() {
        log().trace("Constructing getMessageThrown()");

        final TypeName returnTypeName = ClassName.get(declaration.getOuterClassName(),
                                                      declaration.getClassName());
        return MethodSpec.methodBuilder("getMessageThrown")
                         .addAnnotation(Override.class)
                         .addModifiers(PUBLIC)
                         .returns(returnTypeName)
                         .addStatement("return (" + returnTypeName + ") super.getMessageThrown()")
                         .build();
    }

    private static FieldSpec constructSerialVersionUID() {
        return FieldSpec.builder(long.class, "serialVersionUID", PRIVATE, STATIC, FINAL)
                        .initializer("0L")
                        .build();
    }

    /**
     * Transforms Protobuf-style field name into corresponding Java-style field name.
     *
     * <p>For example, seat_assignment_id -> seatAssignmentId
     *
     * @param protoFieldName Protobuf field name.
     * @return a field name
     */
    private static String getJavaFieldName(String protoFieldName) {
        final String[] words = protoFieldName.split("_");
        final StringBuilder builder = new StringBuilder(words[0]);
        for (int i = 1; i < words.length; i++) {
            final String word = words[i];
            builder.append(Character.toUpperCase(word.charAt(0)))
                   .append(word.substring(1));
        }
        return builder.toString();
    }

    /**
     * Works like {@link #getJavaFieldName(String)}, but
     * additionally capitalizes the first letter.
     *
     * @param protoFieldName Protobuf field name.
     * @return a field name
     */
    private static String getJavaFieldCapitalizedName(String protoFieldName) {
        final String javaFieldName = getJavaFieldName(protoFieldName);
        return Character.toUpperCase(javaFieldName.charAt(0)) + javaFieldName.substring(1);
    }

    /**
     * Reads all descriptor fields.
     *
     * @return name-to-{@link FieldType} map
     */
    private Map<String, FieldType> readFieldValues() {
        log().trace("Reading all the field values from the descriptor: {}",
                    declaration.getDescriptor());

        final Map<String, FieldType> result = Maps.newLinkedHashMap();
        for (FieldDescriptorProto field : declaration.getDescriptor()
                                                     .getFieldList()) {
            result.put(field.getName(), fieldTypeFactory.create(field));
        }
        log().trace("Read fields: {}", result);

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
