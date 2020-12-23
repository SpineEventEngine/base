/*
 * Copyright 2020, TeamDev. All rights reserved.
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
 * The names of Gradle configurations used by the Spine model compiler plugin.
 *
 * <p>See <a href="https://docs.gradle.org/current/userguide/managing_dependency_configurations.html">
 * the Gradle doc</a> on dependency configurations for more info.
 */
public enum ConfigurationName {

    /**
     * The classpath of the Gradle build process.
     */
    classpath,

    /**
     * The annotation processors used during the compilation of this module.
     *
     * <p>These dependencies are not accessible to the user at compile-time or at runtime directly.
     */
    annotationProcessor,

    /**
     * The API of a Java library.
     *
     * <p>The dependencies are available at compile-time and runtime.
     *
     * <p>Dependencies in this configuration are included as compile-time transitive dependencies in
     * the artifacts of the library.
     */
    api,

    /**
     * Dependencies on which the Java module relies for implementation.
     *
     * <p>The dependencies are available at compile-time and runtime.
     *
     * <p>Dependencies in this configuration are included as runtime transitive dependencies in
     * the artifacts of the module.
     */
    implementation,

    /**
     * Dependencies available at compile-time but not at runtime.
     *
     * <p>Suitable for annotations with {@link java.lang.annotation.RetentionPolicy#CLASS}.
     */
    compileOnly,

    /**
     * All the dependencies included for the Java module compilation.
     *
     * <p>Users cannot add dependencies directly to this configuration. However, this configuration
     * may be resolved.
     */
    compileClasspath,

    /**
     * Dependencies available at runtime but not at compile-time.
     *
     * <p>Suitable for SPI implementations loaded via {@link java.util.ServiceLoader} or other
     * classpath scanning utilities.
     */
    runtimeOnly,

    /**
     * All the dependencies included for the Java module runtime.
     *
     * <p>Users cannot add dependencies directly to this configuration. However, this configuration
     * may be resolved.
     */
    runtimeClasspath,

    /**
     * The annotation processors used during the compilation of the tests of this module.
     *
     * <p>These dependencies are not accessible to the user at compile-time or at runtime directly.
     */
    testAnnotationProcessor,

    /**
     * Dependencies on which the Java module tests rely for implementation.
     *
     * <p>The dependencies are available at compile-time of the test code and at the test runtime.
     */
    testImplementation,

    /**
     * All the dependencies included for the Java module tests compilation.
     *
     * <p>Users cannot add dependencies directly to this configuration. However, this configuration
     * may be resolved.
     */
    testCompileClasspath,

    /**
     * Dependencies available at test runtime but not at compile-time.
     *
     * <p>For example, JUnit runners may be depended on with this configuration.
     */
    testRuntimeOnly,

    /**
     * All the dependencies included for the Java module test runtime.
     *
     * <p>Users cannot add dependencies directly to this configuration. However, this configuration
     * may be resolved.
     */
    testRuntimeClasspath,

    /**
     * Configuration that allows to compile {@code .proto} files form the dependencies.
     *
     * <p>Users should use {@code compile} when adding Protobuf dependencies to other strictly
     * Protobuf modules.
     */
    protobuf,

    /**
     * A Spine-specific configuration used to download and resolve artifacts.
     */
    @Internal
    fetch,

    /**
     * The {@code compile} configuration.
     *
     * @deprecated Deprecated since Gradle 5.0. Use {@link #implementation} or {@link #api} instead.
     */
    @Deprecated
    compile;

    public String value() {
        return name();
    }
}
