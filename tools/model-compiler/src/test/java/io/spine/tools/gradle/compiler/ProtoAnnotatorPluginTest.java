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

package io.spine.tools.gradle.compiler;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
import io.spine.annotation.Internal;
import io.spine.annotation.SPI;
import io.spine.code.java.DefaultJavaProject;
import io.spine.code.java.SourceFile;
import io.spine.code.proto.FileName;
import io.spine.code.proto.FileSet;
import io.spine.tools.compiler.annotation.check.FieldAnnotationCheck;
import io.spine.tools.compiler.annotation.check.MainDefinitionAnnotationCheck;
import io.spine.tools.compiler.annotation.check.NestedTypeFieldsAnnotationCheck;
import io.spine.tools.compiler.annotation.check.NestedTypesAnnotationCheck;
import io.spine.tools.compiler.annotation.check.SourceCheck;
import io.spine.tools.gradle.testing.GradleProject;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.impl.AbstractJavaSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static io.spine.code.java.SourceFile.forMessage;
import static io.spine.code.java.SourceFile.forOuterClassOf;
import static io.spine.code.java.SourceFile.forService;
import static io.spine.tools.compiler.annotation.given.GivenProtoFile.INTERNAL_ALL;
import static io.spine.tools.compiler.annotation.given.GivenProtoFile.INTERNAL_ALL_MULTIPLE;
import static io.spine.tools.compiler.annotation.given.GivenProtoFile.INTERNAL_ALL_SERVICE;
import static io.spine.tools.compiler.annotation.given.GivenProtoFile.INTERNAL_FIELD;
import static io.spine.tools.compiler.annotation.given.GivenProtoFile.INTERNAL_FIELD_MULTIPLE;
import static io.spine.tools.compiler.annotation.given.GivenProtoFile.INTERNAL_MESSAGE;
import static io.spine.tools.compiler.annotation.given.GivenProtoFile.INTERNAL_MESSAGE_MULTIPLE;
import static io.spine.tools.compiler.annotation.given.GivenProtoFile.NO_INTERNAL_OPTIONS;
import static io.spine.tools.compiler.annotation.given.GivenProtoFile.NO_INTERNAL_OPTIONS_MULTIPLE;
import static io.spine.tools.compiler.annotation.given.GivenProtoFile.POTENTIAL_ANNOTATION_DUP;
import static io.spine.tools.compiler.annotation.given.GivenProtoFile.SPI_SERVICE;
import static io.spine.tools.gradle.TaskName.ANNOTATE_PROTO;
import static io.spine.tools.gradle.TaskName.COMPILE_JAVA;

@DisplayName("ProtoAnnotatorPlugin should")
@ExtendWith(TempDirectory.class)
class ProtoAnnotatorPluginTest {

    private static final String PROJECT_NAME = "annotator-plugin-test";

    private File testProjectDir;

    @BeforeEach
    void setUp(@TempDir Path tempDirPath) {
        testProjectDir = tempDirPath.toFile();
    }

    @Test
    @DisplayName("annotate if file option is true")
    void annotateIfFileOptionIsTrue() throws FileNotFoundException {
        assertNestedTypesAnnotations(INTERNAL_ALL, true);
    }

    @Test
    @DisplayName("annotate service if file option if true")
    void annotateServiceIfFileOptionIsTrue() throws FileNotFoundException {
        assertServiceAnnotations(INTERNAL_ALL_SERVICE, true);
    }

    @Test
    @DisplayName("not annotate if file option if false")
    void notAnnotateIfFileOptionIfFalse() throws FileNotFoundException {
        assertNestedTypesAnnotations(NO_INTERNAL_OPTIONS, false);
    }

    @Test
    @DisplayName("not annotate service if file option is false")
    void notAnnotateServiceIfFileOptionIfFalse() throws FileNotFoundException {
        assertNestedTypesAnnotations(NO_INTERNAL_OPTIONS, false);
    }

