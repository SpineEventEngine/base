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
import com.google.common.testing.NullPointerTester.Visibility;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.squareup.javapoet.ClassName;
import io.spine.tools.compiler.MessageTypeCache;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.getDefaultInstance;
import static io.spine.testing.Tests.assertHasPrivateParameterlessCtor;
import static io.spine.tools.compiler.validation.ClassNames.getClassName;
import static io.spine.tools.compiler.validation.ClassNames.getStringClassName;
import static io.spine.tools.compiler.validation.ClassNames.getValidatorMessageClassName;
import static org.junit.Assert.assertEquals;

public class ClassNamesShould {

    private static final String TEST_PACKAGE = ClassNamesShould.class.getPackage()
                                                                     .getName();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void return_string_class_name() {
        ClassName actual = getStringClassName();
        assertEquals(ClassName.get(String.class), actual);
    }

    @Test
    public void throw_exception_when_generic_class_name_is_not_found() {
        MessageTypeCache messageTypeCache = new MessageTypeCache();
        thrown.expect(IllegalArgumentException.class);
        getValidatorMessageClassName(TEST_PACKAGE, messageTypeCache, "field");
    }

    @Test
    public void return_constructed_class_name() {
        ClassName actual = getClassName(TEST_PACKAGE, getClass().getSimpleName());
        assertEquals(ClassName.get(getClass()), actual);
    }

    @Test
    public void have_utility_constructor() {
        assertHasPrivateParameterlessCtor(ClassNames.class);
    }

    @Test
    public void pass_null_tolerance_check() {
        new NullPointerTester()
                .setDefault(FieldDescriptorProto.class, getDefaultInstance())
                .testStaticMethods(ClassNames.class, Visibility.PACKAGE);
    }
}
