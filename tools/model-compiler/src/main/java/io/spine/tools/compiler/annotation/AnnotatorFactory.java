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

package io.spine.tools.compiler.annotation;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.java.ClassName;
import io.spine.code.proto.FileDescriptors;
import io.spine.code.proto.FileSet;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toSet;

/**
 * A factory for {@linkplain Annotator Annotators}.
 */
public final class AnnotatorFactory {

    /**
     * Protobuf file descriptors to process.
     */
    private final ImmutableList<FileDescriptor> fileDescriptors;

    /**
     * An absolute path to the Java sources directory,
     * generated basing on {@link #fileDescriptors}.
     */
    private final Path genProtoDir;

    /**
     * An absolute path to the {@code gRPC} services directory,
     * generated basing on {@link #fileDescriptors}.
     */
    private final Path genGrpcDir;

    private AnnotatorFactory(Collection<FileDescriptor> fileDescriptors,
                             Path genProtoDir,
                             Path genGrpcDir) {
        checkNotNull(fileDescriptors);
        checkNotNull(genProtoDir);
        checkNotNull(genGrpcDir);
        this.fileDescriptors = ImmutableList.copyOf(fileDescriptors);
        this.genProtoDir = genProtoDir;
        this.genGrpcDir = genGrpcDir;
    }

    public static AnnotatorFactory newInstance(File descriptorSetFile,
                                               Path generatedProtoDir,
                                               Path generatedGrpcDir) {
        Collection<FileDescriptor> descriptors = FileSet
                .parse(descriptorSetFile)
                .files()
                .stream()
                .filter(FileDescriptors::isNotGoogle)
                .collect(toSet());
        return new AnnotatorFactory(descriptors, generatedProtoDir, generatedGrpcDir);
    }

    Annotator createFileAnnotator(ClassName annotation, ApiOption option) {
        return new FileAnnotator(annotation, option, fileDescriptors, genProtoDir, genGrpcDir);
    }

    Annotator createMessageAnnotator(ClassName annotation, ApiOption option) {
        return new MessageAnnotator(annotation, option, fileDescriptors, genProtoDir);
    }

    Annotator createFieldAnnotator(ClassName annotation, ApiOption option) {
        return new FieldAnnotator(annotation, option, fileDescriptors, genProtoDir);
    }

    Annotator createServiceAnnotator(ClassName annotation, ApiOption option) {
        return new ServiceAnnotator(annotation, option, fileDescriptors, genGrpcDir);
    }

    Annotator createPatternAnnotator(ClassName annotation, ClassNamePattern pattern) {
        return new PatternAnnotator(annotation, pattern, fileDescriptors, genProtoDir);
    }
}
