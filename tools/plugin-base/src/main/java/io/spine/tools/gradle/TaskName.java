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
package io.spine.tools.gradle;

import io.spine.annotation.Internal;

/**
 * Names of well-known Gradle tasks.
 *
 * <p>This enumeration includes the tasks from:
 * <ul>
 *     <li><a href="https://docs.gradle.org/current/userguide/base_plugin.html#sec:base_tasks">the {@code base} plugin</a>;
 *     <li><a href="https://docs.gradle.org/current/userguide/java_plugin.html#sec:java_tasks">the {@code java} plugin</a>;
 *     <li><a href="https://github.com/google/protobuf-gradle-plugin">the {@code com.google.protobuf} plugin</a>;
 *     <li>the Spine Model Compiler plugin;
 *     <li>the Spine Model Verifier plugin;
 *     <li>the Spine ProtoJS plugin;
 *     <li>the Spine ProtoDart plugin.
 * </ul>
 */
@Internal
public enum TaskName {

    /**
     * Deletes the temporary build artifacts.
     *
     * <p>Defined by the {@code base} plugin.
     */
    clean,

    /**
     * Aggregate task that assembles all the artifacts of this project.
     *
     * <p>Defined by the {@code base} plugin.
     */
    assemble,

    /**
     * A lifecycle task which marks the project verification routines, such as static code analysis,
     * executing tests, etc.
     *
     * <p>Defined by the {@code base} plugin.
     */
    check,

    /**
     * A lifecycle task which builds everything in the project, including running tests, producing
     * production artifacts, and generating documentation.
     *
     * <p>Defined by the {@code base} plugin.
     */
    build,

    /**
     * Compiles production Java source files using the JDK compiler.
     *
     * <p>Defined by the {@code java} plugin.
     */
    compileJava,

    /**
     * Compiles test Java source files using the JDK compiler.
     *
     * <p>Defined by the {@code java} plugin.
     */
    compileTestJava,

    /**
     * A lifecycle task which marks processing of all the classes and resources in this project.
     *
     * <p>Defined by the {@code java} plugin.
     */
    classes,

    /**
     * A lifecycle task which marks processing of all the test classes and resources in this
     * project.
     *
     * <p>Defined by the {@code java} plugin.
     */
    testClasses,

    /**
     * Copies production resources into the production resources directory.
     *
     * <p>Defined by the {@code java} plugin.
     */
    processResources,

    /**
     * Copies test resources into the test resources directory.
     *
     * <p>Defined by the {@code java} plugin.
     */
    processTestResources,

    /**
     * Generates production code from Protobuf.
     *
     * <p>Note that this task is not a public API of the plugin. Users should be conscious and
     * cautious when depending on it.
     *
     * <p>Defined by the Protobuf Gradle plugin.
     */
    generateProto,

    /**
     * Generates test code from Protobuf.
     *
     * <p>Note that this task is not a public API of the plugin. Users should be conscious and
     * cautious when depending on it.
     *
     * <p>Defined by the Protobuf Gradle plugin.
     */
    generateTestProto,

    /**
     * Additional cleanup task added to the Gradle lifecycle.
     * 
     * <p>Defined by the Spine Model Compiler plugin.
     */
    preClean,

    /**
     * Generates source code of rejections in the {@code main} scope.
     * 
     * <p>Defined by the Spine Model Compiler plugin.
     */
    generateRejections,

    /**
     * Generates source code of rejections in the {@code test} scope.
     * 
     * <p>Defined by the Spine Model Compiler plugin.
     */
    generateTestRejections,

    /**
     * Annotates the Java sources generated from {@code .proto} files the {@code main} scope.
     * 
     * <p>Defined by the Spine Model Compiler plugin.
     */
    annotateProto,

    /**
     * Annotates the Java sources generated from {@code .proto} files the {@code test} scope.
     * 
     * <p>Defined by the Spine Model Compiler plugin.
     */
    annotateTestProto,

