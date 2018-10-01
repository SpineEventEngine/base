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

package io.spine.js.generate.field.precondition;

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.js.generate.JsOutput;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.js.generate.field.precondition.FieldPreconditions.preconditionFor;
import static io.spine.js.generate.given.Given.messageField;
import static io.spine.js.generate.given.Given.primitiveField;
import static io.spine.js.generate.given.Given.timestampField;

@DisplayName("FieldPreconditions utility should")
class FieldPreconditionsTest extends UtilityClassTest<FieldPreconditions> {

    private JsOutput jsOutput;

    FieldPreconditionsTest() {
        super(FieldPreconditions.class);
    }

    @Override
    protected void configure(NullPointerTester tester) {
        tester.setDefault(FieldDescriptor.class, messageField());
    }

    @BeforeEach
    void setUp() {
        jsOutput = new JsOutput();
    }

    @Test
    @DisplayName("create precondition for primitive field")
    void createForPrimitive() {
        FieldPrecondition precondition = preconditionFor(primitiveField(), jsOutput);
        assertThat(precondition).isInstanceOf(PrimitivePrecondition.class);
    }

    @Test
    @DisplayName("create precondition for message field")
    void createForMessage() {
        FieldPrecondition precondition = preconditionFor(messageField(), jsOutput);
        assertThat(precondition).isInstanceOf(MessagePrecondition.class);
    }

    @Test
    @DisplayName("create message precondition for standard type field")
    void createForWellKnown() {
        FieldPrecondition precondition = preconditionFor(timestampField(), jsOutput);
        assertThat(precondition).isInstanceOf(MessagePrecondition.class);
    }
}
