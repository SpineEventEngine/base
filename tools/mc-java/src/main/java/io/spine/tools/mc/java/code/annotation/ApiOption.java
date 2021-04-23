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

package io.spine.tools.mc.java.code.annotation;

import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.DescriptorProtos.FileOptions;
import com.google.protobuf.DescriptorProtos.MessageOptions;
import com.google.protobuf.DescriptorProtos.ServiceOptions;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import com.google.protobuf.GeneratedMessageV3.ExtendableMessage;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.protobuf.Descriptors.Descriptor;
import static com.google.protobuf.Descriptors.FieldDescriptor;
import static io.spine.option.OptionsProto.beta;
import static io.spine.option.OptionsProto.betaAll;
import static io.spine.option.OptionsProto.betaType;
import static io.spine.option.OptionsProto.experimental;
import static io.spine.option.OptionsProto.experimentalAll;
import static io.spine.option.OptionsProto.experimentalType;
import static io.spine.option.OptionsProto.internal;
import static io.spine.option.OptionsProto.internalAll;
import static io.spine.option.OptionsProto.internalType;
import static io.spine.option.OptionsProto.sPIAll;
import static io.spine.option.OptionsProto.sPIService;
import static io.spine.option.OptionsProto.sPIType;

/**
 * A set of Protobuf options marking an API element.
 *
 * <p>The options in a single set have different targets (such as files, message types, fields,
 * etc.), but always represent a singe concept in terms of API visibility, status, etc.
 *
 * <p>For example, options {@code (beta)}, {@code (beta_type)}, and {@code (beta_all)} target,
 * respectively, fields, messages, and files, but all represent a single concept -
 * a <strong>beta API</strong> element.
 */
@Immutable
public final class ApiOption {

    /*
     * Fields of type `GeneratedExtension` are immutable, despite not being annotated.
     */
    @SuppressWarnings("Immutable")
    private final GeneratedExtension<FileOptions, Boolean> fileOption;
    @SuppressWarnings("Immutable")
    private final GeneratedExtension<MessageOptions, Boolean> messageOption;
    @SuppressWarnings("Immutable")
    private final @Nullable GeneratedExtension<ServiceOptions, Boolean> serviceOption;
    @SuppressWarnings("Immutable")
    private final @Nullable GeneratedExtension<FieldOptions, Boolean> fieldOption;

    private ApiOption(GeneratedExtension<FileOptions, Boolean> fileOption,
                      GeneratedExtension<MessageOptions, Boolean> messageOption,
                      @Nullable GeneratedExtension<ServiceOptions, Boolean> serviceOption,
                      @Nullable GeneratedExtension<FieldOptions, Boolean> fieldOption) {
        this.fileOption = checkNotNull(fileOption);
        this.messageOption = checkNotNull(messageOption);
        this.serviceOption = serviceOption;
        this.fieldOption = fieldOption;
    }

    /**
     * Obtains an option which marks beta API.
     */
    public static ApiOption beta() {
        return KnownOption.BETA.option;
    }

    /**
     * Obtains an option which marks beta service provider interface elements.
     */
    public static ApiOption spi() {
        return KnownOption.SPI.option;
    }

    /**
     * Obtains an option which marks experimental API.
     */
    public static ApiOption experimental() {
        return KnownOption.EXPERIMENTAL.option;
    }

    /**
     * Obtains an option which marks internal API.
     */
    public static ApiOption internal() {
        return KnownOption.INTERNAL.option;
    }

    /**
     * Checks if the given file is declared with this option.
     *
     * @param descriptor
     *         the file descriptor to check
     * @return {@code true} if the option is present in the declaration, {@code false} otherwise
     */
    boolean isPresentAt(FileDescriptor descriptor) {
        FileOptions options = descriptor.getOptions();
        return optionPresent(options, fileOption);
    }

    /**
     * Checks if the given message is declared with this option.
     *
     * @param descriptor
     *         the message descriptor to check
     * @return {@code true} if the option is present in the declaration, {@code false} otherwise
     */
    boolean isPresentAt(Descriptor descriptor) {
        MessageOptions options = descriptor.getOptions();
        return optionPresent(options, messageOption);
    }

    /**
     * Checks if the given service is declared with this option.
     *
     * @param descriptor
     *         the service descriptor to check
     * @return {@code true} if the option is present in the declaration, {@code false} otherwise
     */
    boolean isPresentAt(ServiceDescriptor descriptor) {
        checkState(serviceOption != null,
                   "Option %s does not support services.", messageOption.getDescriptor()
                                                                        .getName());
        ServiceOptions options = descriptor.getOptions();
        return optionPresent(options, serviceOption);
    }

    /**
     * Checks if the given field is declared with this option.
     *
     * @param descriptor
     *         the field descriptor to check
     * @return {@code true} if the option is present in the declaration, {@code false} otherwise
     */
    boolean isPresentAt(FieldDescriptor descriptor) {
        checkState(fieldOption != null,
                   "Option %s does not support fields.", messageOption.getDescriptor()
                                                                      .getName());
        FieldOptions options = descriptor.getOptions();
        return optionPresent(options, fieldOption);
    }

    /**
     * Checks if Protobuf services may be defined with this option.
     *
     * @return {@code true} if the option supports services, {@code false} otherwise
     */
    boolean supportsServices() {
        return serviceOption != null;
    }


    private static <T extends ExtendableMessage<T>> boolean
    optionPresent(ExtendableMessage<T> options, GeneratedExtension<T, Boolean> option) {
        return options.hasExtension(option) && options.getExtension(option);
    }

    /**
     * Checks if message fields may be defined with this option.
     *
     * @return {@code true} if the option supports fields, {@code false} otherwise
     */
    boolean supportsFields() {
        return fieldOption != null;
    }

    @Override
    public String toString() {
        return messageOption.getDescriptor()
                            .getName();
    }

    /**
     * Enumeration of well known API options.
     */
    private enum KnownOption {

        BETA(new ApiOption(betaAll, betaType, null, beta)),
        SPI(new ApiOption(sPIAll, sPIType, sPIService, null)),
        EXPERIMENTAL(new ApiOption(experimentalAll, experimentalType, null, experimental)),
        INTERNAL(new ApiOption(internalAll, internalType, null, internal));

        @SuppressWarnings({"NonSerializableFieldInSerializableClass", "PMD.SingularField"})
        // This private enum should not be serialized.
        private final ApiOption option;

        KnownOption(ApiOption option) {
            this.option = option;
        }
    }
}
