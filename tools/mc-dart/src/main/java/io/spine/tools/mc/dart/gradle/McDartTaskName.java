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

package io.spine.tools.mc.dart.gradle;

import io.spine.annotation.Internal;
import io.spine.tools.gradle.TaskName;

/**
 * Names of Gradle tasks defined by the Spine Protobuf Dart plugin.
 */
@Internal
public enum McDartTaskName implements TaskName {

    /**
     * Creates the {@code types.dart} file which contains type mapping for all the production
     * Protobuf types defined in this project.
     *
     * <p>Works only with the {@code main} scope types.
     */
    generateDartTypeRegistry,

    /**
     * Creates the {@code types.dart} file which contains type mapping for all the test Protobuf
     * types defined in this project.
     *
     * <p>Works only with the {@code test} scope types.
     */
    generateDartTestTypeRegistry,

    /**
     * Copies the Dart code generated from Protobuf from its temporary location to
     * the {@code lib} directory.
     *
     * <p>Works only with the {@code main} scope files.
     */
    copyGeneratedDart,

    /**
     * Copies the Dart code generated from Protobuf from its temporary location to
     * the {@code test} directory.
     *
     * <p>Works only with the {@code test} scope files.
     */
    copyTestGeneratedDart,

    /**
     * Rewrites the Dart source files generated from Protobuf with
     * the resolved absolute imports.
     */
    resolveImports,

    /**
     * Rewrites the Dart test source files of the generated from Protobuf with
     * the resolved absolute imports.
     */
    resolveTestImports
}
