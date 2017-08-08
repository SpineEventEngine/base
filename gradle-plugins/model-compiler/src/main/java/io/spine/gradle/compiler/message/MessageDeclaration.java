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

package io.spine.gradle.compiler.message;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import io.spine.type.TypeName;

import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A declaration of a Protobuf message.
 *
 * <p>Additionally to a {@linkplain DescriptorProto message descriptor},
 * includes information about the location of the declaration.
 *
 * @author Dmytro Grankin
 */
public class MessageDeclaration {

    private static final String PROTO_TYPE_SEPARATOR = ".";

    /**
     * The descriptor of the declaration.
     */
    private final DescriptorProto descriptor;

    /**
     * Descriptors for the outer messages of the declaration.
     *
     * <p>The descriptors ordered from a top-level definition of a file
     * to the outer message for the declaration.
     */
    private final List<DescriptorProto> outerMessages;

    /**
     * A file descriptors, that contains the declaration
     */
    private final FileDescriptorProto fileDescriptor;

    private MessageDeclaration(DescriptorProto descriptor,
                               List<DescriptorProto> outerMessages,
                               FileDescriptorProto fileDescriptor) {
        this.descriptor = descriptor;
        this.outerMessages = outerMessages;
        this.fileDescriptor = fileDescriptor;
    }

    /**
     * Creates the declaration for the specified message.
     *
     * @param message        the top-level definition from the file descriptor
     * @param fileDescriptor the file descriptor containing the message
     * @return the message declaration
     */
    public static MessageDeclaration create(DescriptorProto message,
                                            FileDescriptorProto fileDescriptor) {
        final boolean fileContainsTarget = fileDescriptor.getMessageTypeList()
                                                         .contains(message);
        if (!fileContainsTarget) {
            final String errMsg = "Top-level message `%s` was not found in `%s`.";
            throw newIllegalStateException(errMsg, message.getName(), fileDescriptor.getName());
        }

        final List<DescriptorProto> outerMessages = Collections.emptyList();
        return new MessageDeclaration(message, outerMessages, fileDescriptor);
    }

    /**
     * Obtains the declaration for the specified nested message of the declaration.
     *
     * @param nestedMessage the nested message from this declaration
     * @return the nested message declaration
     */
    public MessageDeclaration forNested(DescriptorProto nestedMessage) {
        final boolean isNestedForCurrentTarget = descriptor.getNestedTypeList()
                                                           .contains(nestedMessage);
        if (!isNestedForCurrentTarget) {
            final String errMsg = "Nested message `%s` was not found in `%s`.";
            throw newIllegalStateException(errMsg, nestedMessage.getName(), descriptor.getName());
        }

        final List<DescriptorProto> outerMessagesForNested = newLinkedList(outerMessages);
        outerMessagesForNested.add(descriptor);
        return new MessageDeclaration(nestedMessage, outerMessagesForNested, fileDescriptor);
    }

    /**
     * Obtains type name for the declaration.
     *
     * @return the type name
     */
    public TypeName getTypeName() {
        final String packagePrefix = fileDescriptor.getPackage() + PROTO_TYPE_SEPARATOR;
        final StringBuilder typeBuilder = new StringBuilder(packagePrefix);
        for (DescriptorProto outerMessage : outerMessages) {
            typeBuilder.append(outerMessage.getName())
                       .append(PROTO_TYPE_SEPARATOR);
        }
        typeBuilder.append(descriptor.getName());
        final String value = typeBuilder.toString();
        return TypeName.of(value);
    }

    public DescriptorProto getDescriptor() {
        return descriptor;
    }

    public FileDescriptorProto getFile() {
        return fileDescriptor;
    }
}
