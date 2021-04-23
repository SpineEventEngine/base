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

package io.spine.tools.protoc.plugin.java.message;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import io.spine.option.IsOption;
import io.spine.tools.protoc.plugin.CompilerOutput;
import io.spine.type.MessageType;
import io.spine.type.Type;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.protoc.plugin.java.message.Implement.interfaceFor;

/**
 * A specification of interfaces that a generated type is going to implement.
 *
 * <p>If a custom interface is specified in a proto option, then the generated class is going
 * to implement two interfaces: a standard interface, which corresponds to the passed type,
 * and the one, which used defined in the option.
 */
final class MixInSpec {

    private final Implement standardInterface;
    private final @Nullable UserDefinedInterface customInterface;

    private MixInSpec(Implement standard, @Nullable UserDefinedInterface custom) {
        this.standardInterface = checkNotNull(standard);
        this.customInterface = custom;
    }

    /**
     * Generates compiler output for a possibly declared {@code (is)} and
     * {@code (every_is)} options for the passed message type.
     *
     * @see InterfaceGen#generate(Type)
     */
    static ImmutableList<CompilerOutput> scanOptionsFor(MessageType type) {
        ImmutableList<CompilerOutput> fromFileOption = scanEveryIsOption(type);
        ImmutableList<CompilerOutput> fromTypeOption = scanIsOption(type);
        ImmutableSet<CompilerOutput> deduplicated = ImmutableSet.<CompilerOutput>builder()
                .addAll(fromFileOption)
                .addAll(fromTypeOption)
                .build();
        return deduplicated.asList();
    }

    /**
     * Scans the given {@linkplain FileDescriptorProto file} for the {@code (every_is)} option.
     */
    private static ImmutableList<CompilerOutput> scanEveryIsOption(MessageType type) {
        @Nullable IsOption option = EveryIs.of(type).orElse(null);
        return process(type, option);
    }

    private static ImmutableList<CompilerOutput> scanIsOption(MessageType type) {
        @Nullable IsOption option = Is.of(type).orElse(null);
        return process(type, option);
    }

    private static ImmutableList<CompilerOutput>
    process(MessageType type, @Nullable IsOption option) {
        if (option == null) {
            return ImmutableList.of();
        }
        MixInSpec mix = mixFor(type, option);
        return mix.asList();
    }

    private static MixInSpec mixFor(MessageType type, IsOption isOption) {
        UserDefinedInterface fromOption = UserDefinedInterface.declaredFor(type, isOption);
        Implement standard = interfaceFor(type, fromOption);
        @Nullable UserDefinedInterface custom =
                isOption.getGenerate()
                ? fromOption
                : null;
        MixInSpec result = new MixInSpec(standard, custom);
        return result;
    }

    /**
     * Converts the instance into the pair containing a message file and an interface file.
     */
    private ImmutableList<CompilerOutput> asList() {
        return customInterface == null
               ? ImmutableList.of(standardInterface)
               : ImmutableList.of(standardInterface, customInterface);
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
