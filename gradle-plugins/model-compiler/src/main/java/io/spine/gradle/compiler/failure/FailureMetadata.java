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

package io.spine.gradle.compiler.failure;

import io.spine.gradle.compiler.util.JavaCode;

import static com.google.protobuf.DescriptorProtos.DescriptorProto;
import static com.google.protobuf.DescriptorProtos.FileDescriptorProto;

/**
 * Encapsulates a failure metadata sufficient for failure writing.
 *
 * @author Dmytro Grankin
 */
public class FailureMetadata {

    private final DescriptorProto descriptor;
    private final String outerClassName;
    private final FileDescriptorProto fileDescriptor;

    /**
     * Creates a new instance.
     *
     * @param failureDescriptor {@link DescriptorProto} of failure's proto message
     * @param fileDescriptor    {@link FileDescriptorProto}, that contains the failure
     */
    public FailureMetadata(DescriptorProto failureDescriptor, FileDescriptorProto fileDescriptor) {
        this.descriptor = failureDescriptor;
        this.outerClassName = JavaCode.getOuterClassName(fileDescriptor);
        this.fileDescriptor = fileDescriptor;
    }

    public DescriptorProto getDescriptor() {
        return descriptor;
    }

    public String getJavaPackage() {
        return fileDescriptor.getOptions()
                             .getJavaPackage();
    }

    public String getOuterClassName() {
        return outerClassName;
    }

    public String getClassName() {
        return descriptor.getName();
    }

    public FileDescriptorProto getFileDescriptor() {
        return fileDescriptor;
    }
}
