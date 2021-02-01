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

import io.spine.annotation.Internal;

/**
 * Names of Gradle tasks defined by the Spine Model Compiler plugin.
 */
@Internal
public enum ModelCompilerTaskName implements TaskName {

    /**
     * Additional cleanup task added to the Gradle lifecycle.
     */
    preClean,

    /**
     * Generates source code of rejections in the {@code main} scope.
     */
    generateRejections,

    /**
     * Generates source code of rejections in the {@code test} scope.
     */
    generateTestRejections,

    /**
     * Generates the helper types for declaring columns in the {@code main} scope.
     */
    generateColumnInterfaces,

    /**
     * Generates the helper types for declaring columns in the {@code test} scope.
     */
    generateTestColumnInterfaces,

    /**
     * Annotates the Java sources generated from {@code .proto} files the {@code main} scope.
     */
    annotateProto,

    /**
     * Annotates the Java sources generated from {@code .proto} files the {@code test} scope.
     */
    annotateTestProto,

    /**
     * Merges all the known type descriptors of the module into one in the {@code main} scope.
     */
    mergeDescriptorSet,

    /**
     * Merges all the known type descriptors of the module into one in the {@code test} scope.
     */
    mergeTestDescriptorSet,

    /**
     * Creates the Protobuf compiler plugin configuration.
     *
     * <p>Works only with the {@code main} scope.
     */
    writePluginConfiguration,

    /**
     * Creates the Protobuf compiler plugin test configuration.
     */
    writeTestPluginConfiguration,

    /**
     * Creates the {@code desc.ref} file containing the reference to the descriptor file(s) with
     * the known types.
     *
     * <p>Works only with the {@code main} scope descriptors.
     */
    writeDescriptorReference,

    /**
     * Creates the {@code desc.ref} file containing the reference to the descriptor file(s) with
     * the known types.
     *
     * <p>Works only with the {@code test} scope descriptors.
     */
    writeTestDescriptorReference
}
