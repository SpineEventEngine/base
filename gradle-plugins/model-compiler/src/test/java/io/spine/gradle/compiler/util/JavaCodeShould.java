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
package io.spine.gradle.compiler.util;

import com.google.common.testing.NullPointerTester;
import org.junit.Test;

import static io.spine.gradle.compiler.util.JavaCode.toJavaFieldName;
import static io.spine.test.Tests.assertHasPrivateParameterlessCtor;
import static org.junit.Assert.assertEquals;

/**
 * @author Alexander Yevsyukov
 */
public class JavaCodeShould {

    private static final String PROTO_FIELD_NAME = "correct_java_name";

    @SuppressWarnings("DuplicateStringLiteralInspection")
    @Test
    public void calculate_outer_class_name() {
        assertEquals("Failures", JavaCode.toCamelCase("failures"));
        assertEquals("ManyFailures", JavaCode.toCamelCase("many_failures"));
        assertEquals("ManyMoreFailures", JavaCode.toCamelCase("many_more_failures"));
    }

    @Test
    public void return_correct_java_field_name() {
        final String expected = "correctJavaName";
        final String actual = toJavaFieldName(PROTO_FIELD_NAME, false);
        assertEquals(expected, actual);
    }

    @Test
    public void return_correct_capitalized_java_name() {
        final String expected = "CorrectJavaName";
        final String actual = toJavaFieldName(PROTO_FIELD_NAME, true);
        assertEquals(expected, actual);
    }

    @Test
    public void have_private_constructor() {
        assertHasPrivateParameterlessCtor(JavaCode.class);
    }

    @Test
    public void pass_null_tolerance_check() {
        new NullPointerTester().testStaticMethods(JavaCode.class,
                                                  NullPointerTester.Visibility.PACKAGE);
    }
}
