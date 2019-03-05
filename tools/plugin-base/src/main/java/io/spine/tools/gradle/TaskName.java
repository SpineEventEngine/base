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

/**
 * Task names in Gradle build lifecycle.
 *
 * <p>Spine `tools` library uses some of 3rd-party Gradle tasks as anchors for own execution.
 */
public enum TaskName {

    /*
     * Gradle-own and 3rd-party task names
     **************************************/

    clean,

    build,

    compileJava,
    compileTestJava,
    classes,

    generateProto,
    generateTestProto,

    processResources,
    processTestResources,

    /*
     * Tasks added by Spine
     ************************/

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
     * Generates the code of validating builders in the {@code main} scope.
     */
    generateValidatingBuilders,

    /**
     * Generates the code of validating builders in the {@code test} scope.
     */
    generateTestValidatingBuilders,

    /**
     * Collects external validation constraints in the {@code main} scope.
     */
    findValidationRules,

    /**
     * Collects external validation constraints in the {@code test} scope.
     */
    findTestValidationRules,

    /**
     * The name of the class path scan task added to the Gradle lifecycle.
     */
    scanClassPath,

    /**
     * Annotates the Java sources generated from {@code .proto} files the {@code main} scope.
     */
    annotateProto,

    /**
     * Annotates the Java sources generated from {@code .proto} files the {@code test} scope.
     */
    annotateTestProto,

    /**
     * Formats Javadocs in sources generated from {@code .proto} files in the {@code main} scope.
     */
    formatProtoDoc,

    /**
     * Formats Javadocs in sources generated from {@code .proto} files in the {@code test} scope.
     */
    formatTestProtoDoc,

    /**
     * Verifies correctness of the domain model definition.
     */
    verifyModel,

    /**
     * Merges all the module known type descriptors into one in the {@code main} scope.
     */
    mergeDescriptorSet,

    /**
     * Merges all the module known type descriptors into one in the {@code test} scope.
     */
    mergeTestDescriptorSet,

    /**
     * Generates JSON-parsing code for the JavaScript messages compiled from Protobuf in both
     * {@code main} and {@code test} scopes.
     */
    generateJsonParsers,

    copyPluginJar,

    writeDescriptorReference,

    writeTestDescriptorReference;

    /**
     * Obtains the name of the task.
     */
    public String value() {
        return name();
    }
}
