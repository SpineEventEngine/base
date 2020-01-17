/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.tools.gradle;


/**
 * A factory of Protobuf-related artifact specs.
 */
public final class ProtobufDependencies {

    private static final String GROUP_ID = "com.google.protobuf";
    private static final String PROTOBUF_LITE = "protobuf-lite";
    private static final String PROTOC = "protoc";
    private static final PluginId PROTOBUF_PLUGIN_ID = new PluginId(GROUP_ID);

    /**
     * Prevents the utility class instantiation.
     */
    private ProtobufDependencies() {
    }

    /**
     * Obtains the ID of the Protobuf Gradle plugin.
     */
    public static PluginId gradlePlugin() {
        return PROTOBUF_PLUGIN_ID;
    }

    /**
     * Obtains the {@link ThirdPartyDependency} on the Protobuf Lite Java runtime library.
     */
    public static ThirdPartyDependency protobufLite() {
        return new ThirdPartyDependency(GROUP_ID, PROTOBUF_LITE);
    }

    public static ThirdPartyDependency protobufCompiler() {
        return new ThirdPartyDependency(GROUP_ID, PROTOC);
    }
}
