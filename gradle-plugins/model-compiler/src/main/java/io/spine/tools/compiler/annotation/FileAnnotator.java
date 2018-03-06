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

package io.spine.tools.compiler.annotation;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileOptions;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import io.spine.tools.java.SourceFile;
import org.jboss.forge.roaster.model.impl.AbstractJavaSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.option.UnknownOptions.getUnknownOptionValue;

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
                  Collection<FileDescriptorProto> files,
                  String genProtoDir,
                  String genGrpcDir) {
        super(annotation, option, files, genProtoDir);
        checkNotNull(genGrpcDir);
        this.genGrpcDir = genGrpcDir;
    }

    @Override
    public void annotate() {
        for (FileDescriptorProto file : fileDescriptors()) {
            if (shouldAnnotate(file)) {
                annotate(file);
            }
        }
    }

    @Override
    protected void annotateOneFile(FileDescriptorProto file) {
        annotateServices(file);
        annotateNestedTypes(file);
    }

    @Override
    protected void annotateMultipleFiles(FileDescriptorProto file) {
        annotateMessages(file);
        annotateEnums(file);
        annotateServices(file);
    }

    /**
     * Annotates all nested types in a generated
     * {@linkplain FileOptions#getJavaOuterClassname() outer class}.
     *
     * @param file the file descriptor to get the outer class.
     * @see #annotateServices(FileDescriptorProto)
     */
    private void annotateNestedTypes(FileDescriptorProto file) {
        final SourceFile filePath = SourceFile.forOuterClassOf(file);
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
     * @param file the file descriptor to get message descriptors
     */
    private void annotateMessages(FileDescriptorProto file) {
        for (DescriptorProto messageType : file.getMessageTypeList()) {
            final SourceFile messageClass =
                    SourceFile.forMessage(messageType, false, file);
            rewriteSource(messageClass, new TypeDeclarationAnnotation());

            final SourceFile messageOrBuilderClass =
                    SourceFile.forMessage(messageType, true, file);
            rewriteSource(messageOrBuilderClass, new TypeDeclarationAnnotation());
        }
    }

    /**
     * Annotates all enums generated from the specified {@link FileDescriptorProto}.
     *
     * <p>The specified file descriptor should
     * {@linkplain FileOptions#hasJavaMultipleFiles() has multiple Java files}.
     *
     * @param file the file descriptor to get enum descriptors
     */
    private void annotateEnums(FileDescriptorProto file) {
        for (EnumDescriptorProto enumType : file.getEnumTypeList()) {
            final SourceFile filePath = SourceFile.forEnum(enumType, file);
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
     * @param file the file descriptor to get service descriptors
     */
    private void annotateServices(FileDescriptorProto file) {
        for (ServiceDescriptorProto serviceDescriptor : file.getServiceList()) {
            final SourceFile serviceClass = SourceFile.forService(serviceDescriptor, file);
            rewriteSource(genGrpcDir, serviceClass, new TypeDeclarationAnnotation());
        }
    }

    @Override
    protected String getRawOptionValue(FileDescriptorProto file) {
        return getUnknownOptionValue(file, getOptionNumber());
    }
}
