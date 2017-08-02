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

package io.spine.gradle.compiler.util;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;

import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A context of a {@linkplain DescriptorProto message declaration}.
 *
 * @author Dmytro Grankin
 */
public class MessageContext {

    private static final String PROTO_TYPE_SEPARATOR = ".";

    /**
     * The target of the context.
     */
    private final DescriptorProto target;

    /**
     * Descriptors for the outer messages of the {@link #target}.
     *
     * <p>The descriptors ordered from a top-level definition of a file
     * to the outer message for the target.
     */
    private final List<DescriptorProto> outerMessages;

    /**
     * A file descriptors, that contains the {@link #target}.
     */
    private final FileDescriptorProto fileDescriptor;

    private MessageContext(DescriptorProto target,
                           List<DescriptorProto> outerMessages,
                           FileDescriptorProto fileDescriptor) {
        this.target = target;
        this.outerMessages = outerMessages;
        this.fileDescriptor = fileDescriptor;
    }

    /**
     * Creates the message context for the specified message.
     *
     * @param message        the top-level definition from the file descriptor
     * @param fileDescriptor the file descriptor containing the message
     * @return the message context
     */
    public static MessageContext create(DescriptorProto message,
                                        FileDescriptorProto fileDescriptor) {
        final boolean fileContainsTarget = fileDescriptor.getMessageTypeList()
                                                         .contains(message);
        if (!fileContainsTarget) {
            final String errMsg = "Top-level message definition `%s` was not found in `%s`.";
            throw newIllegalStateException(errMsg, message.getName(), fileDescriptor.getName());
        }

        final List<DescriptorProto> outerMessages = Collections.emptyList();
        return new MessageContext(message, outerMessages, fileDescriptor);
    }

    /**
     * Obtains the context for the specified nested message of the {@link #target}.
     *
     * @param nestedMessage the nested message for the target
     * @return the nested message context
     */
    public MessageContext forNested(DescriptorProto nestedMessage) {
        final boolean isNestedForCurrentTarget = target.getNestedTypeList()
                                                       .contains(nestedMessage);
        if (!isNestedForCurrentTarget) {
            final String errMsg = "Nested message `%s` was not found in `%s`.";
            throw newIllegalStateException(errMsg, nestedMessage.getName(), target.getName());
        }

        final List<DescriptorProto> outerMessagesForNested = newLinkedList(outerMessages);
        outerMessagesForNested.add(target);
        return new MessageContext(nestedMessage, outerMessagesForNested, fileDescriptor);
    }

    /**
     * Obtains fully qualified type name for the target of the context.
     *
     * @return the fully qualified type name
     */
    public String getType() {
        final String packagePrefix = fileDescriptor.getPackage() + PROTO_TYPE_SEPARATOR;
        final StringBuilder typeBuilder = new StringBuilder(packagePrefix);
        for (DescriptorProto outerMessage : outerMessages) {
            typeBuilder.append(outerMessage.getName())
                       .append(PROTO_TYPE_SEPARATOR);
        }
        typeBuilder.append(target.getName());
        return typeBuilder.toString();
    }

    public DescriptorProto getTarget() {
        return target;
    }
}
