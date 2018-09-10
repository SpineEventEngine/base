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

package io.spine.tools.protojs.knowntypes;

import io.spine.code.proto.FileSet;
import io.spine.tools.protojs.code.JsGenerator;
import io.spine.tools.protojs.code.JsOutput;
import io.spine.tools.protojs.given.Given.PreparedProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.testing.Verify.assertContains;
import static io.spine.tools.protojs.given.Generators.assertGeneratedCodeContains;
import static io.spine.tools.protojs.given.Given.preparedProject;

@DisplayName("KnownTypesGenerator should")
class KnownTypesGeneratorTest {

    private JsGenerator jsGenerator;
    private KnownTypesGenerator generator;

    @BeforeEach
    void setUp() {
        PreparedProject project = preparedProject();
        FileSet fileSet = project.fileSet();
        jsGenerator = new JsGenerator();
        generator = new KnownTypesGenerator(fileSet, jsGenerator);
    }

    @Test
    @DisplayName("generate imports for known types")
    void generateImports() {
        generator.generateImports();
        String taskImport = "require('./task_pb.js');";
        assertGeneratedCodeContains(jsGenerator, taskImport);
    }

    @Test
    @DisplayName("generate known types map")
    void generateKnownTypesMap() {
        generator.generateKnownTypesMap();
        String mapEntry = "['type.spine.io/spine.sample.protojs.TaskId', " +
                "proto.spine.sample.protojs.TaskId]";
        assertGeneratedCodeContains(jsGenerator, mapEntry);
    }
}
