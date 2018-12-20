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

import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import io.spine.type.TypeName;

import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;

import static com.google.common.collect.Lists.newLinkedList;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A declaration of a Protobuf message.
 *
 * <p>Additionally to a {@linkplain DescriptorProto message descriptor},
 * includes information about the location of the declaration.
 */
public class MessageDeclaration extends AbstractMessageDeclaration {

    /**
     * The delimiter between parts of a type name.
     *
     * <p>Literal {@code .}.
     */
    private static final char TYPE_PART_SEPARATOR = '.';

    private final List<DescriptorProto> outerMessages;

    private MessageDeclaration(List<DescriptorProto> outerTypes,
                               DescriptorProto declaration,
                               FileDescriptorProto file) {
        super(declaration, file);
        this.outerMessages = outerTypes;
    }

    /**
     * Creates the declaration for the specified message.
     *
     * @param message
     *         the descriptor of the top-level declaration of the message type
     * @param file
     *         the descriptor of the file containing the message
     * @return the message declaration
     */
    public static MessageDeclaration create(DescriptorProto message,
                                            FileDescriptorProto file) {
        boolean fileContainsTarget = file.getMessageTypeList()
                                         .contains(message);
        if (!fileContainsTarget) {
            String errMsg = "Top-level message `%s` was not found in `%s`.";
            throw newIllegalArgumentException(errMsg, message.getName(), file.getName());
        }

        List<DescriptorProto> outerMessages = Collections.emptyList();
        return new MessageDeclaration(outerMessages, message, file);
    }

    /**
     * Obtains immediate declarations of nested types of this declaration.
     *
     * @return immutable list with message declarations or empty list if no nested types declared
     */
    private List<MessageDeclaration> getImmediateNested() {
        ImmutableList.Builder<MessageDeclaration> result = ImmutableList.builder();
        for (DescriptorProto nestedType : getMessage().getNestedTypeList()) {
            MessageDeclaration nestedDeclaration = forNested(nestedType);
            result.add(nestedDeclaration);
        }
        return result.build();
    }

    /**
     * Obtains all nested declarations that match the passed predicate.
     */
    List<MessageDeclaration> getAllNested(Predicate<DescriptorProto> predicate) {
        ImmutableList.Builder<MessageDeclaration> result = ImmutableList.builder();
        Iterable<MessageDeclaration> nestedDeclarations = getImmediateNested();
        Deque<MessageDeclaration> deque = newLinkedList(nestedDeclarations);

        while (!deque.isEmpty()) {
            MessageDeclaration nestedDeclaration = deque.pollFirst();

            assert nestedDeclaration != null; // Cannot be null since the queue is not empty.
            DescriptorProto nestedDescriptor = nestedDeclaration.getMessage();

            if (predicate.test(nestedDescriptor)) {
                result.add(nestedDeclaration);
            }

            deque.addAll(nestedDeclaration.getImmediateNested());
        }
        return result.build();
    }

    /**
     * Obtains the declaration for the specified nested message of the declaration.
     *
     * @param nestedMessage
     *         the nested message from this declaration
     * @return the nested message declaration
     */
    private MessageDeclaration forNested(DescriptorProto nestedMessage) {
        boolean isNestedForCurrentTarget = getMessage().getNestedTypeList()
                                                       .contains(nestedMessage);
        if (!isNestedForCurrentTarget) {
            String errMsg = "Nested message `%s` was not found in `%s`.";
            throw newIllegalStateException(errMsg,
                                           nestedMessage.getName(),
                                           getMessage().getName());
        }

        List<DescriptorProto> outerMessagesForNested = newLinkedList(outerMessages);
        outerMessagesForNested.add(getMessage());
        return new MessageDeclaration(outerMessagesForNested, nestedMessage, getFile());
    }

    /**
     * Obtains type name for the declaration.
     *
     * @return the type name
     */
    public TypeName getTypeName() {
        StringBuilder name = new StringBuilder(getFile().getPackage());
        name.append(TYPE_PART_SEPARATOR);
        for (DescriptorProto outerMessage : outerMessages) {
            name.append(outerMessage.getName())
                .append(TYPE_PART_SEPARATOR);
        }
        name.append(getMessage().getName());
        String value = name.toString();
        return TypeName.of(value);
    }
}
