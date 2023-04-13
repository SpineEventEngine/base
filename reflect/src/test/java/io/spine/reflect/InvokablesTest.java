/*
 * Copyright 2023, TeamDev. All rights reserved.
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

package io.spine.reflect;

import com.google.common.testing.NullPointerTester;
import com.google.common.truth.BooleanSubject;
import io.spine.reflect.given.ConstructorsTestEnv;
import io.spine.reflect.given.MethodsTestEnv.ClassWithPrivateMethod;
import io.spine.testing.UtilityClassTest;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.reflect.Invokables.asHandle;
import static io.spine.reflect.Invokables.setAccessibleAndInvoke;
import static io.spine.reflect.given.ConstructorsTestEnv.Animal;
import static io.spine.reflect.given.ConstructorsTestEnv.Cat;
import static io.spine.reflect.given.ConstructorsTestEnv.ClassWithDefaultCtor;
import static io.spine.reflect.given.ConstructorsTestEnv.ClassWithPrivateCtor;
import static io.spine.reflect.given.ConstructorsTestEnv.NoParameterlessConstructors;
import static io.spine.reflect.given.ConstructorsTestEnv.ThrowingConstructor;
import static io.spine.testing.Assertions.assertIllegalArgument;
import static io.spine.testing.Assertions.assertIllegalState;

@DisplayName("`Invokables` should")
class InvokablesTest extends UtilityClassTest<Invokables> {

    InvokablesTest() {
        super(Invokables.class);
    }

    private Method privateMethod;

    @Override
    protected void configure(NullPointerTester tester) {
        super.configure(tester);
        tester.setDefault(Method.class, objectToString());
    }

    @Nested
    @DisplayName("when invoking methods")
    class Methods {
        private ClassWithPrivateMethod target;

        @BeforeEach
        void obtainMethod() throws NoSuchMethodException {
            privateMethod = ClassWithPrivateMethod.class.getDeclaredMethod("privateMethod");
            target = new ClassWithPrivateMethod();
        }

        @AfterEach
        void resetMethod() {
            privateMethod.setAccessible(false);
        }

        @Test
        @DisplayName("set accessible and invoke successfully")
        void allowToSetAccessibleAndInvoke() {
            var result = setAccessibleAndInvoke(privateMethod, target);

            assertThat(result).isEqualTo(ClassWithPrivateMethod.METHOD_RESULT);
        }

        @SuppressWarnings("CheckReturnValue") // Called to throw exception.
        @Test
        @DisplayName("throw `IAE` if the given target is not a valid invocation target")
        void throwOnInvalidTarget() {
            var wrongTarget = new Object();

            assertIllegalState(() -> setAccessibleAndInvoke(privateMethod, wrongTarget));
        }

        @SuppressWarnings("CheckReturnValue") // Called to throw exception.
        @Test
        @DisplayName("throw `ISE` if an exception is thrown during invocation")
        void throwOnInvocationError() throws NoSuchMethodException {
            var method = ClassWithPrivateMethod.class.getDeclaredMethod("throwingMethod");

            assertIllegalState(() -> setAccessibleAndInvoke(method, target));
        }

        @Test
        @DisplayName("convert a visible method to a handle")
        void convertToHandle() throws Throwable {
            var method = ClassWithPrivateMethod.class.getMethod("publicMethod");
            var handle = asHandle(method);
            assertThat(handle).isNotNull();

            var invocationResult = handle.bindTo(new ClassWithPrivateMethod())
                                         .invoke();
            assertThat(invocationResult)
                 .isEqualTo(ClassWithPrivateMethod.METHOD_RESULT);
        }

        @Test
        @DisplayName("convert an invisible method to a handle")
        void convertInvisibleToHandle() throws Throwable {
            var handle = asHandle(privateMethod);
            assertThat(handle).isNotNull();
            assertAccessible(target).isFalse();

            var invocationResult = handle.invoke(new ClassWithPrivateMethod());
            assertThat(invocationResult)
                 .isEqualTo(ClassWithPrivateMethod.METHOD_RESULT);
        }

        @Test
        @DisplayName("convert an accessible method to a handle")
        void convertAccessibleToHandle() throws Throwable {
            privateMethod.setAccessible(true);
            var handle = asHandle(privateMethod);
            assertAccessible(target).isTrue();
            assertThat(handle).isNotNull();

            var invocationResult = handle.invoke(target);
            assertThat(invocationResult)
                 .isEqualTo(ClassWithPrivateMethod.METHOD_RESULT);
        }

        @NonNull
        private BooleanSubject assertAccessible(ClassWithPrivateMethod target) {
            return assertThat(privateMethod.canAccess(target));
        }
    }

    @Nested
    @DisplayName("when instantiating objects")
    class Objects {

        @Test
        @DisplayName("instantiate a class using a parameterless constructor")
        void instantiate() {
            var cat = Invokables.callParameterlessCtor(Cat.class);
            assertThat(cat.greet()).contains(Animal.MISSING);
        }

        @Test
        @DisplayName("fail to instantiate an abstract class")
        void notInstantiateAbstractClass() {
            assertIllegalState(() -> Invokables.callParameterlessCtor(Animal.class));
        }

        @Test
        @DisplayName("instantiate using a default ctor")
        void defaultCtor() {
            var instance = Invokables.callParameterlessCtor(ClassWithDefaultCtor.class);
            assertThat(instance.instantiated()).isTrue();
        }

        @Test
        @DisplayName("throw if there was an exception during class instantiation")
        void throwIfThrows() {
            assertIllegalState(() -> Invokables.callParameterlessCtor(ThrowingConstructor.class));
        }

        @Test
        @DisplayName("fail to instantiate a nested class")
        void notInstantiateNested() {
            assertIllegalArgument(() -> Invokables.callParameterlessCtor(ConstructorsTestEnv.Chicken.class));
        }

        @Test
        @DisplayName("instantiate a private class")
        void instantiatePrivate() {
            var instance = Invokables.callParameterlessCtor(
                    ClassWithPrivateCtor.class);
            assertThat(instance.instantiated()).isTrue();
        }

        @Test
        @DisplayName("fail to instantiate a class without a parameterless ctor")
        void noParameterlessCtor() {
            assertIllegalArgument(() -> Invokables.callParameterlessCtor(NoParameterlessConstructors.class));
        }

        @Nested
        @DisplayName("bring the accessibility back")
        class Accessibility {

            private Constructor<?> ctor;

            @Test
            @DisplayName("if the instantiation succeeded")
            void success() throws NoSuchMethodException {
                var privateCtorClass = ClassWithPrivateCtor.class;

                ctor = ClassWithPrivateCtor.class.getDeclaredConstructor();
                assertConstructorAccessible().isFalse();

                var instance = Invokables.callParameterlessCtor(privateCtorClass);
                assertThat(instance.instantiated()).isTrue();

                assertConstructorAccessible().isFalse();
            }

            @Test
            @DisplayName("if the instantiation failed")
            void failure() throws NoSuchMethodException {
                var throwingConstructorClass = ThrowingConstructor.class;

                ctor = ThrowingConstructor.class.getDeclaredConstructor();

                assertConstructorAccessible().isFalse();

                assertIllegalState(() -> Invokables.callParameterlessCtor(throwingConstructorClass));

                assertConstructorAccessible().isFalse();
            }

            BooleanSubject assertConstructorAccessible() {
                return assertThat(ctor.canAccess(null));
            }
        }
    }

    private static Method objectToString() {
        try {
            return Object.class.getDeclaredMethod("toString");
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }
}
