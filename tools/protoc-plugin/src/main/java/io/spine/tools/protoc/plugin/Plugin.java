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

package io.spine.tools.protoc.plugin;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;
import io.spine.code.proto.OptionExtensionRegistry;
import io.spine.tools.protoc.SpineProtocConfig;
import io.spine.tools.protoc.plugin.column.ColumnGen;
import io.spine.tools.protoc.plugin.field.FieldGen;
import io.spine.tools.protoc.plugin.message.BuilderGen;
import io.spine.tools.protoc.plugin.message.InterfaceGen;
import io.spine.tools.protoc.plugin.message.ValidationGen;
import io.spine.tools.protoc.plugin.message.NestedClassGen;
import io.spine.tools.protoc.plugin.method.MethodGen;
import io.spine.tools.protoc.plugin.query.EntityQueryGen;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Base64;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A Protobuf Compiler ({@literal a.k.a.} {@code protoc}) plugin.
 *
 * <p>The program reads
 * a {@link com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest CodeGeneratorRequest}
 * from {@code System.in} and writes
 * a {@link com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse CodeGeneratorResponse}
 * into the {@code System.out}.
 *
 * <p>For the description of the plugin behavior see
 * {@link io.spine.tools.protoc.plugin.message.InterfaceGen InterfaceGen} and
 * {@link io.spine.tools.protoc.plugin.method.MethodGen MethodGen}.
 *
 * <p>For the plugin mechanism see <a href="SpineProtoGenerator.html#contract">
 * {@code SpineProtoGenerator}</a>.
 */
public final class Plugin {

    /** Prevents instantiation from outside. */
    private Plugin() {
    }

    /**
     * The entry point of the program.
     */
    public static void main(String[] args) {
        CodeGeneratorRequest request = readRequest();
        SpineProtocConfig config = readConfig(request);
        CompositeGenerator generator = CompositeGenerator.of(
                InterfaceGen.instance(config),
                MethodGen.instance(config),
                BuilderGen.instance(config),
                ValidationGen.instance(config),
                NestedClassGen.instance(config),
                ColumnGen.instance(config),
                EntityQueryGen.instance(config),
                FieldGen.instance(config)
        );
        CodeGeneratorResponse response = generator.process(request);
        writeResponse(response);
    }

    private static CodeGeneratorRequest readRequest() {
        try {
            CodeGeneratorRequest request = CodeGeneratorRequest
                    .parseFrom(System.in, OptionExtensionRegistry.instance());
            return request;
        } catch (IOException e) {
            throw newIllegalStateException(e, "Unable to read Code Generator Request.");
        }
    }

    private static SpineProtocConfig readConfig(CodeGeneratorRequest request) {
        String configFilePath = decodeBase64(request.getParameter());
        try (FileInputStream fis = new FileInputStream(configFilePath)) {
            SpineProtocConfig config = SpineProtocConfig
                    .parseFrom(fis, OptionExtensionRegistry.instance());
            return config;
        } catch (InvalidProtocolBufferException e) {
            throw newIllegalStateException(e, "Unable to decode Spine Protoc Plugin config.");
        } catch (FileNotFoundException e) {
            throw newIllegalStateException(e, "Spine Protoc Plugin config file not found.");
        } catch (IOException e) {
            throw newIllegalStateException(e, "Unable to read Spine Protoc Plugin config.");
        }
    }

    private static String decodeBase64(String value) {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] decodedBytes = decoder.decode(value);
        String result = new String(decodedBytes, UTF_8);
        return result;
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr") // Required by the protoc API.
    private static void writeResponse(CodeGeneratorResponse response) {
        checkNotNull(response);
        try {
            response.writeTo(System.out);
        } catch (IOException e) {
            throw newIllegalStateException(
                    e, "Unable to write Spine Protoc Plugin code generator response.");
        }
    }
}