    @Test
    @DisplayName("annotate multiple files if file option is true")
    void annotateMultipleFilesIfFileOptionIsTrue() throws FileNotFoundException {
        assertMainDefinitionAnnotations(INTERNAL_ALL_MULTIPLE, true);
    }

    @Test
    @DisplayName("not annotate multiple files if file option is false")
    void notAnnotateMultipleFilesIfFileOptionIsFalse() throws FileNotFoundException {
        assertMainDefinitionAnnotations(NO_INTERNAL_OPTIONS_MULTIPLE, false);
    }

    @Test
    @DisplayName("annotate if message option is true")
    void annotateIfMessageOptionIsTrue() throws FileNotFoundException {
        assertNestedTypesAnnotations(INTERNAL_MESSAGE, true);
    }

    @Test
    @DisplayName("not annotate if message option is false")
    void notAnnotateIfMessageOptionIsFalse() throws FileNotFoundException {
        assertNestedTypesAnnotations(NO_INTERNAL_OPTIONS, false);
    }

    @Test
    @DisplayName("annotate multiple files if message option is true")
    void annotateMultipleFilesIfMessageOptionIsTrue() throws FileNotFoundException {
        assertMainDefinitionAnnotations(INTERNAL_MESSAGE_MULTIPLE, true);
    }

    @Test
    @DisplayName("not annotate multiple files if message option is false")
    void notAnnotateMultipleFilesIfMessageOptionIsFalse() throws FileNotFoundException {
        assertMainDefinitionAnnotations(NO_INTERNAL_OPTIONS_MULTIPLE, false);
    }

    @Test
    @DisplayName("annotate accessors if field option is true")
    void annotateAccessorsIfFieldOptionIsTrue() throws FileNotFoundException {
        assertFieldAnnotations(INTERNAL_FIELD, true);
    }

    @Test
    @DisplayName("not annotate accessors if field option is false")
    void notAnnotateAccessorsIfFieldOptionIsFalse() throws FileNotFoundException {
        assertFieldAnnotations(NO_INTERNAL_OPTIONS, false);
    }

    @Test
    @DisplayName("annotate accessors in multiple files if field option is true")
    void annotateAccessorsInMultipleFilesIfFieldOptionIsTrue()
            throws FileNotFoundException {
        assertFieldAnnotationsMultiple(INTERNAL_FIELD_MULTIPLE, true);
    }

    @Test
    @DisplayName("not annotate accessors in multiple files if field option is false")
    void notAnnotateAccessorsInMultipleFilesIfFieldOptionIsFalse()
            throws FileNotFoundException {
        assertFieldAnnotationsMultiple(NO_INTERNAL_OPTIONS_MULTIPLE, false);
    }

    @Test
    @DisplayName("annotate GRPC services if section option is true")
    void annotateGrpcServicesIfServiceOptionIsTrue() throws FileNotFoundException {
        assertServiceAnnotations(SPI_SERVICE, SPI.class, true);
    }

    @Test
    @DisplayName("not annotate GRPC services if service option is false")
    void notAnnotateGrpcServicesIfServiceOptionIsFalse() throws FileNotFoundException {
        assertServiceAnnotations(NO_INTERNAL_OPTIONS, false);
    }

    @Test
    @DisplayName("compile generated source with potential annotation duplication")
    void compileGeneratedSourcesWithPotentialAnnotationDuplication() {
        newProjectWithFile(POTENTIAL_ANNOTATION_DUP).executeTask(COMPILE_JAVA);
    }

    private void assertServiceAnnotations(FileName testFile, boolean shouldBeAnnotated)
            throws FileNotFoundException {
        assertServiceAnnotations(testFile, Internal.class, shouldBeAnnotated);
    }

    private void assertServiceAnnotations(FileName testFile,
                                          Class<? extends Annotation> expectedAnnotation,
                                          boolean shouldBeAnnotated)
            throws FileNotFoundException {
        FileDescriptor fileDescriptor = compileAndAnnotate(testFile);
        List<ServiceDescriptor> services = fileDescriptor.getServices();
        for (ServiceDescriptor serviceDescriptor : services) {
            SourceFile serviceFile = forService(serviceDescriptor.toProto(),
                                                fileDescriptor.toProto());
            SourceCheck check = new MainDefinitionAnnotationCheck(expectedAnnotation,
                                                                  shouldBeAnnotated);
            checkGrpcService(serviceFile, check);
        }
    }

