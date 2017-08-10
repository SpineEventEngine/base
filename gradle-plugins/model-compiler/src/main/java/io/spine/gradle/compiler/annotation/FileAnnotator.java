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

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileOptions;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import org.jboss.forge.roaster.model.impl.AbstractJavaSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.gradle.compiler.util.JavaSources.getFilePath;
import static io.spine.protobuf.UnknownOptions.getUnknownOptionValue;

/**
 * A file-level annotator.
 *
 * <p>Annotates generated top-level definitions from a {@code .proto} file,
 * if a specified {@linkplain FileOptions file option} value is {@code true}.
 *
 * @author Dmytro Grankin
 */
class FileAnnotator extends Annotator<FileOptions, FileDescriptorProto> {

    private final String genGrpcDir;

    FileAnnotator(Class<? extends Annotation> annotation,
                  GeneratedExtension<FileOptions, Boolean> option,
                  Collection<FileDescriptorProto> fileDescriptors,
                  String genProtoDir,
                  String genGrpcDir) {
        super(annotation, option, fileDescriptors, genProtoDir);
        checkNotNull(genGrpcDir);
        this.genGrpcDir = genGrpcDir;
    }

    @Override
    void annotate() {
        for (FileDescriptorProto fileDescriptor : fileDescriptors) {
            if (shouldAnnotate(fileDescriptor)) {
                annotate(fileDescriptor);
            }
        }
    }

    @Override
    protected void annotateSingularFile(FileDescriptorProto fileDescriptor) {
        annotateServices(fileDescriptor);
        annotateNestedTypes(fileDescriptor);
    }

    @Override
    protected void annotateMultipleFiles(FileDescriptorProto fileDescriptor) {
        annotateMessages(fileDescriptor);
        annotateEnums(fileDescriptor);
        annotateServices(fileDescriptor);
    }

    /**
     * Annotates all nested types in a generated
     * {@linkplain FileOptions#getJavaOuterClassname() outer class}.
     *
     * @param fileDescriptor the file descriptor to get the outer class.
     * @see #annotateServices(FileDescriptorProto)
     */
    private void annotateNestedTypes(FileDescriptorProto fileDescriptor) {
        final Path filePath = getFilePath(fileDescriptor);
        rewriteSource(filePath, new SourceVisitor<JavaClassSource>() {
            @Nullable
            @Override
            public Void apply(@Nullable AbstractJavaSource<JavaClassSource> input) {
                checkNotNull(input);
                for (JavaSource nestedType : input.getNestedTypes()) {
                    addAnnotation(nestedType);
                }
                return null;
            }
        });
    }

    /**
     * Annotates all messages generated from the specified {@link FileDescriptorProto}.
     *
     * <p>The specified file descriptor should
     * {@linkplain FileOptions#hasJavaMultipleFiles() has multiple Java files}.
     *
     * @param fileDescriptor the file descriptor to get message descriptors
     */
    private void annotateMessages(FileDescriptorProto fileDescriptor) {
        for (DescriptorProto messageDescriptor : fileDescriptor.getMessageTypeList()) {
            final Path messageFilePath = getFilePath(messageDescriptor, false,
                                                     fileDescriptor);
            rewriteSource(messageFilePath, new TypeDeclarationAnnotation());

            final Path messageOrBuilderPath = getFilePath(messageDescriptor, true,
                                                          fileDescriptor);
            rewriteSource(messageOrBuilderPath, new TypeDeclarationAnnotation());
        }
    }

    /**
     * Annotates all enums generated from the specified {@link FileDescriptorProto}.
     *
     * <p>The specified file descriptor should
     * {@linkplain FileOptions#hasJavaMultipleFiles() has multiple Java files}.
     *
     * @param fileDescriptor the file descriptor to get enum descriptors
     */
    private void annotateEnums(FileDescriptorProto fileDescriptor) {
        for (EnumDescriptorProto enumDescriptor : fileDescriptor.getEnumTypeList()) {
            final Path filePath = getFilePath(enumDescriptor, fileDescriptor);
            rewriteSource(filePath, new TypeDeclarationAnnotation());
        }
    }

    /**
     * Annotates all {@code gRPC} services,
     * generated basing on the specified {@link FileDescriptorProto}.
     *
     * <p>A generated service is always a separate file.
     * So value of {@link FileOptions#hasJavaMultipleFiles()} does not play a role.
     *
     * @param fileDescriptor the file descriptor to get service descriptors
     */
    private void annotateServices(FileDescriptorProto fileDescriptor) {
        for (ServiceDescriptorProto serviceDescriptor : fileDescriptor.getServiceList()) {
            final Path sourcePath = getFilePath(serviceDescriptor, fileDescriptor);
            rewriteSource(genGrpcDir, sourcePath, new TypeDeclarationAnnotation());
        }
    }

    @Override
    protected String getRawOptionValue(FileDescriptorProto descriptor) {
        return getUnknownOptionValue(descriptor, getOptionNumber());
    }
}
