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

package io.spine.tools.compiler.validation;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.protobuf.DescriptorProtos.DescriptorProto;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A metadata for the validator builders.
 *
 * @author Illia Shepilov
 */
final class VBMetadata {

    private final String javaClass;
    private final String javaPackage;
    private final DescriptorProto msgDescriptor;
    private final String sourceProtoFilePath;

    VBMetadata(String javaPackage,
               String javaClass,
               DescriptorProto msgDescriptor,
               String sourceProtoFilePath) {
        checkNotNull(javaPackage);
        checkNotNull(javaClass);
        checkNotNull(msgDescriptor);
        checkNotNull(sourceProtoFilePath);

        this.javaPackage = javaPackage;
        this.javaClass = javaClass;
        this.msgDescriptor = msgDescriptor;
        this.sourceProtoFilePath = sourceProtoFilePath;
    }

    String getJavaPackage() {
        return javaPackage;
    }

    String getJavaClass() {
        return javaClass;
    }

    DescriptorProto getMsgDescriptor() {
        return msgDescriptor;
    }

    String getSourceProtoFilePath() {
        return sourceProtoFilePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VBMetadata)) {
            return false;
        }
        VBMetadata that = (VBMetadata) o;
        return Objects.equal(javaClass, that.javaClass) &&
                Objects.equal(javaPackage, that.javaPackage) &&
                Objects.equal(msgDescriptor, that.msgDescriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(javaClass, javaPackage, msgDescriptor);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("javaClass", javaClass)
                          .add("javaPackage", javaPackage)
                          .add("sourceProtoFilePath", sourceProtoFilePath)
                          .toString();
    }
}
