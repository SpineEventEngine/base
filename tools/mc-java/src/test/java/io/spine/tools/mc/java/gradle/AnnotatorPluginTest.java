/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.tools.mc.java.gradle;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
import io.spine.annotation.Internal;
import io.spine.annotation.SPI;
import io.spine.code.proto.FileName;
import io.spine.code.proto.FileSet;
import io.spine.testing.TempDir;
import io.spine.tools.debug.DebugBox;
import io.spine.tools.gradle.testing.GradleProject;
import io.spine.tools.java.fs.DefaultJavaPaths;
import io.spine.tools.java.fs.SourceFile;
import io.spine.tools.mc.java.annotation.check.FieldAnnotationCheck;
import io.spine.tools.mc.java.annotation.check.MainDefinitionAnnotationCheck;
import io.spine.tools.mc.java.annotation.check.NestedTypeFieldsAnnotationCheck;
import io.spine.tools.mc.java.annotation.check.NestedTypesAnnotationCheck;
import io.spine.tools.mc.java.annotation.check.SourceCheck;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.impl.AbstractJavaSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static io.spine.code.proto.FileDescriptors.DESC_EXTENSION;
import static io.spine.tools.gradle.JavaTaskName.compileJava;
import static io.spine.tools.java.fs.SourceFile.forMessage;
import static io.spine.tools.java.fs.SourceFile.forOuterClassOf;
import static io.spine.tools.java.fs.SourceFile.forService;
import static io.spine.tools.mc.java.annotation.given.GivenProtoFile.INTERNAL_ALL;
import static io.spine.tools.mc.java.annotation.given.GivenProtoFile.INTERNAL_ALL_MULTIPLE;
import static io.spine.tools.mc.java.annotation.given.GivenProtoFile.INTERNAL_ALL_SERVICE;
import static io.spine.tools.mc.java.annotation.given.GivenProtoFile.INTERNAL_FIELD;
import static io.spine.tools.mc.java.annotation.given.GivenProtoFile.INTERNAL_FIELD_MULTIPLE;
import static io.spine.tools.mc.java.annotation.given.GivenProtoFile.INTERNAL_MESSAGE;
import static io.spine.tools.mc.java.annotation.given.GivenProtoFile.INTERNAL_MESSAGE_MULTIPLE;
import static io.spine.tools.mc.java.annotation.given.GivenProtoFile.NO_INTERNAL_OPTIONS;
import static io.spine.tools.mc.java.annotation.given.GivenProtoFile.NO_INTERNAL_OPTIONS_MULTIPLE;
import static io.spine.tools.mc.java.annotation.given.GivenProtoFile.POTENTIAL_ANNOTATION_DUP;
import static io.spine.tools.mc.java.annotation.given.GivenProtoFile.SPI_SERVICE;
import static io.spine.tools.mc.java.gradle.McJavaTaskName.annotateProto;

@SuppressWarnings("MethodOnlyUsedFromInnerClass")
@DisplayName("`AnnotatorPlugin` should")
class AnnotatorPluginTest {

    private static final String PROJECT_NAME = "annotator-plugin-test";

    private File testProjectDir;

    @BeforeEach
    void createTempDir() {
        testProjectDir = TempDir.forClass(getClass());
    }

    @Nested
    @DisplayName("annotate")
    class Annotating {

        @Test
        @DisplayName("if file option is `true`")
        void ifFileOptionSet() throws IOException {
            checkNestedTypesAnnotations(INTERNAL_ALL, true);
        }

        @Test
        @DisplayName("a gRCP service if file option if `true`")
        void serviceIfFileOptionSet() throws IOException {
            checkServiceAnnotations(INTERNAL_ALL_SERVICE, true);
        }

        @Test
        @DisplayName("multiple files if file option is `true`")
        void multipleFilesIfFileOptionSet() throws IOException {
            checkMainDefinitionAnnotations(INTERNAL_ALL_MULTIPLE, true);
        }

        @Test
        @DisplayName("if message option is `true`")
        void ifMessageOptionSet() throws IOException {
            checkNestedTypesAnnotations(INTERNAL_MESSAGE, true);
        }

