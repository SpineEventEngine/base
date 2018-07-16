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

package io.spine.code.proto;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import io.spine.code.java.PackageName;
import io.spine.code.java.SimpleClassName;

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
    private final DescriptorProto message;

    /**
     * The file which contains the declaration.
     */
    private final FileDescriptorProto file;

    AbstractMessageDeclaration(DescriptorProto message, FileDescriptorProto file) {
        this.message = message;
        this.file = file;
    }

    public DescriptorProto getMessage() {
        return message;
    }

    public String getSimpleTypeName() {
        return message.getName();
    }

    public PackageName getJavaPackage() {
        return PackageName.resolve(file);
    }

    public SimpleClassName getSimpleJavaClassName() {
        return SimpleClassName.ofMessage(message);
    }

    public FileDescriptorProto getFile() {
        return file;
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, file);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AbstractMessageDeclaration other = (AbstractMessageDeclaration) obj;
        return Objects.equals(this.message, other.message)
                && Objects.equals(this.file, other.file);
    }
}
