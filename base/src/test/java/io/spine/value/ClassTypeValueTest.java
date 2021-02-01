/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.value;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.testing.SerializableTester.reserializeAndAssert;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("ClassTypeValue should")
class ClassTypeValueTest {

    private final Class<?> cls = getClass();
    private final ClassTypeValue<?> classTypeValue =  new AClassValue(cls);

    @Test
    @DisplayName("return enclosed value")
    void enclosedValue() {
        assertEquals(cls, classTypeValue.value());
    }

    @Test
    @DisplayName("give enclosed class name in toString()")
    void classNameInString() {
        assertEquals(cls.getName(), classTypeValue.toString());
    }

    @Test
    @DisplayName("be equal to another with the same class value")
    void equality() {
        new EqualsTester().addEqualityGroup(new AClassValue(cls), new AClassValue(cls))
                          .addEqualityGroup(new AClassValue(Boolean.class))
                          .testEquals();
    }
    
    @Test
    @DisplayName("be serializable")
    void serialize() {
        reserializeAndAssert(new AClassValue(Void.class));
    }

    private static class AClassValue extends ClassTypeValue<Object> {

        private static final long serialVersionUID = 0L;

        private AClassValue(Class<?> value) {
            super(value);
        }
    }
}