    private void assertFieldAnnotations(FileName testFile, boolean shouldBeAnnotated)
            throws FileNotFoundException {
        FileDescriptor fileDescriptor = compileAndAnnotate(testFile);
        Descriptor messageDescriptor = fileDescriptor.getMessageTypes()
                                                     .get(0);
        Path sourcePath = forMessage(messageDescriptor.toProto(), fileDescriptor.toProto())
                .getPath();
        NestedTypeFieldsAnnotationCheck check =
                new NestedTypeFieldsAnnotationCheck(messageDescriptor, shouldBeAnnotated);
        check(sourcePath, check);
    }

    private void assertFieldAnnotationsMultiple(FileName testFile, boolean shouldBeAnnotated)
            throws FileNotFoundException {
        FileDescriptor fileDescriptor = compileAndAnnotate(testFile);
        Descriptor messageDescriptor = fileDescriptor.getMessageTypes()
                                                     .get(0);
        FieldDescriptor experimentalField = messageDescriptor.getFields()
                                                             .get(0);
        Path sourcePath = forMessage(messageDescriptor.toProto(), fileDescriptor.toProto())
                .getPath();
        check(sourcePath, new FieldAnnotationCheck(experimentalField, shouldBeAnnotated));
    }

    private void assertMainDefinitionAnnotations(FileName testFile, boolean shouldBeAnnotated)
            throws FileNotFoundException {
        FileDescriptor fileDescriptor = compileAndAnnotate(testFile);
        for (Descriptor messageDescriptor : fileDescriptor.getMessageTypes()) {
            DescriptorProto messageProto = messageDescriptor.toProto();
            DescriptorProtos.FileDescriptorProto fileProto = fileDescriptor.toProto();
            Path messagePath = forMessage(messageProto, fileProto).getPath();
            SourceCheck annotationCheck = new MainDefinitionAnnotationCheck(shouldBeAnnotated);
            check(messagePath, annotationCheck);
        }
    }

    private void assertNestedTypesAnnotations(FileName testFile, boolean shouldBeAnnotated)
            throws FileNotFoundException {
        FileDescriptor fileDescriptor = compileAndAnnotate(testFile);
        Path sourcePath = forOuterClassOf(fileDescriptor.toProto()).getPath();
        check(sourcePath, new NestedTypesAnnotationCheck(shouldBeAnnotated));
    }

    private void check(Path sourcePath, SourceCheck check) throws FileNotFoundException {
        Path filePath = DefaultJavaProject.at(testProjectDir)
                                          .generated()
                                          .mainJava()
                                          .resolve(sourcePath);
        @SuppressWarnings("unchecked")
        AbstractJavaSource<JavaClassSource> javaSource =
                Roaster.parse(AbstractJavaSource.class, filePath.toFile());
        check.accept(javaSource);
    }

    private void checkGrpcService(SourceFile serviceFile, SourceCheck check)
            throws FileNotFoundException {
        Path fullPath = DefaultJavaProject.at(testProjectDir)
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
        gradleProject.executeTask(ANNOTATE_PROTO);
        FileDescriptor result = getDescriptor(testFile);
        return result;
    }

    private FileDescriptor getDescriptor(FileName fileName) {
        Path mainDescriptor = DefaultJavaProject
                .at(testProjectDir)
                .buildRoot()
                .descriptors()
                .mainDescriptors()
                .resolve("io.spine.test_" + testProjectDir.getName() + "_3.14.desc");
        FileSet fileSet = FileSet.parse(mainDescriptor.toFile());
        Optional<FileDescriptor> file = fileSet.tryFind(fileName);
        checkState(file.isPresent(), "Unable to get file descriptor for %s", fileName);
        return file.get();
    }
}