    /**
     * Formats Javadocs in sources generated from {@code .proto} files in the {@code main} scope.
     * 
     * <p>Defined by the Spine Model Compiler plugin.
     */
    formatProtoDoc,

    /**
     * Formats Javadocs in sources generated from {@code .proto} files in the {@code test} scope.
     * 
     * <p>Defined by the Spine Model Compiler plugin.
     */
    formatTestProtoDoc,

    /**
     * Merges all the known type descriptors of the module into one in the {@code main} scope.
     * 
     * <p>Defined by the Spine Model Compiler plugin.
     */
    mergeDescriptorSet,

    /**
     * Merges all the known type descriptors of the module into one in the {@code test} scope.
     * 
     * <p>Defined by the Spine Model Compiler plugin.
     */
    mergeTestDescriptorSet,

    /**
     * Downloads the Protobuf compiler plugin executable JAR into the required paths in the project.
     *
     * <p>Defined by the Spine Model Compiler plugin.
     */
    copyPluginJar,

    /**
     * Creates the Protobuf compiler plugin configuration.
     *
     * <p>Works only with the {@code main} scope.
     *
     * <p>Defined by the Spine Model Compiler plugin.
     */
    writePluginConfiguration,

    /**
     * Creates the Protobuf compiler plugin test configuration.
     *
     * <p>Defined by the Spine Model Compiler plugin.
     */
    writeTestPluginConfiguration,

    /**
     * Creates the {@code desc.ref} file containing the reference to the descriptor file(s) with
     * the known types.
     *
     * <p>Works only with the {@code main} scope descriptors.
     *
     * <p>Defined by the Spine Model Compiler plugin.
     */
    writeDescriptorReference,

    /**
     * Creates the {@code desc.ref} file containing the reference to the descriptor file(s) with
     * the known types.
     *
     * <p>Works only with the {@code test} scope descriptors.
     *
     * <p>Defined by the Spine Model Compiler plugin.
     */
    writeTestDescriptorReference,

    /**
     * Verifies correctness of the domain model definition.
     *
     * <p>Defined by the Spine Model Verifier plugin.
     */
    verifyModel,

    /**
     * Generates JSON-parsing code for the JavaScript messages compiled from Protobuf in both
     * {@code main} and {@code test} scopes.
     *
     * <p>Defined by the Spine ProtoJs plugin.
     */
    generateJsonParsers,

    /**
     * Creates the {@code types.dart} file which contains type mapping for all the production
     * Protobuf types defined in this project.
     *
     * <p>Works only with the {@code main} scope types.
     * 
     * <p>Defined by the Spine ProtoDart plugin.
     */
    generateDartTypeRegistry,

    /**
     * Creates the {@code types.dart} file which contains type mapping for all the test Protobuf
     * types defined in this project.
     *
     * <p>Works only with the {@code test} scope types.
     *
     * <p>Defined by the Spine ProtoDart plugin.
     */
    generateDartTestTypeRegistry,

    /**
     * Copies the Dart code generated from Protobuf from its temporary location to the {@code lib}
     * directory.
     *
     * <p>Works only with the {@code main} scope files.
     *
     * <p>Defined by the Spine ProtoDart plugin.
     */
    copyGeneratedDart,

    /**
     * Copies the Dart code generated from Protobuf from its temporary location to the {@code test}
     * directory.
     *
     * <p>Works only with the {@code test} scope files.
     *
     * <p>Defined by the Spine ProtoDart plugin.
     */
    copyTestGeneratedDart;

    /**
     * Obtains the name of the task.
     */
    public String value() {
        return name();
    }

    /**
     * Obtains this task name as a path.
     *
     * <p>It is expected that the referred task belongs to the root project (a.k.a {@code :}).
     *
     * @return the name with a colon symbol ({@code :}) at the beginning
     */
    public String path() {
        return ':' + name();
    }
}
