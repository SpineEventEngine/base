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

package io.spine.gradle.compiler.annotation;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;
import io.spine.gradle.GradleProject;
import io.spine.gradle.compiler.annotation.given.Given.FieldAnnotationValidator;
import io.spine.gradle.compiler.annotation.given.Given.MainDefinitionAnnotationValidator;
import io.spine.gradle.compiler.annotation.given.Given.NestedTypeFieldsAnnotationValidator;
import io.spine.gradle.compiler.annotation.given.Given.NestedTypesAnnotationValidator;
import io.spine.gradle.compiler.annotation.given.Given.SourceValidator;
import io.spine.tools.java.CodePaths;
import io.spine.tools.proto.FileDescriptors;
import io.spine.util.Exceptions;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.impl.AbstractJavaSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.gradle.TaskName.ANNOTATE_PROTO;
import static io.spine.gradle.TaskName.COMPILE_JAVA;
import static io.spine.gradle.compiler.Extension.getDefaultMainDescriptorsPath;
import static io.spine.gradle.compiler.Extension.getDefaultMainGenDir;
import static io.spine.gradle.compiler.Extension.getDefaultMainGenGrpcDir;
import static io.spine.gradle.compiler.annotation.given.Given.NO_SPI_OPTIONS_FILENAME;
import static io.spine.gradle.compiler.annotation.given.Given.NO_SPI_OPTIONS_MULTIPLE_FILENAME;
import static io.spine.tools.java.CodePaths.getFile;
import static io.spine.util.Exceptions.newIllegalStateException;
import static java.nio.file.Paths.get;

/**
 * @author Dmytro Grankin
 */
public class ProtoAnnotatorPluginShould {

    private static final String PROJECT_NAME = "annotator-plugin-test";

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();

    @Test
    public void annotate_if_file_option_is_true() {
        final String testFile = "spi_all.proto";
        assertNestedTypesAnnotations(testFile, true);
    }

    @Test
    public void annotate_service_if_file_option_is_true() {
        final String testFile = "spi_all_service.proto";
        assertServiceAnnotations(testFile, true);
    }

    @Test
    public void not_annotate_if_file_option_if_false() {
        assertNestedTypesAnnotations(NO_SPI_OPTIONS_FILENAME, false);
    }

    @Test
    public void not_annotate_service_if_file_option_if_false() {
        assertNestedTypesAnnotations(NO_SPI_OPTIONS_FILENAME, false);
    }

    @Test
    public void annotate_multiple_files_if_file_option_is_true() {
        final String testFile = "spi_all_multiple.proto";
        assertMainDefinitionAnnotations(testFile, true);
    }

    @Test
    public void not_annotate_multiple_files_if_file_option_is_false() {
        assertMainDefinitionAnnotations(NO_SPI_OPTIONS_MULTIPLE_FILENAME, false);
    }

    @Test
    public void annotate_if_message_option_is_true() {
        final String testFile = "spi_message.proto";
        assertNestedTypesAnnotations(testFile, true);
    }

    @Test
    public void not_annotate_if_message_option_is_false() {
        assertNestedTypesAnnotations(NO_SPI_OPTIONS_FILENAME, false);
    }

    @Test
    public void annotate_multiple_files_if_message_option_is_true() {
        final String testFile = "spi_message_multiple.proto";
        assertMainDefinitionAnnotations(testFile, true);
    }

    @Test
    public void not_annotate_multiple_files_if_message_option_is_false() {
        assertMainDefinitionAnnotations(NO_SPI_OPTIONS_MULTIPLE_FILENAME, false);
    }

    @Test
    public void annotate_accessors_if_field_option_is_true() {
        final String testFile = "spi_field.proto";
        assertFieldAnnotations(testFile, true);
    }

    @Test
    public void not_annotate_accessors_if_field_option_is_false() {
        assertFieldAnnotations(NO_SPI_OPTIONS_FILENAME, false);
    }

    @Test
    public void annotate_accessors_in_multiple_files_if_field_option_is_true() {
        final String testFile = "spi_field_multiple.proto";
        assertFieldAnnotationsMultiple(testFile, true);
    }

    @Test
    public void not_annotate_accessors_in_multiple_files_if_field_option_is_false() {
        assertFieldAnnotationsMultiple(NO_SPI_OPTIONS_MULTIPLE_FILENAME, false);
    }

    @Test
    public void annotate_grpc_services_if_service_option_is_true() {
        final String testFile = "spi_service.proto";
        assertServiceAnnotations(testFile, true);
    }

    @Test
    public void not_annotate_grpc_services_if_service_option_is_false() {
        assertServiceAnnotations(NO_SPI_OPTIONS_FILENAME, false);
    }

    @Test
    public void compile_generated_sources_with_potential_annotation_duplication() {
        final String file = "potential_annotation_duplication.proto";
        newProjectWithFile(file).executeTask(COMPILE_JAVA);
    }

    private void assertServiceAnnotations(final String testFile,
                                          final boolean shouldBeAnnotated) {
        newProjectWithFile(testFile).executeTask(ANNOTATE_PROTO);

        final FileDescriptorProto fileDescriptor = getDescriptor(testFile);
        final List<ServiceDescriptorProto> services = fileDescriptor.getServiceList();
        for (ServiceDescriptorProto serviceDescriptor : services) {
            final Path messagePath = getFile(serviceDescriptor, fileDescriptor);
            validateGrpcService(messagePath,
                                new MainDefinitionAnnotationValidator(shouldBeAnnotated));
        }
    }

