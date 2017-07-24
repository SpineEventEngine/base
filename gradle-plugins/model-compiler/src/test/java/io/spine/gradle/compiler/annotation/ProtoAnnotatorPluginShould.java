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
import io.spine.gradle.compiler.annotation.Given.FieldAnnotationValidator;
import io.spine.gradle.compiler.annotation.Given.MainDefinitionAnnotationValidator;
import io.spine.gradle.compiler.annotation.Given.NestedTypeFieldsAnnotationValidator;
import io.spine.gradle.compiler.annotation.Given.NestedTypesAnnotationValidator;
import io.spine.gradle.compiler.annotation.Given.ProtoAnnotatorConfigurator;
import io.spine.util.Exceptions;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.ResultHandler;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.impl.AbstractJavaSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.gradle.TaskName.ANNOTATE_PROTO;
import static io.spine.gradle.TaskName.COMPILE_JAVA;
import static io.spine.gradle.compiler.Extension.getDefaultMainDescriptorsPath;
import static io.spine.gradle.compiler.Extension.getDefaultMainGenDir;
import static io.spine.gradle.compiler.Extension.getDefaultMainGenGrpcDir;
import static io.spine.gradle.compiler.ProjectConfigurator.newEmptyResultHandler;
import static io.spine.gradle.compiler.annotation.Given.NO_SPI_OPTIONS_FILENAME;
import static io.spine.gradle.compiler.annotation.Given.NO_SPI_OPTIONS_MULTIPLE_FILENAME;
import static io.spine.gradle.compiler.util.DescriptorSetUtil.getProtoFileDescriptors;
import static io.spine.gradle.compiler.util.JavaSources.getFilePath;
import static io.spine.util.Exceptions.newIllegalStateException;
import static java.nio.file.Paths.get;

