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

package io.spine.tools.protojs.field.checker;

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.testing.UtilityClassTest;
import io.spine.tools.protojs.code.JsOutput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.testing.Verify.assertInstanceOf;
import static io.spine.tools.protojs.field.checker.FieldCheckers.checkerFor;
import static io.spine.tools.protojs.given.Given.messageField;
import static io.spine.tools.protojs.given.Given.primitiveField;
import static io.spine.tools.protojs.given.Given.timestampField;

/**
 * @author Dmytro Kuzmin
 */
@DisplayName("FieldCheckers utility should")
class FieldCheckersTest extends UtilityClassTest<FieldCheckers> {

    private JsOutput jsOutput;

    FieldCheckersTest() {
        super(FieldCheckers.class);
    }

    @Override
    protected void setDefaults(NullPointerTester tester) {
        tester.setDefault(FieldDescriptor.class, messageField());
    }

    @BeforeEach
    void setUp() {
        jsOutput = new JsOutput();
    }

    @Test
    @DisplayName("create checker for primitive field")
    void createForPrimitive() {
        FieldChecker checker = checkerFor(primitiveField(), jsOutput);
        assertInstanceOf(PrimitiveFieldChecker.class, checker);
    }

    @Test
    @DisplayName("create checker for message field")
    void createForMessage() {
        FieldChecker checker = checkerFor(messageField(), jsOutput);
        assertInstanceOf(MessageFieldChecker.class, checker);
    }

    @Test
    @DisplayName("create message checker for standard type field")
    void createForWellKnown() {
        FieldChecker checker = checkerFor(timestampField(), jsOutput);
        assertInstanceOf(MessageFieldChecker.class, checker);
    }
}
