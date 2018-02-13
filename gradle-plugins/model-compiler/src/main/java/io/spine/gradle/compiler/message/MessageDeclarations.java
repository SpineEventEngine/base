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

package io.spine.gradle.compiler.message;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;

import java.util.Collection;
import java.util.List;

import static io.spine.gradle.compiler.message.MessageDeclaration.create;

/**
 * Utilities for working with {@linkplain MessageDeclaration message declarations}.
 *
 * @author Dmytro Grankin
 */
public class MessageDeclarations {

    /** Prevent instantiation of this utility class. */
    private MessageDeclarations() {
    }

    /**
     * Obtains message declarations, that match the specified {@link Predicate}.
     *
     * @param files     the file descriptors to scan
     * @param predicate the predicate to test a message
     * @return the message declarations
     */
    public static List<MessageDeclaration> find(Iterable<FileDescriptorProto> files,
                                                Predicate<DescriptorProto> predicate) {
        final ImmutableList.Builder<MessageDeclaration> result = ImmutableList.builder();
        for (FileDescriptorProto file : files) {
            final Collection<MessageDeclaration> declarations =
                    scanFile(file, predicate);
            result.addAll(declarations);
        }
        return result.build();
    }

    private static List<MessageDeclaration> scanFile(FileDescriptorProto file,
                                                     Predicate<DescriptorProto> predicate) {
        final ImmutableList.Builder<MessageDeclaration> result = ImmutableList.builder();
        for (DescriptorProto messageType : file.getMessageTypeList()) {
            final MessageDeclaration declaration = create(messageType, file);
            if (predicate.apply(messageType)) {
                result.add(declaration);
            }
            final Collection<MessageDeclaration> allNested =
                    declaration.getAllNested(predicate);
            result.addAll(allNested);
        }
        return result.build();
    }
}
