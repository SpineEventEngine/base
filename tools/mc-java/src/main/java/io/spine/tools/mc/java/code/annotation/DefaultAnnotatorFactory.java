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

package io.spine.tools.mc.java.code.annotation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.java.ClassName;
import io.spine.code.proto.FileSet;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The default implementation of the {@link AnnotatorFactory}.
 */
public final class DefaultAnnotatorFactory implements AnnotatorFactory {

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

    private DefaultAnnotatorFactory(Collection<FileDescriptor> fileDescriptors,
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
        FileSet files = FileSet.parseAsKnownFiles(descriptorSetFile);
        return new DefaultAnnotatorFactory(files.files(), generatedProtoDir, generatedGrpcDir);
    }

    @Override
    public Annotator createFileAnnotator(ClassName annotation, ApiOption option) {
        return new FileAnnotator(annotation, option, fileDescriptors, genProtoDir, genGrpcDir);
    }

    @Override
    public Annotator createMessageAnnotator(ClassName annotation, ApiOption option) {
        return new MessageAnnotator(annotation, option, fileDescriptors, genProtoDir);
    }

    @Override
    public Annotator createFieldAnnotator(ClassName annotation, ApiOption option) {
        return new FieldAnnotator(annotation, option, fileDescriptors, genProtoDir);
    }

    @Override
    public Annotator createServiceAnnotator(ClassName annotation, ApiOption option) {
        return new ServiceAnnotator(annotation, option, fileDescriptors, genGrpcDir);
    }

    @Override
    public Annotator createPatternAnnotator(ClassName annotation, ClassNamePattern pattern) {
        return new PatternAnnotator(annotation, pattern, fileDescriptors, genProtoDir);
    }

    @Override
    public Annotator createMethodAnnotator(ClassName annotation,
                                           ImmutableSet<MethodPattern> patterns) {
        return new MethodNameAnnotator(annotation, patterns, fileDescriptors, genProtoDir);
    }
}
