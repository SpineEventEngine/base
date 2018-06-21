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

package io.spine.option;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto;
import com.google.protobuf.DescriptorProtos.EnumOptions;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileOptions;
import com.google.protobuf.DescriptorProtos.MessageOptions;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;
import com.google.protobuf.DescriptorProtos.ServiceOptions;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
import com.google.protobuf.Extension;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.GeneratedMessageV3.ExtendableMessage;
import io.spine.annotation.Internal;

import java.util.Optional;

/**
 * A utility class for working with custom Protobuf options.
 *
 * @author Dmytro Dashenkov
 */
@Internal
public final class Options {

    /**
     * The {@link ExtensionRegistry} with all the {@code spine/options.proto} extensions.
     */
    private static final ExtensionRegistry EXTENSIONS = optionExtensions();

    /**
     * Prevents the utility class instantiation.
     */
    private Options() {
    }

    public static ExtensionRegistry registry() {
        return EXTENSIONS;
    }

    /**
     * Reads the option value from the given message descriptor.
     *
     * @param descriptor the descriptor to read option from
     * @param option     the option to read
     * @param <T>        the type of the option value
     * @return the option value or {@code Optional.empty()} if there is no such value
     */
    public static <T> Optional<T> option(Descriptor descriptor,
                                         Extension<MessageOptions, T> option) {
        return option(descriptor.toProto(), option);
    }

    /**
     * Reads the option value from the given message descriptor.
     *
     * @param descriptor the descriptor to read option from
     * @param option     the option to read
     * @param <T>        the type of the option value
     * @return the option value or {@code Optional.empty()} if there is no such value
     */
    public static <T> Optional<T> option(DescriptorProto descriptor,
                                         Extension<MessageOptions, T> option) {
        MessageOptions options = descriptor.getOptions();
        return readOption(options, option);
    }

    /**
     * Reads the option value from the given enum descriptor.
     *
     * @param descriptor the descriptor to read option from
     * @param option     the option to read
     * @param <T>        the type of the option value
     * @return the option value or {@code Optional.empty()} if there is no such value
     */
    public static <T> Optional<T> option(EnumDescriptor descriptor,
                                         Extension<EnumOptions, T> option) {
        return option(descriptor.toProto(), option);
    }

    /**
     * Reads the option value from the given enum descriptor.
     *
     * @param descriptor the descriptor to read option from
     * @param option     the option to read
     * @param <T>        the type of the option value
     * @return the option value or {@code Optional.empty()} if there is no such value
     */
    public static <T> Optional<T> option(EnumDescriptorProto descriptor,
                                         Extension<EnumOptions, T> option) {
        EnumOptions options = descriptor.getOptions();
        return readOption(options, option);
    }

    /**
     * Reads the option value from the given field descriptor.
     *
     * @param descriptor the descriptor to read option from
     * @param option     the option to read
     * @param <T>        the type of the option value
     * @return the option value or {@code Optional.empty()} if there is no such value
     */
    public static <T> Optional<T> option(FieldDescriptor descriptor,
                                         Extension<FieldOptions, T> option) {
        return option(descriptor.toProto(), option);
    }

    /**
     * Reads the option value from the given field descriptor.
     *
     * @param descriptor the descriptor to read option from
     * @param option     the option to read
     * @param <T>        the type of the option value
     * @return the option value or {@code Optional.empty()} if there is no such value
     */
    public static <T> Optional<T> option(FieldDescriptorProto descriptor,
                                         Extension<FieldOptions, T> option) {
        FieldOptions options = descriptor.getOptions();
        return readOption(options, option);
    }

    /**
     * Reads the option value from the given service descriptor.
     *
     * @param descriptor the descriptor to read option from
     * @param option     the option to read
     * @param <T>        the type of the option value
     * @return the option value or {@code Optional.empty()} if there is no such value
     */
    public static <T> Optional<T> option(ServiceDescriptor descriptor,
                                         Extension<ServiceOptions, T> option) {
        return option(descriptor.toProto(), option);
    }

    /**
     * Reads the option value from the given service descriptor.
     *
     * @param descriptor the descriptor to read option from
     * @param option     the option to read
     * @param <T>        the type of the option value
     * @return the option value or {@code Optional.empty()} if there is no such value
     */
    public static <T> Optional<T> option(ServiceDescriptorProto descriptor,
                                         Extension<ServiceOptions, T> option) {
        ServiceOptions options = descriptor.getOptions();
        return readOption(options, option);
    }


    /**
     * Reads the option value from the given file descriptor.
     *
     * @param descriptor the descriptor to read option from
     * @param option     the option to read
     * @param <T>        the type of the option value
     * @return the option value or {@code Optional.empty()} if there is no such value
     */
    public static <T> Optional<T> option(FileDescriptor descriptor,
                                         Extension<FileOptions, T> option) {
        return option(descriptor.toProto(), option);
    }

    /**
     * Reads the option value from the given file descriptor.
     *
     * @param descriptor the descriptor to read option from
     * @param option     the option to read
     * @param <T>        the type of the option value
     * @return the option value or {@code Optional.empty()} if there is no such value
     */
    public static <T> Optional<T> option(FileDescriptorProto descriptor,
                                         Extension<FileOptions, T> option) {
        FileOptions options = descriptor.getOptions();
        return readOption(options, option);
    }

    private static <T, O extends ExtendableMessage<O>> Optional<T>
    readOption(ExtendableMessage<O> options, Extension<O, T> option) {
        if (options.hasExtension(option)) {
            T value = options.getExtension(option);
            return Optional.of(value);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Creates an {@link ExtensionRegistry} with all the {@code spine/options.proto} extensions.
     */
    private static ExtensionRegistry optionExtensions() {
        ExtensionRegistry registry = ExtensionRegistry.newInstance();
        OptionsProto.registerAllExtensions(registry);
        return registry;
    }
}