        @Test
        @DisplayName("multiple files if message option is `true`")
        void multipleFiles() throws IOException {
            checkMainDefinitionAnnotations(INTERNAL_MESSAGE_MULTIPLE, true);
        }

        @Test
        @DisplayName("accessors if field option is true")
        void accessors() throws IOException {
            checkFieldAnnotations(INTERNAL_FIELD, true);
        }

        @Test
        @DisplayName("accessors in multiple files if field option is `true`")
        void accessorsInMultipleFiles() throws IOException {
            checkFieldAnnotationsMultiple(INTERNAL_FIELD_MULTIPLE, true);
        }

        @Test
        @DisplayName("a gRPC service if service option is `true`")
        void grpcServices() throws IOException {
            checkServiceAnnotations(SPI_SERVICE, SPI.class, true);
        }
    }

    @Nested
    @DisplayName("not annotate")
    class NotAnnotating {

        @Test
        @DisplayName("if file option if `false`")
        void ifFileOption() throws IOException {
            checkNestedTypesAnnotations(NO_INTERNAL_OPTIONS, false);
        }

        @Test
        @DisplayName("service if file option is `false`")
        void serviceIfFileOption() throws IOException {
            checkNestedTypesAnnotations(NO_INTERNAL_OPTIONS, false);
        }

        @Test
        @DisplayName("multiple files if file option is `false`")
        void multipleFilesIfFileOption() throws IOException {
            checkMainDefinitionAnnotations(NO_INTERNAL_OPTIONS_MULTIPLE, false);
        }

        @Test
        @DisplayName("if message option is false")
        void ifMessageOption() throws IOException {
            checkNestedTypesAnnotations(NO_INTERNAL_OPTIONS, false);
        }

        @Test
        @DisplayName("multiple files if message option is `false`")
        void multipleFiles() throws IOException {
            checkMainDefinitionAnnotations(NO_INTERNAL_OPTIONS_MULTIPLE, false);
        }

        @Test
        @DisplayName("accessors if field option is `false`")
        void accessors() throws IOException {
            checkFieldAnnotations(NO_INTERNAL_OPTIONS, false);
        }

        @Test
        @DisplayName("accessors in multiple files if field option is `false`")
        void accessorsInMultipleFiles() throws IOException {
            checkFieldAnnotationsMultiple(NO_INTERNAL_OPTIONS_MULTIPLE, false);
        }

        @Test
        @DisplayName("GRPC services if service option is `false`")
        void gprcServices() throws IOException {
            checkServiceAnnotations(NO_INTERNAL_OPTIONS, false);
        }
    }

    @Test
    @DisplayName("compile generated source with potential annotation duplication")
    void compilingSources() {
        newProjectWithFile(POTENTIAL_ANNOTATION_DUP).executeTask(compileJava);
    }

    private void checkServiceAnnotations(FileName testFile, boolean shouldBeAnnotated)
            throws IOException {
        checkServiceAnnotations(testFile, Internal.class, shouldBeAnnotated);
    }

    private void checkServiceAnnotations(FileName testFile,
                                         Class<? extends Annotation> expectedAnnotation,
                                         boolean shouldBeAnnotated)
            throws IOException {
        FileDescriptor fileDescriptor = compileAndAnnotate(testFile);
        List<ServiceDescriptor> services = fileDescriptor.getServices();
        for (ServiceDescriptor serviceDescriptor : services) {
            SourceFile serviceFile =
                    forService(serviceDescriptor.toProto(), fileDescriptor.toProto());
            SourceCheck check =
                    new MainDefinitionAnnotationCheck(expectedAnnotation, shouldBeAnnotated);
            checkGrpcService(serviceFile, check);
        }
    }

    private void checkFieldAnnotations(FileName testFile, boolean shouldBeAnnotated)
            throws IOException {
        FileDescriptor fileDescriptor = compileAndAnnotate(testFile);
        Descriptor messageDescriptor = fileDescriptor.getMessageTypes().get(0);
        Path sourcePath = forMessage(messageDescriptor.toProto(), fileDescriptor.toProto()).path();
        NestedTypeFieldsAnnotationCheck check =
                new NestedTypeFieldsAnnotationCheck(messageDescriptor, shouldBeAnnotated);
        check(sourcePath, check);
    }

