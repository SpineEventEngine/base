/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.code.gen.java.field;

import com.google.common.collect.ImmutableList;
import io.spine.code.java.SimpleClassName;
import io.spine.code.proto.FieldDeclaration;
import io.spine.type.MessageType;

import java.util.List;
import java.util.Queue;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Sets.newHashSet;

/**
 * A recursive collector of {@link com.google.protobuf.Message Message}-typed fields from
 * a message.
 */
final class NestedFieldScanner {

    private final MessageType messageType;

    NestedFieldScanner(MessageType messageType) {
        this.messageType = messageType;
    }

    /**
     * Traverses all top-level and nested fields of the enclosed message type to retrieve those
     * that are singular {@link com.google.protobuf.Message Message}-typed fields.
     *
     * <p>The returned results are unique-by-name. The reason for that is that uniqueness-by-name
     * is required for the uniqueness of the generated types among message strongly-typed fields.
     *
     * @see FieldsSpec
     * @see MessageTypedField
     */
    List<MessageType> scan() {
        List<MessageType> resultTypes = newLinkedList();
        Queue<MessageType> typesToScan = newLinkedList(ImmutableList.of(messageType));
        while (!typesToScan.isEmpty()) {
            MessageType type = typesToScan.poll();
            ImmutableList<MessageType> messageTypes = uniqueMessageFields(type, resultTypes);
            resultTypes.addAll(messageTypes);
            typesToScan.addAll(messageTypes);
        }
        return resultTypes;
    }

    /**
     * Obtains {@code Message}-typed fields of a message with names that not yet occur among
     * the result types.
     */
    private static ImmutableList<MessageType> uniqueMessageFields(MessageType messageType,
                                                                  List<MessageType> resultTypes) {
        Set<SimpleClassName> distinctNames = newHashSet();
        ImmutableList<MessageType> uniqueMessageTypes =
                messageType.fields()
                           .stream()
                           .filter(FieldDeclaration::isSingularMessage)
                           .map(FieldDeclaration::messageType)
                           .filter(type -> distinctNames.add(type.simpleJavaClassName()))
                           .filter(type -> !containsTypeWithSameName(resultTypes, type))
                           .collect(toImmutableList());
        return uniqueMessageTypes;
    }

    private static boolean containsTypeWithSameName(List<MessageType> result, MessageType type) {
        boolean contains = result.stream()
                                 .map(MessageType::simpleJavaClassName)
                                 .anyMatch(name -> name.equals(type.simpleJavaClassName()));
        return contains;
    }
}
