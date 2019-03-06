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

package io.spine.tools.protoc.given;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;
import io.spine.option.OptionsProto;
import io.spine.tools.gradle.compiler.protoc.GeneratedInterfaces;
import io.spine.tools.gradle.compiler.protoc.GeneratedMethods;
import io.spine.tools.protoc.SpineProtocConfig;

import java.util.Base64;

/**
 * A helper class for {@link com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest
 * CodeGeneratorRequest}s creation.
 */
public final class CodeGeneratorRequestGiven {

    /** Prevents instantiation of this utility class. */
    private CodeGeneratorRequestGiven() {
    }

    /**
     * Creates a {@link com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest.Builder
     * CodeGeneratorRequest.Builder} instance with the default Protobuf descriptors and
     * Spine options set.
     */
    public static PluginProtos.CodeGeneratorRequest.Builder requestBuilder() {
        return PluginProtos.CodeGeneratorRequest
                .newBuilder()
                .addProtoFile(descriptorProto())
                .addProtoFile(spineOptionsProto())
                .setCompilerVersion(compilerVersion());
    }

    /**
     * Creates an instance of the latest supported Protobuf version.
     */
    public static PluginProtos.Version compilerVersion() {
        return PluginProtos.Version.newBuilder()
                                   .setMajor(3)
                                   .setMajor(6)
                                   .setPatch(1)
                                   .setSuffix("")
                                   .build();
    }

    /**
     * Creates a Base64-encoded version of a {@link SpineProtocConfig} out of the supplied
     * {@code GeneratedMethods} and a default instance of {@code GeneratedInterfaces}.
     *
     * @see #encodedProtocConfig(GeneratedInterfaces, GeneratedMethods)
     */
    public static String encodedProtocConfig(GeneratedMethods methods) {
        return encodedProtocConfig(GeneratedInterfaces.withDefaults(), methods);
    }

    /**
     * Creates a Base64-encoded version of a {@link SpineProtocConfig} out of the supplied
     * {@code GeneratedInterfaces} and a default instance of {@code GeneratedMethods}.
     *
     * @see #encodedProtocConfig(GeneratedInterfaces, GeneratedMethods)
     */
    public static String encodedProtocConfig(GeneratedInterfaces interfaces) {
        return encodedProtocConfig(interfaces, GeneratedMethods.withDefaults());
    }

    /**
     * Creates a Base64-encoded version of a {@link SpineProtocConfig} out of the supplied
     * {@code GeneratedInterfaces} and {@code GeneratedMethods}.
     */
    public static String encodedProtocConfig(GeneratedInterfaces interfaces,
                                             GeneratedMethods methods) {
        SpineProtocConfig protocConfig = SpineProtocConfig
                .newBuilder()
                .setInterfacesGeneration(interfaces.asProtocConfig())
                .setMethodsGeneration(methods.asProtocConfig())
                .build();
        return Base64.getEncoder()
                     .encodeToString(protocConfig.toByteArray());
    }

    private static DescriptorProtos.FileDescriptorProto spineOptionsProto() {
        return OptionsProto.getDescriptor()
                           .toProto();
    }

    private static DescriptorProtos.FileDescriptorProto descriptorProto() {
        return DescriptorProtos.getDescriptor()
                               .toProto();
    }
}
