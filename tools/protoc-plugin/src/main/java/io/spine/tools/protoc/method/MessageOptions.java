/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.tools.protoc.method;

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import io.spine.code.proto.MessageType;
import io.spine.option.Options;
import io.spine.option.OptionsProto;

import java.util.Optional;

/**
 * A holder of currently supported {@link com.google.protobuf.DescriptorProtos.MessageOptions
 * MessageOptions}.
 */
final class MessageOptions {

    private static final ImmutableMap<String, GeneratedExtension<DescriptorProtos.MessageOptions, ?>> supportedOptions;

    static {
        supportedOptions = ImmutableMap.<String, GeneratedExtension<DescriptorProtos.MessageOptions, ?>>builder()
                .put("enrichment_for", OptionsProto.enrichmentFor)
                .put("SPI_type", OptionsProto.sPIType)
                .put("experimental_type", OptionsProto.experimentalType)
                .put("beta_type", OptionsProto.betaType)
                .put("validation_of", OptionsProto.validationOf)
                .build();
    }

    /** Prevents instantiation of this utility class. */
    private MessageOptions() {
    }

    /**
     * Determines is a message of supplied {@code type} has specified option or not.
     *
     * <p>If an option is not supported, returns {@code false}.
     */
    static boolean hasOption(String messageOption, MessageType type) {
        if (!isSupported(messageOption)) {
            return false;
        }
        GeneratedExtension<DescriptorProtos.MessageOptions, ?> optionType =
                supportedOptions.get(messageOption);
        Optional<?> option = Options.option(type.descriptor(), optionType);
        return option.isPresent();
    }

    private static boolean isSupported(String messageOption) {
        return supportedOptions.containsKey(messageOption);
    }

}
