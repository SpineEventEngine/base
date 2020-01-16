/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.js.generate.field;

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.js.generate.output.CodeLines;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.js.generate.field.given.Given.mapField;
import static io.spine.js.generate.field.given.Given.messageField;
import static io.spine.js.generate.field.given.Given.repeatedField;

@DisplayName("FieldGenerators utility should")
class FieldGeneratorsTest extends UtilityClassTest<FieldGenerators> {

    private CodeLines jsOutput;

    FieldGeneratorsTest() {
        super(FieldGenerators.class);
    }

    @Override
    protected void configure(NullPointerTester tester) {
        tester.setDefault(FieldToParse.class, fieldToParse(messageField()));
    }

    @BeforeEach
    void setUp() {
        jsOutput = new CodeLines();
    }

    @Test
    @DisplayName("create singular field generator for ordinary Protobuf field")
    void createForSingular() {
        FieldGenerator generator = generatorFor(messageField());
        assertThat(generator).isInstanceOf(SingularFieldGenerator.class);
    }

    @Test
    @DisplayName("create repeated field generator for repeated Protobuf field")
    void createForRepeated() {
        FieldGenerator generator = generatorFor(repeatedField());
        assertThat(generator).isInstanceOf(RepeatedFieldGenerator.class);
    }

    @Test
    @DisplayName("create map field generator for map Protobuf field")
    void createForMap() {
        FieldGenerator generator = generatorFor(mapField());
        assertThat(generator).isInstanceOf(MapFieldGenerator.class);
    }

    private FieldGenerator generatorFor(FieldDescriptor field) {
        FieldToParse fieldToParse = fieldToParse(field);
        return FieldGenerators.createFor(fieldToParse, jsOutput);
    }

    private static FieldToParse fieldToParse(FieldDescriptor field) {
        return new FieldToParse(field, "inputVar", "outputVar");
    }
}
