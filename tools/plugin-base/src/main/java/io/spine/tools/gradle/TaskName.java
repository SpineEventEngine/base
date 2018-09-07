/*
 * Copyright 2018, TeamDev. All rights reserved.
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

import com.google.common.base.MoreObjects;

/**
 * Task names in Gradle build lifecycle.
 *
 * <p>Spine `tools` library uses some of 3rd-party Gradle tasks as anchors for own execution.
 *
 * @author Alex Tymchenko
 */
public enum TaskName {

    /*
     * Gradle-own and 3rd-party task names
     **************************************/

    CLEAN("clean"),

    BUILD("build"),

    COMPILE_JAVA("compileJava"),
    COMPILE_TEST_JAVA("compileTestJava"),
    CLASSES("classes"),

    GENERATE_PROTO("generateProto"),
    GENERATE_TEST_PROTO("generateTestProto"),

    PROCESS_RESOURCES("processResources"),
    PROCESS_TEST_RESOURCES("processTestResources"),

    /*
     * Spine custom task names
     **************************/

    /**
     * The name of the additional cleanup task added to the Gradle lifecycle.
     */
    PRE_CLEAN("preClean"),

    /**
     * The name of the task that checks for wrong FQN naming in javadocs.
     *
     * <p>Relates only to {@code main} classes and resources scope.
     */
    CHECK_FQN("checkJavadocLink"),

    /**
     * The name of the task that checks for allowed line length.
     *
     * <p>Relates only to {@code main} classes and resources scope.
     */
    CHECK_RIGHT_MARGIN_WRAPPING("checkRightMarginWrapping"),

    /**
     * The name of the rejection generation task added to the Gradle lifecycle.
     *
     * <p>Handles the {@code main} classes and resources scope.
     */
    GENERATE_REJECTIONS("generateRejections"),

    /**
     * The name of the rejection generation task added to the Gradle lifecycle.
     *
     * <p>Handles the {@code test} classes and resources scope.
     */
    GENERATE_TEST_REJECTIONS("generateTestRejections"),

    /**
     * The name of the validating builder generation task added to the Gradle lifecycle.
     *
     * <p>Handles the {@code test} classes and resources scope.
     */
    GENERATE_VALIDATING_BUILDERS("generateValidatingBuilders"),

    /**
     * The name of the validating builder generation task added to the Gradle lifecycle.
     *
     * <p>Handles the {@code main} classes and resources scope.
     */
    GENERATE_TEST_VALIDATING_BUILDERS("generateTestValidatingBuilders"),

    /**
     * The name of the enrichment lookup task added to the Gradle lifecycle.
     *
     * <p>Handles the {@code main} classes and resources scope.
     */
    FIND_ENRICHMENTS("findEnrichments"),

    /**
     * The name of the enrichment lookup task added to the Gradle lifecycle.
     *
     * <p>Handles the {@code test} classes and resources scope.
     */
    FIND_TEST_ENRICHMENTS("findTestEnrichments"),

    /**
     * The name of the validation rules lookup task added to the Gradle lifecycle.
     *
     * <p>Relates to {@code main} classes and resources scope.
     */
    FIND_VALIDATION_RULES("findValidationRules"),

    /**
     * The name of the validation rules lookup task added to the Gradle lifecycle.
     *
     * <p>Relates to {@code test} classes and resources scope.
     */
    FIND_TEST_VALIDATION_RULES("findTestValidationRules"),

    /**
     * The name of the {@code .proto}-to-Java mapping task added to the Gradle lifecycle.
     *
     * <p>Handles the {@code main} classes and resources scope.
     */
    MAP_PROTO_TO_JAVA("mapProtoToJava"),

    /**
     * The name of the {@code .proto}-to-Java mapping task added to the Gradle lifecycle.
     *
     * <p>Handles the {@code test} classes and resources scope.
     */
    MAP_TEST_PROTO_TO_JAVA("mapTestProtoToJava"),

    /**
     * The name of the class path scan task added to the Gradle lifecycle.
     */
    SCAN_CLASS_PATH("scanClassPath"),

    /**
     * The name of the task, that annotates the Java sources generated from {@code .proto} files,
     * added to the Gradle lifecycle.
     *
     * <p>Handles the {@code main} classes and resources scope.
     */
    ANNOTATE_PROTO("annotateProto"),

    LINK_CLIENT_PROTO("linkClientProto"),
    INSTALL_DEPENDENCIES("installDependencies"),

    /**
     * The name of the task, that annotates the Java sources generated from {@code .proto} files,
     * added to the Gradle lifecycle.
     *
     * <p>Handles the {@code test} classes and resources scope.
     */
    ANNOTATE_TEST_PROTO("annotateTestProto"),

    /**
     * The name of the task, that formats Javadocs in sources generated from {@code .proto}
     * files, added to the Gradle lifecycle.
     *
     * <p>Handles the {@code main} classes and resources scope.
     */
    FORMAT_PROTO_DOC("formatProtoDoc"),

    /**
     * The name of the task, that formats Javadocs in sources generated from {@code .proto}
     * files, added to the Gradle lifecycle.
     *
     * <p>Handles the {@code test} classes and resources scope.
     */
    FORMAT_TEST_PROTO_DOC("formatTestProtoDoc"),

    /**
     * The name of the task, that checks if the defined model matches the rules of Spine.
     *
     * <p>Handles the {@code main} classes and resources scope.
     */
    VERIFY_MODEL("verifyModel"),

    /**
     * The name of the task, that merges all the module known type descriptors into one.
     *
     * <p>Handles the {@code main} classes and resources scope.
     */
    MERGE_DESCRIPTOR_SET("mergeDescriptorSet"),

    /**
     * The name of the task, that merges all the module known type descriptors into one.
     *
     * <p>Handles the {@code test} classes and resources scope.
     */
    MERGE_TEST_DESCRIPTOR_SET("mergeTestDescriptorSet"),

    ADD_FROM_JSON("addFromJson"),

    COMPILE_PROTO_TO_JS("compileProtoToJs"),

    COPY_MODULE_SOURCES("copyModuleSources");

    private final String value;

    TaskName(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("val", value)
                          .toString();
    }
}
