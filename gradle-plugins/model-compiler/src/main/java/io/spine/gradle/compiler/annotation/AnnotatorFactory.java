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

import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileOptions;
import com.google.protobuf.DescriptorProtos.MessageOptions;
import com.google.protobuf.DescriptorProtos.ServiceOptions;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;

import java.lang.annotation.Annotation;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * A factory for {@linkplain Annotator Annotators}.
 */
class AnnotatorFactory {

    /**
     * Protobuf file descriptors to process.
     */
    private final Collection<FileDescriptorProto> fileDescriptors;

    /**
     * An absolute path to the Java sources directory,
     * generated basing on {@link #fileDescriptors}.
     */
    private final String genProtoDir;

    /**
     * An absolute path to the {@code gRPC} services directory,
     * generated basing on {@link #fileDescriptors}.
     */
    private final String genGrpcDir;

    AnnotatorFactory(Collection<FileDescriptorProto> fileDescriptors, String genProtoDir,
                     String genGrpcDir) {
        checkArguments(fileDescriptors, genProtoDir, genGrpcDir);
        this.fileDescriptors = fileDescriptors;
        this.genProtoDir = genProtoDir;
        this.genGrpcDir = genGrpcDir;
    }

    Annotator createFileAnnotator(Class<? extends Annotation> annotation,
                                  GeneratedExtension<FileOptions, Boolean> option) {
        return new FileAnnotator(annotation, option, fileDescriptors, genProtoDir, genGrpcDir);
    }

    Annotator createMessageAnnotator(Class<? extends Annotation> annotation,
                                     GeneratedExtension<MessageOptions, Boolean> option) {
        return new MessageAnnotator(annotation, option, fileDescriptors, genProtoDir);
    }

    Annotator createFieldAnnotator(Class<? extends Annotation> annotation,
                                   GeneratedExtension<FieldOptions, Boolean> option) {
        return new FieldAnnotator(annotation, option, fileDescriptors, genProtoDir);
    }

    Annotator createServiceAnnotator(Class<? extends Annotation> annotation,
                                     GeneratedExtension<ServiceOptions, Boolean> option) {
        return new ServiceAnnotator(annotation, option, fileDescriptors, genGrpcDir);
    }

    private static void checkArguments(Collection<FileDescriptorProto> fileDescriptors,
                                       String genProtoDir, String genGrpcDir) {
        checkNotNull(fileDescriptors);
        checkArgument(!isNullOrEmpty(genProtoDir));
        checkArgument(!isNullOrEmpty(genGrpcDir));
    }
}
