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

package io.spine.tools.protoc;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import io.spine.option.IsOption;
import io.spine.option.Options;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.option.OptionsProto.everyIs;
import static io.spine.option.OptionsProto.is;
import static io.spine.tools.protoc.MarkerInterfaceSpec.prepareInterface;

/**
 * A tuple of two {@link File} instances representing a message and the marker interface
 * resolved for that message.
 */
final class MessageAndInterface {

    private final InsertionPoint messageFile;
    private final @Nullable UserMarkerInterface interfaceFile;

    private MessageAndInterface(InsertionPoint messageFile,
                                @Nullable UserMarkerInterface interfaceFile) {
        this.messageFile = checkNotNull(messageFile);
        this.interfaceFile = interfaceFile;
    }

    /**
     * Scans the given {@linkplain FileDescriptorProto file} for the {@code (every_is)} option.
     */
    static Collection<CompilerOutput> scanFileOption(FileDescriptorProto file,
                                                     DescriptorProto msg) {
        Set<CompilerOutput> files = getEveryIs(file)
                .map(option -> generateFile(file, msg, option))
                .map(MessageAndInterface::asSet)
                .orElseGet(ImmutableSet::of);
        return files;
    }

    private static Optional<IsOption> getEveryIs(FileDescriptorProto descriptor) {
        Optional<IsOption> value = Options.option(descriptor, everyIs);
        return value;
    }

    /**
     * Scans the given {@linkplain DescriptorProto message} for the {@code (is)} option.
     */
    static Collection<CompilerOutput> scanMsgOption(FileDescriptorProto file, DescriptorProto msg) {
        Set<CompilerOutput> files = getIs(msg)
                .map(option -> generateFile(file, msg, option))
                .map(MessageAndInterface::asSet)
                .orElseGet(ImmutableSet::of);
        return files;
    }

    private static Optional<IsOption> getIs(DescriptorProto descriptor) {
        Optional<IsOption> value = Options.option(descriptor, is);
        return value;
    }

    private static MessageAndInterface generateFile(FileDescriptorProto file,
                                                    DescriptorProto msg,
                                                    IsOption optionValue) {
        MarkerInterfaceSpec interfaceSpec = prepareInterface(optionValue, file);
        UserMarkerInterface markerInterface = UserMarkerInterface.from(interfaceSpec);
        InsertionPoint message = InsertionPoint.implementInterface(file, msg, markerInterface);
        UserMarkerInterface interfaceToGenerate = optionValue.getGenerate()
                                                  ? markerInterface
                                                  : null;
        MessageAndInterface result = new MessageAndInterface(message, interfaceToGenerate);
        return result;
    }

    /**
     * Converts the instance into the pair containing a message file and an interface file.
     */
    private Set<CompilerOutput> asSet() {
        if (interfaceFile == null) {
            return ImmutableSet.of(messageFile);
        } else {
            return ImmutableSet.of(messageFile, interfaceFile);
        }
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
