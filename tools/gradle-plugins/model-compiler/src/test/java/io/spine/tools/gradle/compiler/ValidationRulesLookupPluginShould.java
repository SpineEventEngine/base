/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

package io.spine.tools.gradle.compiler;

import io.spine.tools.DefaultProject;
import io.spine.tools.gradle.given.GradleProject;
import io.spine.tools.properties.PropertyFile;
import io.spine.validate.rules.ValidationRules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static io.spine.tools.gradle.TaskName.FIND_VALIDATION_RULES;
import static org.junit.Assert.assertEquals;

/**
 * @author Dmytro Grankin
 */
public class ValidationRulesLookupPluginShould {

    private static final char DOT = '.';
    private static final String PROJECT_NAME = "validation-rules-lookup-plugin-test";
    private static final String PROTO_FILE_PACKAGE = "test.valrule";
    private static final String OUTER_MESSAGE_TYPE = "Outer";
    private static final String VALIDATION_RULE_TYPE = "ValidationRule";
    private static final String VALIDATION_TARGET = PROTO_FILE_PACKAGE + DOT +
                                                    OUTER_MESSAGE_TYPE + DOT +
                                                    "field_name";
    private static final List<String> NESTED_VALIDATION_RULE_PROTO =
            Arrays.asList("syntax = \"proto3\";",
                          "package " + PROTO_FILE_PACKAGE + ';',
                          "import \"spine/options.proto\";",

                          "message " + OUTER_MESSAGE_TYPE + " {",

                                "message " + VALIDATION_RULE_TYPE + " {",
                                    "option (validation_of) = \"" + VALIDATION_TARGET + "\";",
                                "}",
                          "}"
            );

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();

    @Test
    public void findNestedValidationRules() {
        final String file = "nested_validation_rule.proto";
        final GradleProject project = newProjectWithFile(file, NESTED_VALIDATION_RULE_PROTO);
        project.executeTask(FIND_VALIDATION_RULES);

        final String expectedKey = PROTO_FILE_PACKAGE + DOT +
                                   OUTER_MESSAGE_TYPE + DOT +
                                   VALIDATION_RULE_TYPE;
        final String value = loadProperties().get(expectedKey);
        assertEquals(VALIDATION_TARGET, value);
    }

    private Map<String, String> loadProperties() {
        final PropertyFile propFile = PropertyFile.of(ValidationRules.fileName())
                                                  .at(DefaultProject.at(testProjectDir.getRoot())
                                                                    .generated()
                                                                    .mainResources());
        final Map<String, String> result = propFile.load();
        return result;
    }

    private GradleProject newProjectWithFile(String protoFileName, List<String> protoFileLines) {
        return GradleProject.newBuilder()
                            .setProjectName(PROJECT_NAME)
                            .setProjectFolder(testProjectDir)
                            .createProto(protoFileName, protoFileLines)
                            .build();
    }
}
