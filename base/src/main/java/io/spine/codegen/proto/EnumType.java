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

package io.spine.codegen.proto;

import com.google.protobuf.DescriptorProtos.EnumDescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An enumeration type.
 *
 * @author Alexander Yevsyukov
 */
public final class EnumType extends Type<EnumDescriptor, EnumDescriptorProto> {

    private EnumType(EnumDescriptor descriptor) {
        super(descriptor, descriptor.toProto());
    }

    @SuppressWarnings("MethodWithMultipleLoops")
        // Need to go through top level enums and those nested messages.
    static TypeSet allFrom(FileDescriptor file) {
        checkNotNull(file);
        final TypeSet result = TypeSet.newInstance();

        for (EnumDescriptor enumDescriptor : file.getEnumTypes()) {
            result.add(new EnumType(enumDescriptor));
        }

        for (Descriptor messageType : file.getMessageTypes()) {
            addNested(messageType, result);
        }
        return result;
    }

    @SuppressWarnings("MethodWithMultipleLoops") // Need to go through enums and nested messages.
    private static void addNested(Descriptor messageType, TypeSet set) {
        for (EnumDescriptor enumDescriptor : messageType.getEnumTypes()) {
            set.add(new EnumType(enumDescriptor));
        }

        for (Descriptor nestedType : messageType.getNestedTypes()) {
            addNested(nestedType, set);
        }
    }
}
