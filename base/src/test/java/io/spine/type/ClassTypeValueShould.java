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

package io.spine.type;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import com.google.common.testing.SerializableTester;
import org.junit.Test;

import java.util.Objects;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("SerializableInnerClassWithNonSerializableOuterClass")
public class ClassTypeValueShould {

    private final Class<?> cls = getClass();
    private final ClassTypeValue<?> classTypeValue =  new AClassValue(cls);

    @Test
    public void return_enclosed_value() {
        assertEquals(cls, classTypeValue.value());
    }

    @Test
    public void return_java_class_name() {
        assertEquals(ClassName.of(cls), classTypeValue.getClassName());
    }

    @Test
    public void have_string_form_with_the_name_of_the_enclosed_class() {
        assertEquals(cls.getName(), classTypeValue.toString());
    }

    @Test
    public void return_hash_code() {
        assertEquals(Objects.hash(cls), classTypeValue.hashCode());
    }

    @Test
    public void pass_null_tolerance_check() {
        new NullPointerTester().testAllPublicInstanceMethods(classTypeValue);
    }

    @Test
    public void override_equals() {
        new EqualsTester().addEqualityGroup(new AClassValue(cls), new AClassValue(cls))
                          .testEquals();
    }

    @Test
    public void serialize() {
        SerializableTester.reserializeAndAssert(new AClassValue(Void.class));
    }

    private static class AClassValue extends ClassTypeValue<Object> {

        private static final long serialVersionUID = 0L;

        private AClassValue(Class<?> value) {
            super(value);
        }
    }
}
