/*
 * Copyright 2021, TeamDev. All rights reserved.
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

package io.spine.tools.protoc.message;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import io.spine.option.IsOption;
import io.spine.tools.protoc.CompilerOutput;
import io.spine.type.MessageType;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.protoc.message.Implement.interfaceFor;
import static io.spine.tools.protoc.message.InterfaceSpec.createFor;

/**
 * A tuple of two {@link File} instances representing a message and the interface resolved for
 * that message.
 */
final class MixInSpec {

    private final Implement standardInterface;
    private final @Nullable UserDefinedInterface customInterface;

    private MixInSpec(Implement standard, @Nullable UserDefinedInterface custom) {
        this.standardInterface = checkNotNull(standard);
        this.customInterface = custom;
    }

    /**
     * Scans the given {@linkplain FileDescriptorProto file} for the {@code (every_is)} option.
     */
    static ImmutableSet<CompilerOutput> scanFileOption(MessageType type) {
        Optional<MixInSpec> mixin =
                EveryIs.of(type)
                       .map(option -> mixFor(type, option));
        ImmutableSet<CompilerOutput> files =
                mixin.map(MixInSpec::asSet)
                     .orElseGet(ImmutableSet::of);
        return files;
    }

    /**
     * Scans the given {@linkplain DescriptorProto message} for the {@code (is)} option.
     */
    static ImmutableSet<CompilerOutput> scanMsgOption(MessageType type) {
        Optional<MixInSpec> mixin =
                Is.of(type)
                  .map(option -> mixFor(type, option));
        ImmutableSet<CompilerOutput> files =
                mixin.map(MixInSpec::asSet)
                     .orElseGet(ImmutableSet::of);
        return files;
    }

    private static MixInSpec mixFor(MessageType type, IsOption isOption) {
        InterfaceSpec spec = createFor(type, isOption);
        UserDefinedInterface fromOption = UserDefinedInterface.from(spec);
        Implement message = interfaceFor(type, fromOption);
        @Nullable UserDefinedInterface interfaceToGenerate =
                isOption.getGenerate()
                ? fromOption
                : null;
        MixInSpec result = new MixInSpec(message, interfaceToGenerate);
        return result;
    }

    /**
     * Converts the instance into the pair containing a message file and an interface file.
     */
    private ImmutableSet<CompilerOutput> asSet() {
        return customInterface == null
               ? ImmutableSet.of(standardInterface)
               : ImmutableSet.of(standardInterface, customInterface);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MixInSpec that = (MixInSpec) o;
        return Objects.equal(standardInterface, that.standardInterface) &&
                Objects.equal(customInterface, that.customInterface);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(standardInterface, customInterface);
    }
}
