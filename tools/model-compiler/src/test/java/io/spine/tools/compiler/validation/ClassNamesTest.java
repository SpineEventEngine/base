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
import io.spine.testing.UtilityClassTest;
import io.spine.tools.compiler.TypeCache;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.getDefaultInstance;
import static io.spine.tools.compiler.validation.ClassNames.getClassName;
import static io.spine.tools.compiler.validation.ClassNames.getStringClassName;
import static io.spine.tools.compiler.validation.ClassNames.getValidatorMessageClassName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ClassNames utility class should")
class ClassNamesTest extends UtilityClassTest<ClassNames> {

    private static final String TEST_PACKAGE = ClassNamesTest.class.getPackage()
                                                                   .getName();

    ClassNamesTest() {
        super(ClassNames.class);
    }

    @Test
    @DisplayName("return ClassName for String")
    void return_string_class_name() {
        ClassName actual = getStringClassName();
        assertEquals(ClassName.get(String.class), actual);
    }

    @Test
    @DisplayName("throw when generic class name is not found")
    void throw_exception_when_generic_class_name_is_not_found() {
        TypeCache typeCache = new TypeCache();
        assertThrows(IllegalArgumentException.class,
                     () -> getValidatorMessageClassName(TEST_PACKAGE, typeCache, "field"));
    }

    @Test
    @DisplayName("return constructed class name")
    void return_constructed_class_name() {
        ClassName actual = getClassName(TEST_PACKAGE, getClass().getSimpleName());
        assertEquals(ClassName.get(getClass()), actual);
    }

    @Override
    protected void configure(NullPointerTester tester) {
        super.configure(tester);
        tester.setDefault(FieldDescriptorProto.class, getDefaultInstance())
              .testStaticMethods(ClassNames.class, Visibility.PACKAGE);
    }
}
