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

    clean(),

    build(),

    compileJava(),
    compileTestJava(),
    classes(),

    generateProto(),
    generateTestProto(),

    processResources(),
    processTestResources(),

    /*
     * Spine custom task names
     **************************/

    /**
     * The name of the additional cleanup task added to the Gradle lifecycle.
     */
    preClean(),

    /**
     * The name of the task that checks for wrong FQN naming in javadocs.
     *
     * <p>Relates only to {@code main} classes and resources scope.
     */
    checkJavadocLink(),

    /**
     * The name of the task that checks for allowed line length.
     *
     * <p>Relates only to {@code main} classes and resources scope.
     */
    checkRightMarginWrapping(),

    /**
     * The name of the rejection generation task added to the Gradle lifecycle.
     *
     * <p>Handles the {@code main} classes and resources scope.
     */
    generateRejections(),

    /**
     * The name of the rejection generation task added to the Gradle lifecycle.
     *
     * <p>Handles the {@code test} classes and resources scope.
     */
    generateTestRejections(),

    /**
     * The name of the validating builder generation task added to the Gradle lifecycle.
     *
     * <p>Handles the {@code test} classes and resources scope.
     */
    generateValidatingBuilders(),

    /**
     * The name of the validating builder generation task added to the Gradle lifecycle.
     *
     * <p>Handles the {@code main} classes and resources scope.
     */
    generateTestValidatingBuilders(),

    /**
     * The name of the enrichment lookup task added to the Gradle lifecycle.
     *
     * <p>Handles the {@code main} classes and resources scope.
     */
    findEnrichments(),

    /**
     * The name of the enrichment lookup task added to the Gradle lifecycle.
     *
     * <p>Handles the {@code test} classes and resources scope.
     */
    findTestEnrichments(),

    /**
     * The name of the validation rules lookup task added to the Gradle lifecycle.
     *
     * <p>Relates to {@code main} classes and resources scope.
     */
    findValidationRules(),

    /**
     * The name of the validation rules lookup task added to the Gradle lifecycle.
     *
     * <p>Relates to {@code test} classes and resources scope.
     */
    findTestValidationRules(),

    /**
     * The name of the {@code .proto}-to-Java mapping task added to the Gradle lifecycle.
     *
     * <p>Handles the {@code main} classes and resources scope.
     */
    mapProtoToJava(),

    /**
     * The name of the {@code .proto}-to-Java mapping task added to the Gradle lifecycle.
     *
     * <p>Handles the {@code test} classes and resources scope.
     */
    mapTestProtoToJava(),

    /**
     * The name of the class path scan task added to the Gradle lifecycle.
     */
    scanClassPath(),

    /**
     * The name of the task, that annotates the Java sources generated from {@code .proto} files,
     * added to the Gradle lifecycle.
     *
     * <p>Handles the {@code main} classes and resources scope.
     */
    annotateProto(),

    /**
     * The name of the task, that annotates the Java sources generated from {@code .proto} files,
     * added to the Gradle lifecycle.
     *
     * <p>Handles the {@code test} classes and resources scope.
     */
    annotateTestProto(),

    /**
     * The name of the task, that formats Javadocs in sources generated from {@code .proto}
     * files, added to the Gradle lifecycle.
     *
     * <p>Handles the {@code main} classes and resources scope.
     */
    formatProtoDoc(),

    /**
     * The name of the task, that formats Javadocs in sources generated from {@code .proto}
     * files, added to the Gradle lifecycle.
     *
     * <p>Handles the {@code test} classes and resources scope.
     */
    formatTestProtoDoc(),

    /**
     * The name of the task, that checks if the defined model matches the rules of Spine.
     *
     * <p>Handles the {@code main} classes and resources scope.
     */
    verifyModel(),

    /**
     * The name of the task, that merges all the module known type descriptors into one.
     *
     * <p>Handles the {@code main} classes and resources scope.
     */
    mergeDescriptorSet(),

    /**
     * The name of the task, that merges all the module known type descriptors into one.
     *
     * <p>Handles the {@code test} classes and resources scope.
     */
    mergeTestDescriptorSet(),

    /**
     * The name of the task, that generates JSON-parsing code for the JavaScript messages compiled
     * from Protobuf.
     *
     * <p>Handles both {@code main} and {@code test} classes and resources scope.
     */
    generateJsonParsers(),

    copyPluginJar(),

    writeDescriptorReference(),

    writeTestDescriptorReference();

    public String value() {
        return name();
    }
}
