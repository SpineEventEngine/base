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

package io.spine.tools.proto;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import io.spine.tools.java.PackageName;
import io.spine.tools.java.SimpleClassName;

import java.util.Objects;

/**
 * Abstract base for message declarations in a proto file.
 *
 * @author Alexander Yevsyukov
 */
public abstract class AbstractMessageDeclaration {

    /**
     * The message declaration.
     */
    private final DescriptorProto descriptor;

    /**
     * The file which contains the declaration.
     */
    private final FileDescriptorProto fileDescriptor;

    AbstractMessageDeclaration(DescriptorProto descriptor, FileDescriptorProto file) {
        this.descriptor = descriptor;
        this.fileDescriptor = file;
    }

    public DescriptorProto getDescriptor() {
        return descriptor;
    }

    public String getSimpleTypeName() {
        return descriptor.getName();
    }

    public PackageName getJavaPackage() {
        return PackageName.resolve(fileDescriptor);
    }

    public SimpleClassName getSimpleJavaClassName() {
        return SimpleClassName.ofMessage(descriptor);
    }

    public FileDescriptorProto getFileDescriptor() {
        return fileDescriptor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(descriptor, fileDescriptor);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final AbstractMessageDeclaration other = (AbstractMessageDeclaration) obj;
        return Objects.equals(this.descriptor, other.descriptor)
                && Objects.equals(this.fileDescriptor, other.fileDescriptor);
    }
}
