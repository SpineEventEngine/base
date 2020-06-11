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

import io.spine.reflect.given.ConstructorsTestEnv.Animal;
import io.spine.reflect.given.ConstructorsTestEnv.Cat;
import io.spine.reflect.given.ConstructorsTestEnv.Chicken;
import io.spine.reflect.given.ConstructorsTestEnv.ClassWithPrivateCtor;
import io.spine.reflect.given.ConstructorsTestEnv.NoParameterlessConstructors;
import io.spine.reflect.given.ConstructorsTestEnv.ThrowingConstructor;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.reflect.Constructors.ensureParameterlessCtor;
import static io.spine.reflect.Objects.instantiateWithoutParameters;
import static io.spine.reflect.given.ConstructorsTestEnv.Animal.MISSING;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("`Objects` should")
class ObjectsTest extends UtilityClassTest<Objects> {

    ObjectsTest() {
        super(Objects.class);
    }

    @Test
    @DisplayName("instantiate a class using a parameterless constructor")
    void instantiate() {
        Cat cat = instantiateWithoutParameters(Cat.class);
        assertThat(cat.greet()).contains(MISSING);
    }

    @Test
    @DisplayName("fail to instantiate an abstract class")
    void notInstantiateAbstractClass() {
        assertThrows(IllegalStateException.class, () -> instantiateWithoutParameters(Animal.class));
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
                     () -> instantiateWithoutParameters(Chicken.class));
    }

    @Test
    @DisplayName("instantiate a private class")
    void instantiatePrivate() {
        ClassWithPrivateCtor instance = instantiateWithoutParameters(ClassWithPrivateCtor.class);
        assertThat(instance.instantiated()).isTrue();
    }

    @Test
    @DisplayName("fail to instantiate a class without a parameterless ctor")
    void noParameterlessCtor() {
        assertThrows(IllegalArgumentException.class,
                     () -> instantiateWithoutParameters(NoParameterlessConstructors.class));
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
