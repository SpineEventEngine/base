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

import com.google.common.base.Predicate;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;

/**
 * Utilities for finding Protobuf {@link MessageContext message declarations}.
 *
 * @author Dmytro Grankin
 */
public class MessageFinder {

    private MessageFinder() {
        // Prevent instantiation of this utility class.
    }

    /**
     * Obtains message contexts for the messages, that match the specified {@link Predicate}.
     *
     * @param fileDescriptors the file descriptors to scan
     * @param predicate       the predicate to test a message
     * @return the message contexts
     */
    public static List<MessageContext> find(Collection<FileDescriptorProto> fileDescriptors,
                                            Predicate<DescriptorProto> predicate) {
        final List<MessageContext> result = newLinkedList();
        for (FileDescriptorProto fileDescriptor : fileDescriptors) {
            final List<MessageContext> contextsFromFile = scanFile(fileDescriptor, predicate);
            result.addAll(contextsFromFile);
        }
        return result;
    }

    private static List<MessageContext> scanFile(FileDescriptorProto fileDescriptor,
                                                 Predicate<DescriptorProto> predicate) {
        final List<MessageContext> result = newLinkedList();
        for (DescriptorProto messageDescriptor : fileDescriptor.getMessageTypeList()) {
            final MessageContext messageContext = MessageContext.create(messageDescriptor,
                                                                        fileDescriptor);
            if (predicate.apply(messageDescriptor)) {
                result.add(messageContext);
            }
            final List<MessageContext> contextsFromNestedTypes =
                    scanNestedTypesRecursively(messageContext, predicate);
            result.addAll(contextsFromNestedTypes);
        }
        return result;
    }

    private static List<MessageContext> scanNestedTypesRecursively(MessageContext messageContext,
                                                                   Predicate<DescriptorProto> predicate) {
        final List<DescriptorProto> nestedTypes = messageContext.getTarget()
                                                                .getNestedTypeList();
        if (nestedTypes.isEmpty()) {
            return Collections.emptyList();
        }

        final List<MessageContext> result = newLinkedList();
        for (DescriptorProto nestedType : nestedTypes) {
            final MessageContext nestedTypeContext = messageContext.forNested(nestedType);
            if (predicate.apply(nestedType)) {
                result.add(nestedTypeContext);
            }

            final List<MessageContext> contextsFromNestedType =
                    scanNestedTypesRecursively(nestedTypeContext, predicate);
            result.addAll(contextsFromNestedType);
        }
        return result;
    }
}
