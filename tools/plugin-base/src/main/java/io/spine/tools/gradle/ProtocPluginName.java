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

package io.spine.tools.gradle;

/**
 * Names of known Protobuf compiler plugins.
 */
public enum ProtocPluginName {

    /**
     * The standard Java protoc plugin.
     *
     * <p>Built into the Protobuf compiler.
     *
     * @see <a href="https://developers.google.com/protocol-buffers/docs/reference/java-generated">
     *         Java generated code reference</a>
     */
    java,

    /**
     * The standard JavaScript protoc plugin.
     *
     * <p>Built into the Protobuf compiler.
     *
     * @see <a href="https://developers.google.com/protocol-buffers/docs/reference/javascript-generated">
     *         Java generated code reference</a>
     */
    js,

    /**
     * The gRPC protoc plugin.
     *
     * <p>Generates service implementation bases and stubs for remote calls.
     *
     * @see <a href="https://grpc.io/docs/reference/java/generated-code/">gRPC generated code
     *         reference</a>
     */
    grpc,

    /**
     * The Spine code generation plugin.
     *
     * <p>Generates message interfaces as well as additional methods and nested classes for
     * messages.
     *
     * @see <a href="https://github.com/SpineEventEngine/base/tree/master/tools/protoc-plugin">the
     *         plugin feature overview</a>
     */
    spineProtoc,

    /**
     * The Dart code generation plugin.
     *
     * @see <a href="https://developers.google.com/protocol-buffers/docs/reference/dart-generated">
     *         Dart generated code reference</a>
     */
    dart
}