    private void assertFieldAnnotations(final String testFile,
                                        final boolean shouldBeAnnotated) {
        newProjectWithFile(testFile).executeTask(ANNOTATE_PROTO);

        final FileDescriptorProto fileDescriptor = getDescriptor(testFile);
        final DescriptorProto messageDescriptor = fileDescriptor.getMessageType(0);
        final Path sourcePath = CodePaths.getFile(messageDescriptor, false,
                                                  fileDescriptor);
        final NestedTypeFieldsAnnotationValidator validator =
                new NestedTypeFieldsAnnotationValidator(messageDescriptor,
                                                        shouldBeAnnotated);
        validate(sourcePath, validator);
    }

    private void assertFieldAnnotationsMultiple(final String testFile,
                                                final boolean shouldBeAnnotated) {
        newProjectWithFile(testFile).executeTask(ANNOTATE_PROTO);

        final FileDescriptorProto fileDescriptor = getDescriptor(testFile);
        final DescriptorProto messageDescriptor = fileDescriptor.getMessageType(0);
        final FieldDescriptorProto experimentalField = messageDescriptor.getField(0);
        final Path sourcePath = CodePaths.getFile(messageDescriptor, false, fileDescriptor);
        validate(sourcePath, new FieldAnnotationValidator(experimentalField, shouldBeAnnotated));
    }

    private void assertMainDefinitionAnnotations(final String testFile,
                                                 final boolean shouldBeAnnotated) {
        newProjectWithFile(testFile).executeTask(ANNOTATE_PROTO);

        final FileDescriptorProto fileDescriptor = getDescriptor(testFile);
        for (DescriptorProto messageDescriptor : fileDescriptor.getMessageTypeList()) {
            final Path messagePath =
                    CodePaths.getFile(messageDescriptor, false, fileDescriptor);
            final Path messageOrBuilderPath =
                    CodePaths.getFile(messageDescriptor, true, fileDescriptor);
            final SourceValidator annotationValidator =
                    new MainDefinitionAnnotationValidator(shouldBeAnnotated);
            validate(messagePath, annotationValidator);
            validate(messageOrBuilderPath, annotationValidator);
        }
    }

    private void assertNestedTypesAnnotations(final String testFile,
                                              final boolean shouldBeAnnotated) {
        newProjectWithFile(testFile).executeTask(ANNOTATE_PROTO);

        final FileDescriptorProto fileDescriptor = getDescriptor(testFile);
        final Path sourcePath = CodePaths.getFile(fileDescriptor);
        validate(sourcePath, new NestedTypesAnnotationValidator(shouldBeAnnotated));
    }

    @SuppressWarnings("unchecked")
    private void validate(Path sourcePath, SourceValidator validator) {
        final String projectPath = testProjectDir.getRoot()
                                                 .getAbsolutePath();
        final Path fullSourcePath = get(projectPath, getDefaultMainGenDir(), sourcePath.toString());

        final AbstractJavaSource<JavaClassSource> javaSource;
        try {
            javaSource = Roaster.parse(AbstractJavaSource.class, fullSourcePath.toFile());
        } catch (FileNotFoundException e) {
            throw Exceptions.illegalStateWithCauseOf(e);
        }
        validator.apply(javaSource);
    }

    @SuppressWarnings("unchecked")
    private void validateGrpcService(Path servicePath, SourceValidator validator) {
        final String projectPath = testProjectDir.getRoot()
                                                 .getAbsolutePath();
        final Path fullSourcePath = get(projectPath, getDefaultMainGenGrpcDir(),
                                        servicePath.toString());
        final AbstractJavaSource<JavaClassSource> javaSource;
        try {
            javaSource = Roaster.parse(AbstractJavaSource.class, fullSourcePath.toFile());
        } catch (FileNotFoundException e) {
            throw Exceptions.illegalStateWithCauseOf(e);
        }
        validator.apply(javaSource);
    }

    private FileDescriptorProto getDescriptor(final String fileName) {
        final String projectPath = testProjectDir.getRoot()
                                                 .getAbsolutePath();
        final String descriptorSetPath = projectPath + getDefaultMainDescriptorsPath();
        final Collection<FileDescriptorProto> descriptors =
                FileDescriptors.parseAndFilter(descriptorSetPath, new Predicate<FileDescriptorProto>() {
                    @Override
                    public boolean apply(@Nullable FileDescriptorProto input) {
                        checkNotNull(input);
                        return input.getName()
                                    .equals(fileName);
                    }
                });
        if (descriptors.isEmpty() || descriptors.size() > 1) {
            throw newIllegalStateException("Could not get file descriptor for file `%s`.",
                                           fileName);
        }

        final FileDescriptorProto result = Iterables.get(descriptors, 0);
        return result;
    }

    private GradleProject newProjectWithFile(String protoFileName) {
        return GradleProject.newBuilder()
                            .setProjectName(PROJECT_NAME)
                            .setProjectFolder(testProjectDir)
                            .addProtoFile(protoFileName)
                            .build();
    }
}