/**
 * @author Dmytro Grankin
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")  // It's fine for a test, running a Gradle build.
public class ProtoAnnotatorPluginShould {

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    @Test
    public void annotate_if_file_option_is_true() throws Exception {
        final String testFile = "spi_all.proto";
        assertNestedTypesAnnotations(testFile, true);
    }

    @Test
    public void annotate_service_if_file_option_is_true() throws Exception {
        final String testFile = "spi_all_service.proto";
        assertServiceAnnotations(testFile, true);
    }

    @Test
    public void not_annotate_if_file_option_if_false() throws Exception {
        assertNestedTypesAnnotations(NO_SPI_OPTIONS_FILENAME, false);
    }

    @Test
    public void not_annotate_service_if_file_option_if_false() throws Exception {
        assertNestedTypesAnnotations(NO_SPI_OPTIONS_FILENAME, false);
    }

    @Test
    public void annotate_multiple_files_if_file_option_is_true() throws Exception {
        final String testFile = "spi_all_multiple.proto";
        assertMainDefinitionAnnotations(testFile, true);
    }

    @Test
    public void not_annotate_multiple_files_if_file_option_is_false() throws Exception {
        assertMainDefinitionAnnotations(NO_SPI_OPTIONS_MULTIPLE_FILENAME, false);
    }

    @Test
    public void annotate_if_message_option_is_true() throws Exception {
        final String testFile = "spi_message.proto";
        assertNestedTypesAnnotations(testFile, true);
    }

    @Test
    public void not_annotate_if_message_option_is_false() throws Exception {
        assertNestedTypesAnnotations(NO_SPI_OPTIONS_FILENAME, false);
    }

    @Test
    public void annotate_multiple_files_if_message_option_is_true() throws Exception {
        final String testFile = "spi_message_multiple.proto";
        assertMainDefinitionAnnotations(testFile, true);
    }

    @Test
    public void not_annotate_multiple_files_if_message_option_is_false() throws Exception {
        assertMainDefinitionAnnotations(NO_SPI_OPTIONS_MULTIPLE_FILENAME, false);
    }

    @Test
    public void annotate_accessors_if_field_option_is_true() throws Exception {
        final String testFile = "spi_field.proto";
        assertFieldAnnotations(testFile, true);
    }

    @Test
    public void not_annotate_accessors_if_field_option_is_false() throws Exception {
        assertFieldAnnotations(NO_SPI_OPTIONS_FILENAME, false);
    }

    @Test
    public void annotate_accessors_in_multiple_files_if_field_option_is_true() throws Exception {
        final String testFile = "spi_field_multiple.proto";
        assertFieldAnnotationsMultiple(testFile, true);
    }

    @Test
    public void not_annotate_accessors_in_multiple_files_if_field_option_is_false() throws
                                                                                    Exception {
        assertFieldAnnotationsMultiple(NO_SPI_OPTIONS_MULTIPLE_FILENAME, false);
    }

    @Test
    public void annotate_grpc_services_if_service_option_is_true() throws Exception {
        final String testFile = "spi_service.proto";
        assertServiceAnnotations(testFile, true);
    }

    @Test
    public void not_annotate_grpc_services_if_service_option_is_false() throws Exception {
        assertServiceAnnotations(NO_SPI_OPTIONS_FILENAME, false);
    }

    @Test
    public void compile_generated_sources_with_potential_annotation_duplication() throws Exception {
        final String testFile = "potential_annotation_duplication.proto";
        final ProjectConnection connection =
                new ProtoAnnotatorConfigurator(testProjectDir, testFile).configure();
        final BuildLauncher launcher = connection.newBuild();

        launcher.setStandardError(System.out)
                .forTasks(COMPILE_JAVA.getValue());
        try {
            launcher.run(newEmptyResultHandler(countDownLatch));
        } finally {
            connection.close();
        }
        countDownLatch.await(100, TimeUnit.MILLISECONDS);
    }

    private void assertServiceAnnotations(final String testFile,
                                          final boolean shouldBeAnnotated) throws Exception {
        final ProjectConnection connection =
                new ProtoAnnotatorConfigurator(testProjectDir, testFile).configure();
        final BuildLauncher launcher = connection.newBuild();

        launcher.setStandardError(System.out)
                .forTasks(ANNOTATE_PROTO.getValue());
        try {
            launcher.run(new ResultHandler<Void>() {
                @Override
                public void onComplete(Void aVoid) {
                    final FileDescriptorProto fileDescriptor = getDescriptor(testFile);
                    final List<ServiceDescriptorProto> services = fileDescriptor.getServiceList();
                    for (ServiceDescriptorProto serviceDescriptor : services) {
                        final Path messagePath =
                                getFilePath(serviceDescriptor, fileDescriptor);
                        validateGrpcService(messagePath,
                                            new MainDefinitionAnnotationValidator(
                                                    shouldBeAnnotated));
                    }
                    countDownLatch.countDown();
                }

                @Override
                public void onFailure(GradleConnectionException e) {
                    throw e;
                }
            });
        } finally {
            connection.close();
        }
        countDownLatch.await(100, TimeUnit.MILLISECONDS);
    }

    private void assertFieldAnnotations(final String testFile,
                                        final boolean shouldBeAnnotated) throws Exception {
        final ProjectConnection connection =
                new ProtoAnnotatorConfigurator(testProjectDir, testFile).configure();
        final BuildLauncher launcher = connection.newBuild();

        launcher.setStandardError(System.out)
                .forTasks(ANNOTATE_PROTO.getValue());
        try {
            launcher.run(new ResultHandler<Void>() {
                @Override
                public void onComplete(Void aVoid) {
                    final FileDescriptorProto fileDescriptor = getDescriptor(testFile);
                    final DescriptorProto messageDescriptor = fileDescriptor.getMessageType(0);
                    final Path sourcePath = getFilePath(messageDescriptor, false,
                                                        fileDescriptor);
                    final NestedTypeFieldsAnnotationValidator validator =
                            new NestedTypeFieldsAnnotationValidator(messageDescriptor,
                                                                    shouldBeAnnotated);
                    validate(sourcePath, validator);
                    countDownLatch.countDown();
                }

                @Override
                public void onFailure(GradleConnectionException e) {
                    throw e;
                }
            });
        } finally {
            connection.close();
        }
        countDownLatch.await(100, TimeUnit.MILLISECONDS);
    }

    private void assertFieldAnnotationsMultiple(final String testFile,
                                                final boolean shouldBeAnnotated) throws Exception {
        final ProjectConnection connection =
                new ProtoAnnotatorConfigurator(testProjectDir, testFile).configure();
        final BuildLauncher launcher = connection.newBuild();

        launcher.setStandardError(System.out)
                .forTasks(ANNOTATE_PROTO.getValue());
        try {
            launcher.run(new ResultHandler<Void>() {
                @Override
                public void onComplete(Void aVoid) {
                    final FileDescriptorProto fileDescriptor = getDescriptor(testFile);
                    final DescriptorProto messageDescriptor = fileDescriptor.getMessageType(0);
                    final FieldDescriptorProto experimentalField = messageDescriptor.getField(0);
                    final Path sourcePath = getFilePath(messageDescriptor, false,
                                                        fileDescriptor);
                    validate(sourcePath, new FieldAnnotationValidator(experimentalField,
                                                                      shouldBeAnnotated));
                    countDownLatch.countDown();
                }

                @Override
                public void onFailure(GradleConnectionException e) {
                    throw e;
                }
            });
        } finally {
            connection.close();
        }
        countDownLatch.await(100, TimeUnit.MILLISECONDS);
    }

    private void assertMainDefinitionAnnotations(final String testFile,
                                                 final boolean shouldBeAnnotated) throws Exception {
        final ProjectConnection connection =
                new ProtoAnnotatorConfigurator(testProjectDir, testFile).configure();
        final BuildLauncher launcher = connection.newBuild();

        launcher.setStandardError(System.out)
                .forTasks(ANNOTATE_PROTO.getValue());
        try {
            launcher.run(new ResultHandler<Void>() {
                @Override
                public void onComplete(Void aVoid) {
                    final FileDescriptorProto fileDescriptor = getDescriptor(testFile);
                    for (DescriptorProto messageDescriptor : fileDescriptor.getMessageTypeList()) {
                        final Path messagePath =
                                getFilePath(messageDescriptor, false, fileDescriptor);
                        final Path messageOrBuilderPath =
                                getFilePath(messageDescriptor, true, fileDescriptor);
                        final SourceVisitor annotationValidator =
                                new MainDefinitionAnnotationValidator(shouldBeAnnotated);
                        validate(messagePath, annotationValidator);
                        validate(messageOrBuilderPath, annotationValidator);
                    }
                    countDownLatch.countDown();
                }

                @Override
                public void onFailure(GradleConnectionException e) {
                    throw e;
                }
            });
        } finally {
            connection.close();
        }
        countDownLatch.await(100, TimeUnit.MILLISECONDS);
    }

    private void assertNestedTypesAnnotations(final String testFile,
                                              final boolean shouldBeAnnotated) throws Exception {
        final ProjectConnection connection =
                new ProtoAnnotatorConfigurator(testProjectDir, testFile).configure();
        final BuildLauncher launcher = connection.newBuild();

        launcher.setStandardError(System.out)
                .forTasks(ANNOTATE_PROTO.getValue());
        try {
            launcher.run(new ResultHandler<Void>() {
                @Override
                public void onComplete(Void aVoid) {
                    final FileDescriptorProto fileDescriptor = getDescriptor(testFile);
                    final Path sourcePath = getFilePath(fileDescriptor);
                    validate(sourcePath, new NestedTypesAnnotationValidator(shouldBeAnnotated));
                    countDownLatch.countDown();
                }

                @Override
                public void onFailure(GradleConnectionException e) {
                    throw e;
                }
            });
        } finally {
            connection.close();
        }
        countDownLatch.await(100, TimeUnit.MILLISECONDS);
    }

    @SuppressWarnings("unchecked")
    private <T extends JavaSource<T>> void validate(Path sourcePath,
                                                    SourceVisitor<T> validationFunction) {
        final Path fullSourcePath =
                get(testProjectDir.getRoot()
                                  .getAbsolutePath(), getDefaultMainGenDir(),
                    sourcePath.toString());

        final AbstractJavaSource<T> javaSource;
        try {
            javaSource = Roaster.parse(AbstractJavaSource.class, fullSourcePath.toFile());
        } catch (FileNotFoundException e) {
            throw Exceptions.illegalStateWithCauseOf(e);
        }
        validationFunction.apply(javaSource);
    }

    @SuppressWarnings("unchecked")
    private <T extends JavaSource<T>> void validateGrpcService(Path servicePath,
                                                               SourceVisitor<T> validationFn) {
        final Path fullSourcePath =
                get(testProjectDir.getRoot()
                                  .getAbsolutePath(), getDefaultMainGenGrpcDir(),
                    servicePath.toString());

        final AbstractJavaSource<T> javaSource;
        try {
            javaSource = Roaster.parse(AbstractJavaSource.class, fullSourcePath.toFile());
        } catch (FileNotFoundException e) {
            throw Exceptions.illegalStateWithCauseOf(e);
        }
        validationFn.apply(javaSource);
    }

    private FileDescriptorProto getDescriptor(final String fileName) {
        final String descriptorSetPath =
                testProjectDir.getRoot()
                              .getAbsolutePath() + getDefaultMainDescriptorsPath();
        final Collection<FileDescriptorProto> descriptors =
                getProtoFileDescriptors(descriptorSetPath, new Predicate<FileDescriptorProto>() {
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

        return Iterables.get(descriptors, 0);
    }
}
