/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.reflect;

import io.spine.reflect.given.MethodsTestEnv.ClassWithPrivateMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("`Methods` utility should")
class MethodsTest {

    private Method method;

    @BeforeEach
    void obtainMethod() throws NoSuchMethodException {
        method = ClassWithPrivateMethod.class.getDeclaredMethod("privateMethod");
    }

    @Test
    @DisplayName("set the method accessible and invoke")
    void setAccessibleAndInvoke() {
        ClassWithPrivateMethod target = new ClassWithPrivateMethod();
        Object result = Methods.setAccessibleAndInvoke(method, target);

        assertThat(result).isEqualTo(ClassWithPrivateMethod.METHOD_RESULT);
    }

    @SuppressWarnings("CheckReturnValue") // Called to throw exception.
    @Test
    @DisplayName("throw `IAE` if the given target is not a valid invocation target")
    void throwOnInvalidTarget() {
        Object wrongTarget = new Object();

        assertThrows(IllegalArgumentException.class,
                     () -> Methods.setAccessibleAndInvoke(method, wrongTarget));
    }

    @SuppressWarnings("CheckReturnValue") // Called to throw exception.
    @Test
    @DisplayName("throw `ISE` if an exception is thrown during invocation")
    void throwOnInvocationError() throws NoSuchMethodException {
        Method method = ClassWithPrivateMethod.class.getDeclaredMethod("throwingMethod");
        ClassWithPrivateMethod target = new ClassWithPrivateMethod();

        assertThrows(IllegalStateException.class,
                     () -> Methods.setAccessibleAndInvoke(method, target));
    }
}
