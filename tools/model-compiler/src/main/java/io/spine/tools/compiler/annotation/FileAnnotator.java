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

package io.spine.tools.compiler.annotation;

import com.google.protobuf.DescriptorProtos.FileOptions;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import io.spine.code.java.SourceFile;
import io.spine.option.Options;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jboss.forge.roaster.model.impl.AbstractJavaSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.code.java.SourceFile.forEnum;
import static io.spine.code.java.SourceFile.forService;

/**
 * A file-level annotator.
 *
 * <p>Annotates generated top-level definitions from a {@code .proto} file,
 * if a specified {@linkplain FileOptions file option} value is {@code true}.
 */
class FileAnnotator extends Annotator<FileOptions, FileDescriptor> {

    private final String genGrpcDir;

    FileAnnotator(Class<? extends Annotation> annotation,
                  GeneratedExtension<FileOptions, Boolean> option,
                  Collection<FileDescriptor> files,
                  String genProtoDir,
                  String genGrpcDir) {
        super(annotation, option, files, genProtoDir);
        checkNotNull(genGrpcDir);
        this.genGrpcDir = genGrpcDir;
    }

    @Override
    public void annotate() {
        for (FileDescriptor file : fileDescriptors()) {
            if (shouldAnnotate(file)) {
                annotate(file);
            }
        }
    }

    @Override
    protected void annotateOneFile(FileDescriptor file) {
        annotateServices(file);
        annotateNestedTypes(file);
    }

    @Override
    protected void annotateMultipleFiles(FileDescriptor file) {
        annotateMessages(file);
        annotateEnums(file);
        annotateServices(file);
    }

    /**
     * Annotates all nested types in a generated
     * {@linkplain FileOptions#getJavaOuterClassname() outer class}.
     *
     * @param file the file descriptor to get the outer class.
     * @see #annotateServices(FileDescriptor)
     */
    private void annotateNestedTypes(FileDescriptor file) {
        SourceFile filePath = SourceFile.forOuterClassOf(file.toProto());
        rewriteSource(filePath, new SourceVisitor<JavaClassSource>() {
            @Override
            public @Nullable Void apply(@Nullable AbstractJavaSource<JavaClassSource> input) {
                checkNotNull(input);
                for (JavaSource nestedType : input.getNestedTypes()) {
                    addAnnotation(nestedType);
                }
                return null;
            }
        });
    }

    /**
     * Annotates all messages generated from the specified {@link FileDescriptor}.
     *
     * <p>The specified file descriptor should
     * {@linkplain FileOptions#getJavaMultipleFiles() have multiple Java files}.
     *
     * @param file the file descriptor to get message descriptors
     */
    private void annotateMessages(FileDescriptor file) {
        for (Descriptor messageType : file.getMessageTypes()) {
            annotateMessageTypes(messageType, file);
        }
    }

    /**
     * Annotates all enums generated from the specified {@link FileDescriptor}.
     *
     * <p>The specified file descriptor should
     * {@linkplain FileOptions#getJavaMultipleFiles() have multiple Java files}.
     *
     * @param file the file descriptor to get enum descriptors
     */
    private void annotateEnums(FileDescriptor file) {
        for (EnumDescriptor enumType : file.getEnumTypes()) {
            SourceFile filePath = forEnum(enumType.toProto(), file.toProto());
            annotate(filePath);
        }
    }

    /**
     * Annotates all {@code gRPC} services,
     * generated basing on the specified {@link FileDescriptor}.
     *
     * <p>A generated service is always a separate file.
     * So value of {@link FileOptions#getJavaMultipleFiles()} does not play a role.
     *
     * @param file the file descriptor to get service descriptors
     */
    private void annotateServices(FileDescriptor file) {
        for (ServiceDescriptor serviceDescriptor : file.getServices()) {
            SourceFile serviceClass = forService(serviceDescriptor.toProto(), file.toProto());
            rewriteSource(genGrpcDir, serviceClass, new TypeDeclarationAnnotation());
        }
    }

    @Override
    protected Optional<Boolean> getOptionValue(FileDescriptor file) {
        return Options.option(file, getOption());
    }
}