    private void checkFieldAnnotationsMultiple(FileName testFile, boolean shouldBeAnnotated)
            throws IOException {
        FileDescriptor fileDescriptor = compileAndAnnotate(testFile);
        Descriptor messageDescriptor = fileDescriptor.getMessageTypes()
                                                     .get(0);
        FieldDescriptor experimentalField = messageDescriptor.getFields().get(0);
        Path sourcePath = forMessage(messageDescriptor.toProto(), fileDescriptor.toProto()).path();
        check(sourcePath, new FieldAnnotationCheck(experimentalField, shouldBeAnnotated));
    }

    private void checkMainDefinitionAnnotations(FileName testFile, boolean shouldBeAnnotated)
            throws IOException {
        FileDescriptor fileDescriptor = compileAndAnnotate(testFile);
        for (Descriptor messageDescriptor : fileDescriptor.getMessageTypes()) {
            DescriptorProto messageProto = messageDescriptor.toProto();
            DescriptorProtos.FileDescriptorProto fileProto = fileDescriptor.toProto();
            Path messagePath = forMessage(messageProto, fileProto).path();
            SourceCheck annotationCheck = new MainDefinitionAnnotationCheck(shouldBeAnnotated);
            check(messagePath, annotationCheck);
        }
    }

    private void checkNestedTypesAnnotations(FileName testFile, boolean shouldBeAnnotated)
            throws IOException {
        FileDescriptor fileDescriptor = compileAndAnnotate(testFile);
        Path sourcePath = forOuterClassOf(fileDescriptor.toProto()).path();
        check(sourcePath, new NestedTypesAnnotationCheck(shouldBeAnnotated));
    }

    private void check(Path sourcePath, SourceCheck check) throws IOException {
        Path filePath = DefaultJavaPaths.at(testProjectDir)
                                        .generated()
                                        .mainJava()
                                        .resolve(sourcePath);
        @SuppressWarnings("unchecked")
        AbstractJavaSource<JavaClassSource> javaSource =
                Roaster.parse(AbstractJavaSource.class, filePath.toFile());
        check.accept(javaSource);
    }

    private void checkGrpcService(SourceFile serviceFile, SourceCheck check)
            throws IOException {
        Path fullPath = DefaultJavaPaths.at(testProjectDir)
                                        .generated()
                                        .mainGrpc()
                                        .resolve(serviceFile);
        @SuppressWarnings("unchecked")
        AbstractJavaSource<JavaClassSource> javaSource =
                Roaster.parse(AbstractJavaSource.class, fullPath.toFile());
        check.accept(javaSource);
    }

    /*
     * Test environment setup
     ************************************/

    private GradleProject newProjectWithFile(FileName protoFileName) {
        return GradleProject.newBuilder()
                            .setProjectName(PROJECT_NAME)
                            .setProjectFolder(testProjectDir)
                            .addProtoFile(protoFileName.value())
                            .build();
    }

    private FileDescriptor compileAndAnnotate(FileName testFile) {
        GradleProject gradleProject = newProjectWithFile(testFile);
        gradleProject.executeTask(annotateProto);
        FileDescriptor result = getDescriptor(testFile);
        return result;
    }

    private FileDescriptor getDescriptor(FileName fileName) {
        Path mainDescriptor = DefaultJavaPaths
                .at(testProjectDir)
                .buildRoot()
                .descriptors()
                .mainDescriptors()
                .resolve("io.spine.test_" + testProjectDir.getName() + "_3.14" + DESC_EXTENSION);
        FileSet fileSet = FileSet.parse(mainDescriptor.toFile());
        Optional<FileDescriptor> file = fileSet.tryFind(fileName);
        checkState(file.isPresent(), "Unable to get file descriptor for `%s`.", fileName);
        return file.get();
    }
}
