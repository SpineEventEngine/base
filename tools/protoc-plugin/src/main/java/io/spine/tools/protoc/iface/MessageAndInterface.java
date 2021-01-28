/*
 * Copyright 2020, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.tools.protoc.iface;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import io.spine.option.IsOption;
import io.spine.tools.protoc.CompilerOutput;
import io.spine.type.MessageType;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A tuple of two {@link File} instances representing a message and the interface resolved for that
 * message.
 */
final class MessageAndInterface {

    private final Implement messageFile;
    private final @Nullable UserDefinedInterface interfaceFile;

    private MessageAndInterface(Implement messageFile,
                                @Nullable UserDefinedInterface interfaceFile) {
        this.messageFile = checkNotNull(messageFile);
        this.interfaceFile = interfaceFile;
    }

    /**
     * Scans the given {@linkplain FileDescriptorProto file} for the {@code (every_is)} option.
     */
    static Collection<CompilerOutput> scanFileOption(MessageType type) {
        Set<CompilerOutput> files = getEveryIs(type)
                .map(option -> generateFile(type, option))
                .map(MessageAndInterface::asSet)
                .orElseGet(ImmutableSet::of);
        return files;
    }

    private static Optional<IsOption> getEveryIs(MessageType type) {
        Descriptors.FileDescriptor descriptor = type.descriptor()
                                                    .getFile();
        Optional<IsOption> value = new EveryIs().valueFrom(descriptor);
        return value;
    }

    /**
     * Scans the given {@linkplain DescriptorProto message} for the {@code (is)} option.
     */
    static Collection<CompilerOutput> scanMsgOption(MessageType type) {
        Set<CompilerOutput> files = getIs(type)
                .map(option -> generateFile(type, option))
                .map(MessageAndInterface::asSet)
                .orElseGet(ImmutableSet::of);
        return files;
    }

    private static Optional<IsOption> getIs(MessageType type) {
        return Is.from(type.descriptor());
    }

    private static MessageAndInterface generateFile(MessageType type,
                                                    IsOption optionValue) {
        InterfaceSpec interfaceSpec = InterfaceSpec.prepareInterface(optionValue, type);
        UserDefinedInterface messageInterface = UserDefinedInterface.from(interfaceSpec);
        Implement message = Implement.interfaceFor(type, messageInterface);
        UserDefinedInterface interfaceToGenerate = optionValue.getGenerate()
                                                     ? messageInterface
                                                     : null;
        MessageAndInterface result = new MessageAndInterface(message, interfaceToGenerate);
        return result;
    }

    /**
     * Converts the instance into the pair containing a message file and an interface file.
     */
    private Set<CompilerOutput> asSet() {
        return interfaceFile == null
               ? ImmutableSet.of(messageFile)
               : ImmutableSet.of(messageFile, interfaceFile);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MessageAndInterface that = (MessageAndInterface) o;
        return Objects.equal(messageFile, that.messageFile) &&
                Objects.equal(interfaceFile, that.interfaceFile);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(messageFile, interfaceFile);
    }
}
