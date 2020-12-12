/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.reflect.given;

import static java.lang.String.format;

/**
 * This is an environment for {@linkplain io.spine.reflect.InvokablesTest testing
 * constructor-related} utilities.
 */
@SuppressWarnings("unused" /* need unused members for reflection lookup. */)
public final class ConstructorsTestEnv {

    /** Prevents instantiation of this test env class. */
    private ConstructorsTestEnv() {
    }

    public static class NoParameterlessConstructors {

        @SuppressWarnings("FieldCanBeLocal")
        private final int id;

        public NoParameterlessConstructors(int id) {
            this.id = id;
        }
    }

    public static class ClassWithDefaultCtor {

        public boolean instantiated() {
            return true;
        }
    }

    public abstract static class Animal {

        public static final String MISSING = "missing";
        private final String name;

        Animal(String name) {
            this.name = name;
        }

        Animal() {
            this.name = MISSING;
        }

        public abstract String makeSound();

        public final String greet() {
            return makeSound() + format(". My name is %s.", name);
        }
    }

    public static final class Cat extends Animal {

        public Cat(String name) {
            super(name);
        }

        public Cat() {
            super();
        }

        @Override
        public String makeSound() {
            return "Meow";
        }
    }

    public final class Chicken extends Animal {

        @Override
        public String makeSound() {
            return "Cluck";
        }
    }

    public static class ClassWithPrivateCtor {

        private ClassWithPrivateCtor() {
        }

        public boolean instantiated() {
            return true;
        }
    }

    public static class ThrowingConstructor {

        public ThrowingConstructor() {
            throw new IllegalStateException("");
        }

        public boolean instantiated() {
            return true;
        }
    }
}
