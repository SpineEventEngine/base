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

import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import io.spine.base.ThrowableMessage;
import io.spine.code.Indent;
import io.spine.code.proto.MessageDocumentation;
import io.spine.code.proto.RejectionDeclaration;
import io.spine.logging.Logging;
import io.spine.tools.compiler.field.FieldDeclaration;
import io.spine.tools.compiler.field.type.FieldType;
import io.spine.tools.compiler.field.type.FieldTypeFactory;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static io.spine.tools.compiler.annotation.Annotations.generatedBySpineModelCompiler;
import static io.spine.tools.compiler.rejection.FormattedCodeBlock.lineSeparator;
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
    private final MessageDocumentation documentation;
    private final RejectionBuilder builder;
    private final Indent indent;

    /**
     * Creates a new instance.
     *
     * @param metadata
     *         a rejection metadata
     * @param outputDirectory
     *         a directory to write a Rejection
     * @param messageTypeMap
     *         the Proto-to-Java names map
     * @param indent
     *         indentation for the generated code
     */
    public RejectionWriter(RejectionDeclaration metadata,
                           File outputDirectory,
                           Map<String, String> messageTypeMap,
                           Indent indent) {
        this.documentation = new MessageDocumentation(metadata);
        this.declaration = metadata;
        this.outputDirectory = outputDirectory;
        this.fieldTypeFactory = new FieldTypeFactory(metadata.getMessage(), messageTypeMap);
        this.builder = new RejectionBuilder(new GeneratedRejectionDeclaration(metadata),
                                            fieldDeclarations(documentation));
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

            String className = declaration.getSimpleJavaClassName()
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
                    JavaFile.builder(declaration.getJavaPackage()
                                                .toString(),
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
                    declaration.getSimpleJavaClassName());
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

        ClassName returnType = new GeneratedRejectionDeclaration(declaration).rejectionMessage();
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
        CodeBlock.Builder docBuilder = CodeBlock.builder();
        Optional<String> leadingComments = documentation.leadingComments();

        leadingComments.ifPresent(s -> docBuilder.add(FormattedCodeBlock.from(s)
                                                                        .asJavadoc())
                                                 .add(lineSeparator()));

        docBuilder.add("Rejection based on proto type ")
                  .add("{@code $L.$L}",
                       declaration.getJavaPackage(), declaration.getSimpleJavaClassName())
                  .add(lineSeparator());
        return docBuilder.build();
    }

    /**
     * Generates a Javadoc content for the rejection constructor.
     *
     * @param builderParameter
     *         the name of a rejection builder parameter
     * @return the constructor Javadoc content
     */
    private static CodeBlock constructorJavadoc(ParameterSpec builderParameter) {
        return CodeBlock.builder()
                        .add("Creates a new instance.")
                        .add(lineSeparator())
                        .add(lineSeparator())
                        .add("@param $N the builder for the rejection", builderParameter)
                        .add(lineSeparator())
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
     * @param documentation
     *         the documentation for the rejection
     * @return field declarations with the order as in a {@code .proto} file
     */
    private List<FieldDeclaration> fieldDeclarations(MessageDocumentation documentation) {
        Logger log = log();
        log.debug("Reading all the field values from the descriptor: {}", declaration.getMessage());
        List<FieldDeclaration> result = newArrayList();
        for (FieldDescriptorProto field : declaration.getMessage()
                                                     .getFieldList()) {
            FieldType type = fieldTypeFactory.create(field);
            Optional<String> leadingComments = documentation.getFieldLeadingComments(field);
            FieldDeclaration declaration = new FieldDeclaration(field, type, leadingComments);
            result.add(declaration);
        }
        log.debug("Read fields: {}", result);
        return result;
    }
}
