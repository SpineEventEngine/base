/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

package io.spine.gradle.compiler.validate;

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.DescriptorProtos;
import com.squareup.javapoet.ClassName;
import io.spine.gradle.compiler.message.MessageTypeCache;
import org.junit.Test;

import static io.spine.gradle.compiler.validate.ClassNames.getClassName;
import static io.spine.gradle.compiler.validate.ClassNames.getStringClassName;
import static io.spine.gradle.compiler.validate.ClassNames.getValidatorMessageClassName;
import static io.spine.test.Tests.assertHasPrivateParameterlessCtor;
import static org.junit.Assert.assertEquals;

/**
 * @author Illia Shepilov
 */
public class ClassNamesShould {

    public static final String TEST_PACKAGE = "io.spine.gradle.compiler.validate";

    @Test
    public void return_string_class_name() {
        final ClassName actual = getStringClassName();
        assertEquals(ClassName.get(String.class), actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throw_exception_when_generic_class_name_is_not_found() {
        final MessageTypeCache messageTypeCache = new MessageTypeCache();
        getValidatorMessageClassName(TEST_PACKAGE, messageTypeCache, "field");
    }

    @Test
    public void return_constructed_class_name() {
        final ClassName actual = getClassName(TEST_PACKAGE, "ClassNamesShould");
        assertEquals(ClassName.get(getClass()), actual);
    }

    @Test
    public void have_private_constructor() {
        assertHasPrivateParameterlessCtor(ClassNames.class);
    }

    @Test
    public void pass_null_tolerance_check() {
        final NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.setDefault(DescriptorProtos.FieldDescriptorProto.class,
                                     DescriptorProtos.FieldDescriptorProto.getDefaultInstance());
        nullPointerTester.testStaticMethods(ClassNames.class, NullPointerTester.Visibility.PACKAGE);
    }
}
