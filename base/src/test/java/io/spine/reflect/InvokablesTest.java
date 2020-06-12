/*
 * Copyright 2020, TeamDev. All rights reserved.
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
import io.spine.reflect.given.ConstructorsTestEnv;
import io.spine.reflect.given.MethodsTestEnv.ClassWithPrivateMethod;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.reflect.Invokables.asHandle;
import static io.spine.reflect.Invokables.ensureParameterlessCtor;
import static io.spine.reflect.Invokables.instantiateWithoutParameters;
import static io.spine.reflect.Invokables.setAccessibleAndInvoke;
import static io.spine.reflect.given.ConstructorsTestEnv.Animal;
import static io.spine.reflect.given.ConstructorsTestEnv.Animal.MISSING;
import static io.spine.reflect.given.ConstructorsTestEnv.Cat;
import static io.spine.reflect.given.ConstructorsTestEnv.ClassWithDefaultCtor;
import static io.spine.reflect.given.ConstructorsTestEnv.ClassWithPrivateCtor;
import static io.spine.reflect.given.ConstructorsTestEnv.NoParameterlessConstructors;
import static io.spine.reflect.given.ConstructorsTestEnv.ThrowingConstructor;
import static io.spine.reflect.given.MethodsTestEnv.ClassWithPrivateMethod.METHOD_RESULT;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

        @BeforeEach
        void obtainMethod() throws NoSuchMethodException {
            privateMethod = ClassWithPrivateMethod.class.getDeclaredMethod("privateMethod");
        }

        @AfterEach
        void resetMethod() {
            privateMethod.setAccessible(false);
        }

        @Test
        @DisplayName("set accessible and invoke successfully")
        void allowToSetAccessibleAndInvoke() {
            ClassWithPrivateMethod target = new ClassWithPrivateMethod();
            Object result = setAccessibleAndInvoke(privateMethod, target);

            assertThat(result).isEqualTo(METHOD_RESULT);
        }

        @SuppressWarnings("CheckReturnValue") // Called to throw exception.
        @Test
        @DisplayName("throw `IAE` if the given target is not a valid invocation target")
        void throwOnInvalidTarget() {
            Object wrongTarget = new Object();

            assertThrows(IllegalStateException.class,
                         () -> setAccessibleAndInvoke(privateMethod, wrongTarget));
        }

        @SuppressWarnings("CheckReturnValue") // Called to throw exception.
        @Test
        @DisplayName("throw `ISE` if an exception is thrown during invocation")
        void throwOnInvocationError() throws NoSuchMethodException {
            Method method = ClassWithPrivateMethod.class.getDeclaredMethod("throwingMethod");
            ClassWithPrivateMethod target = new ClassWithPrivateMethod();

            assertThrows(IllegalStateException.class,
                         () -> setAccessibleAndInvoke(method, target));
        }

        @Test
        @DisplayName("convert a visible method to a handle")
        void convertToHandle() throws Throwable {
            Method method = ClassWithPrivateMethod.class.getMethod("publicMethod");
            MethodHandle handle = asHandle(method);
            assertThat(handle).isNotNull();

            Object invocationResult = handle.bindTo(new ClassWithPrivateMethod())
                                            .invoke();
            assertThat(invocationResult)
                    .isEqualTo(METHOD_RESULT);
        }

        @Test
        @DisplayName("convert an invisible method to a handle")
        void convertInvisibleToHandle() throws Throwable {
            MethodHandle handle = asHandle(privateMethod);
            assertThat(handle).isNotNull();
            assertThat(privateMethod.isAccessible()).isFalse();

            Object invocationResult = handle.invoke(new ClassWithPrivateMethod());
            assertThat(invocationResult)
                    .isEqualTo(METHOD_RESULT);
        }

        @Test
        @DisplayName("convert an accessible method to a handle")
        void convertAccessibleToHandle() throws Throwable {
            privateMethod.setAccessible(true);
            MethodHandle handle = asHandle(privateMethod);
            assertThat(privateMethod.isAccessible()).isTrue();
            assertThat(handle).isNotNull();

            Object invocationResult = handle.invoke(new ClassWithPrivateMethod());
            assertThat(invocationResult)
                    .isEqualTo(METHOD_RESULT);
        }
    }

    @Nested
    @DisplayName("when looking for parameterless constructors")
    class ParamaterlessCtors {

        @Test
        @DisplayName("find one in a concrete class")
        void findParameterless() throws IllegalAccessException,
                                        InvocationTargetException,
                                        InstantiationException {
            Constructor<Cat> constructor = ensureParameterlessCtor(
                    Cat.class);
            Cat cat = constructor.newInstance();
            assertThat(cat.greet()).contains(MISSING);
        }

        @Test
        @DisplayName("find one in an abstract class")
        void findParameterlessInAbstract() {
            Constructor<Animal> constructor = ensureParameterlessCtor(
                    Animal.class);
            assertThrows(InstantiationException.class, constructor::newInstance);
        }

        @Test
        @DisplayName("not find one in a nested class")
        void notFindInNested() {
            assertThrows(IllegalArgumentException.class,
                         () -> ensureParameterlessCtor(ConstructorsTestEnv.Chicken.class));
        }

        @Test
        @DisplayName("find a default one")
        void defaultCtor() throws IllegalAccessException,
                                  InvocationTargetException,
                                  InstantiationException {
            Constructor<ClassWithDefaultCtor> ctor =
                    ensureParameterlessCtor(ClassWithDefaultCtor.class);
            ClassWithDefaultCtor instance = ctor.newInstance();
            assertThat(instance.instantiated()).isTrue();
        }

        @Test
        @DisplayName("not find one if it's not declared in a concrete class")
        void noParameterlessCtorInConcrete() {
            assertThrows(IllegalArgumentException.class, () ->
                    ensureParameterlessCtor(NoParameterlessConstructors.class));
        }
    }

    @Nested
    @DisplayName("when instantiating objects")
    class Objects {

        @Test
        @DisplayName("instantiate a class using a parameterless constructor")
        void instantiate() {
            Cat cat = instantiateWithoutParameters(Cat.class);
            assertThat(cat.greet()).contains(MISSING);
        }

        @Test
        @DisplayName("fail to instantiate an abstract class")
        void notInstantiateAbstractClass() {
            assertThrows(IllegalStateException.class, () -> instantiateWithoutParameters(
                    Animal.class));
        }

        @Test
        @DisplayName("throw if there was an exception during class instantiation")
        void throwIfThrows() {
            assertThrows(IllegalStateException.class,
                         () -> instantiateWithoutParameters(ThrowingConstructor.class));
        }

        @Test
        @DisplayName("fail to instantiate a nested class")
        void notInstantiateNested() {
            assertThrows(IllegalArgumentException.class,
                         () -> instantiateWithoutParameters(ConstructorsTestEnv.Chicken.class));
        }

        @Test
        @DisplayName("instantiate a private class")
        void instantiatePrivate() {
            ClassWithPrivateCtor instance = instantiateWithoutParameters(
                    ClassWithPrivateCtor.class);
            assertThat(instance.instantiated()).isTrue();
        }

        @Test
        @DisplayName("fail to instantiate a class without a parameterless ctor")
        void noParameterlessCtor() {
            assertThrows(IllegalArgumentException.class,
                         () -> instantiateWithoutParameters(
                                 NoParameterlessConstructors.class));
        }

        @Nested
        @DisplayName("bring the accessibility back")
        class Accessibility {

            @Test
            @DisplayName("if the instantiation succeeded")
            void success() {
                Class<ClassWithPrivateCtor> privateCtorClass = ClassWithPrivateCtor.class;
                Constructor<ClassWithPrivateCtor> ctor =
                        ensureParameterlessCtor(privateCtorClass);
                assertThat(ctor.isAccessible()).isFalse();

                ClassWithPrivateCtor instance =
                        instantiateWithoutParameters(privateCtorClass);
                assertThat(instance.instantiated()).isTrue();

                assertThat(ctor.isAccessible()).isFalse();
            }

            @Test
            @DisplayName("if the instantiation failed")
            void failure() {
                Class<ThrowingConstructor> throwingCtorClass = ThrowingConstructor.class;
                Constructor<ThrowingConstructor> ctor =
                        ensureParameterlessCtor(throwingCtorClass);
                assertThat(ctor.isAccessible()).isFalse();

                assertThrows(IllegalStateException.class,
                             () -> instantiateWithoutParameters(throwingCtorClass));

                assertThat(ctor.isAccessible()).isFalse();
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
