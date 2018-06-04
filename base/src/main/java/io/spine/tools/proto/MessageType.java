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

package io.spine.tools.proto;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.type.ClassName;
import io.spine.type.TypeUrl;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A message type as declared in a proto file.
 *
 * @author Alexander Yevsyukov
 */
public class MessageType extends Type<Descriptor, DescriptorProto> {

    protected MessageType(Descriptor descriptor) {
        super(descriptor,
              descriptor.toProto(),
              ClassName.from(descriptor),
              TypeUrl.from(descriptor));
    }

    /**
     * Collects all message types, including nested, declared in the passed file.
     */
    public static TypeSet allFrom(FileDescriptor file) {
        checkNotNull(file);
        final TypeSet.Builder result = TypeSet.newBuilder();
        for (Descriptor messageType : file.getMessageTypes()) {
            addType(messageType, result);
        }
        return result.build();
    }

    private static void addType(Descriptor type, TypeSet.Builder set) {
        set.add(new MessageType(type));
        for (Descriptor nestedType : type.getNestedTypes()) {
            addType(nestedType, set);
        }
    }
}
