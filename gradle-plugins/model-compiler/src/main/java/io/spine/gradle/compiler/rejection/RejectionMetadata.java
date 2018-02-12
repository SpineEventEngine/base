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

package io.spine.gradle.compiler.rejection;

import io.spine.tools.java.SimpleClassName;

import java.util.Objects;

import static com.google.protobuf.DescriptorProtos.DescriptorProto;
import static com.google.protobuf.DescriptorProtos.FileDescriptorProto;

/**
 * A code generation metadata on a rejection.
 *
 * @author Dmytro Grankin
 */
class RejectionMetadata {

    private final DescriptorProto descriptor;
    private final String outerClassName;
    private final FileDescriptorProto fileDescriptor;

    /**
     * Creates a new instance.
     *
     * @param rejectionDescriptor {@link DescriptorProto} of rejection's proto message
     * @param fileDescriptor      {@link FileDescriptorProto}, that contains the rejection
     */
    RejectionMetadata(DescriptorProto rejectionDescriptor,
                      FileDescriptorProto fileDescriptor) {
        this.descriptor = rejectionDescriptor;
        this.outerClassName = SimpleClassName.outerOf(fileDescriptor)
                                             .value();
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

    @Override
    public int hashCode() {
        return Objects.hash(descriptor, outerClassName, fileDescriptor);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final RejectionMetadata other = (RejectionMetadata) obj;
        return Objects.equals(this.descriptor, other.descriptor)
                && Objects.equals(this.outerClassName, other.outerClassName)
                && Objects.equals(this.fileDescriptor, other.fileDescriptor);
    }
}
