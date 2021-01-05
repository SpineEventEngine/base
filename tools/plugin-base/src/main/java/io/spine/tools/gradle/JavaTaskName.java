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
 * Names of Gradle tasks defined by the {@code java} plugin.
 *
 * @see <a href="https://docs.gradle.org/current/userguide/java_plugin.html#sec:java_tasks">
 *         the plugin doc</a>
 */
@Internal
public enum JavaTaskName implements TaskName {

    /**
     * Compiles production Java source files using the JDK compiler.
     */
    compileJava,

    /**
     * Compiles test Java source files using the JDK compiler.
     */
    compileTestJava,

    /**
     * A lifecycle task which marks processing of all the classes and resources in this project.
     */
    classes,

    /**
     * A lifecycle task which marks processing of all the test classes and resources in this
     * project.
     */
    testClasses,

    /**
     * Copies production resources into the production resources directory.
     */
    processResources,

    /**
     * Copies test resources into the test resources directory.
     */
    processTestResources
}
