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
import com.squareup.javapoet.ClassName;
import org.junit.Test;

import static io.spine.test.Tests.assertHasPrivateParameterlessCtor;
import static io.spine.tools.compiler.validation.MethodConstructors.createConvertSingularValue;
import static io.spine.tools.compiler.validation.MethodConstructors.createDescriptorStatement;
import static io.spine.tools.compiler.validation.MethodConstructors.createValidateStatement;
import static org.junit.Assert.assertNotNull;

/**
 * @author Illia Shepilov
 */
public class MethodConstructorsShould {

    private static final String TEST_VALUE = "testValue";

    @Test
    public void return_constructed_descriptor_statement() {
        String result = createDescriptorStatement(0, ClassName.get(getClass()));
        assertNotNull(result);
    }

    @Test
    public void return_constructed_validate_statement(){
        String result = createValidateStatement(TEST_VALUE);
        assertNotNull(result);
    }

    @Test
    public void return_constructed_converted_value_statement() {
        String result = createConvertSingularValue(TEST_VALUE);
        assertNotNull(result);
    }

    @Test
    public void return_validate_statement() {
        String result = createConvertSingularValue(TEST_VALUE);
        assertNotNull(result);
    }

    @Test
    public void have_private_constructor() {
        assertHasPrivateParameterlessCtor(MethodConstructors.class);
    }

    @Test
    public void pass_null_tolerance_check() {
        new NullPointerTester().testStaticMethods(MethodConstructors.class,
                                                  NullPointerTester.Visibility.PACKAGE);
    }
}
