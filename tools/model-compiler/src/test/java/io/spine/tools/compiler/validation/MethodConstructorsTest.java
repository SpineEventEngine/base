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

package io.spine.tools.compiler.validation;

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import io.spine.code.proto.FieldName;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.tools.compiler.validation.MethodConstructors.createConvertSingularValue;
import static io.spine.tools.compiler.validation.MethodConstructors.createDescriptorStatement;
import static io.spine.tools.compiler.validation.MethodConstructors.createValidateStatement;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("MethodConstructors utility class should")
class MethodConstructorsTest extends UtilityClassTest<MethodConstructors> {

    private static final String TEST_VALUE = "testValue";

    MethodConstructorsTest() {
        super(MethodConstructors.class);
    }

    @Test
    @DisplayName("return constructed description statement")
    void return_constructed_descriptor_statement() {
        ClassName message = ClassName.get(getClass());
        ClassName fieldDescriptor = ClassName.get(FieldDescriptor.class);
        String result = createDescriptorStatement(0, message);
        String expected = format("%s fieldDescriptor = %s.getDescriptor().getFields().get(0)",
                                 fieldDescriptor, message);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("return constructed validate statement")
    void return_constructed_validate_statement() {
        String name = "var";
        String result = createValidateStatement(TEST_VALUE, name);
        String expected = format("validate(fieldDescriptor, %s, \"%s\")", TEST_VALUE, name);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("return constructed converted value statement")
    void return_constructed_converted_value_statement() {
        TypeName type = ClassName.get(getClass());
        String convertedVariableSuffix = FieldName.of(TEST_VALUE)
                                                  .toCamelCase();
        String result = createConvertSingularValue(TEST_VALUE, type);
        String expected = format("%s converted%s = convert(%s, %s.class)",
                                 type, convertedVariableSuffix, TEST_VALUE, type);
        assertEquals(expected, result);
    }

    @Override
    protected void configure(NullPointerTester tester) {
        super.configure(tester);
        tester.testStaticMethods(MethodConstructors.class,
                                 NullPointerTester.Visibility.PACKAGE);
    }
}
