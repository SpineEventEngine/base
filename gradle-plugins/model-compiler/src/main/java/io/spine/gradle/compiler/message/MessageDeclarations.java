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

import com.google.common.base.Predicate;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;

import java.util.Collection;
import java.util.Deque;
import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;

/**
 * Utilities for working with {@linkplain MessageDeclaration message declarations}.
 *
 * @author Dmytro Grankin
 */
public class MessageDeclarations {

    private MessageDeclarations() {
        // Prevent instantiation of this utility class.
    }

    /**
     * Obtains message declarations, that match the specified {@link Predicate}.
     *
     * @param fileDescriptors the file descriptors to scan
     * @param predicate       the predicate to test a message
     * @return the message declarations
     */
    public static Collection<MessageDeclaration> find(
            Collection<FileDescriptorProto> fileDescriptors, Predicate<DescriptorProto> predicate) {
        final List<MessageDeclaration> result = newLinkedList();
        for (FileDescriptorProto fileDescriptor : fileDescriptors) {
            final Collection<MessageDeclaration> declarationsFromFile = scanFile(fileDescriptor,
                                                                                 predicate);
            result.addAll(declarationsFromFile);
        }
        return result;
    }

    private static Collection<MessageDeclaration> scanFile(FileDescriptorProto fileDescriptor,
                                                           Predicate<DescriptorProto> predicate) {
        final List<MessageDeclaration> result = newLinkedList();
        for (DescriptorProto messageDescriptor : fileDescriptor.getMessageTypeList()) {
            final MessageDeclaration declaration = MessageDeclaration.create(messageDescriptor,
                                                                             fileDescriptor);
            if (predicate.apply(messageDescriptor)) {
                result.add(declaration);
            }
            final Collection<MessageDeclaration> nestedDeclarations =
                    scanNestedTypesRecursively(declaration, predicate);
            result.addAll(nestedDeclarations);
        }
        return result;
    }

    private static Collection<MessageDeclaration> scanNestedTypesRecursively(
            MessageDeclaration declaration, Predicate<DescriptorProto> predicate) {
        final List<MessageDeclaration> result = newLinkedList();
        final List<DescriptorProto> nestedTypes = declaration.getDescriptor()
                                                             .getNestedTypeList();
        final Deque<DescriptorProto> deque = newLinkedList(nestedTypes);

        while (!deque.isEmpty()) {
            final DescriptorProto nestedMessage = deque.pollFirst();
            final MessageDeclaration nestedDeclaration = declaration.forNested(nestedMessage);

            if (predicate.apply(nestedMessage)) {
                result.add(nestedDeclaration);
            }

            deque.addAll(nestedMessage.getNestedTypeList());
        }
        return result;
    }
}
