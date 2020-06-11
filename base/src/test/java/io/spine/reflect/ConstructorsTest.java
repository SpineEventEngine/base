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
import io.spine.reflect.given.ConstructorsTestEnv.ClassWithDefaultCtor;
import io.spine.reflect.given.ConstructorsTestEnv.Dog;
import io.spine.reflect.given.ConstructorsTestEnv.NoParameterlessConstructors;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.reflect.Constructors.ensureParameterlessCtor;
import static io.spine.reflect.given.ConstructorsTestEnv.Animal.MISSING;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("`Constructors` should")
class ConstructorsTest extends UtilityClassTest<Constructors> {

    ConstructorsTest() {
        super(Constructors.class);
    }

    @Nested
    @DisplayName("when looking for parameterless constructors")
    class ParamaterlessCtors {

        @Test
        @DisplayName("find one in a concrete class")
        void findParameterless() throws IllegalAccessException,
                                        InvocationTargetException,
                                        InstantiationException {
            Constructor<Cat> constructor = ensureParameterlessCtor(Cat.class);
            Cat cat = constructor.newInstance();
            assertThat(cat.greet()).contains(MISSING);
        }

        @Test
        @DisplayName("find one in an abstract class")
        void findParameterlessInAbstract() {
            Constructor<Animal> constructor = ensureParameterlessCtor(Animal.class);
            assertThrows(InstantiationException.class, constructor::newInstance);
        }

        @Test
        @DisplayName("find one if it's declared in a parent")
        void findParameterlessConstructorsInTheParent() throws IllegalAccessException,
                                                               InvocationTargetException,
                                                               InstantiationException {
            Constructor<Dog> constructor = ensureParameterlessCtor(Dog.class);
            Dog dog = constructor.newInstance();
            assertThat(dog.greet()).contains(MISSING);
        }

        @Test
        @DisplayName("not find one in a nested class")
        void notFindInNested() {
            assertThrows(IllegalArgumentException.class,
                         () -> ensureParameterlessCtor(Chicken.class));
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
}
