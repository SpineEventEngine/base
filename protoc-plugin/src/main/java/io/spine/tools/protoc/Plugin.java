/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;

/**
 * A Protobuf Compiler (a.k.a. {@code protoc}) plugin.
 *
 * <p>The program reads a {@link CodeGeneratorRequest} from {@code System.in} and writes
 * a {@link CodeGeneratorResponse} into the {@code System.out}.
 *
 * <p>For the description of the plugin behavior see {@link NarrowMessageInterfaceGenerator}.
 *
 * @author Dmytro Dashenkov
 */
public class Plugin {

    private Plugin() {
        // Prevent instantiation.
    }

    /**
     * The entry point of the program.
     */
    public static void main(String[] args) {
        @SuppressWarnings("UseOfSystemOutOrSystemErr") // As part of the `protoc` API
        final MessageIO parser = new MessageIO(System.in, System.out);
        final CodeGeneratorRequest request = parser.readRequest();
        final SpineProtoGenerator generator = NarrowMessageInterfaceGenerator.instance();
        final CodeGeneratorResponse response = generator.process(request);
        parser.writeResponse(response);
    }
}
