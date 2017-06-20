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

package io.spine.gradle.compiler.validate;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.protobuf.DescriptorProtos.DescriptorProto;

import javax.annotation.Nullable;

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

    @Nullable
    private String sourceProtoFilePath;

    VBMetadata(String javaPackage, String javaClass, DescriptorProto msgDescriptor) {
        checkNotNull(javaPackage);
        checkNotNull(javaClass);
        checkNotNull(msgDescriptor);

        this.javaPackage = javaPackage;
        this.javaClass = javaClass;
        this.msgDescriptor = msgDescriptor;
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

    Optional<String> getSourceProtoFilePath() {
        return Optional.fromNullable(sourceProtoFilePath);
    }

    public void setSourceProtoFilePath(String sourceProtoFilePath) {
        checkNotNull(sourceProtoFilePath);
        this.sourceProtoFilePath = sourceProtoFilePath;
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
