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

package io.spine.testing.given;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Test environment for {@link io.spine.testing.SingletonTestTest}.
 */
public class SingletonTestEnv {

    private SingletonTestEnv() {
    }

    public static class SingletonClass {

        /** This field makes this class non-utility. */
        @SuppressWarnings({"FieldMayBeStatic", "unused"})
        private final boolean haveSomeState = true;

        private static final SingletonClass INSTANCE = new SingletonClass();

        /** Prevents direct instantiation. */
        private SingletonClass() {
        }

        public static SingletonClass instance() {
            return INSTANCE;
        }

        @SuppressWarnings("unused")
        public static void staticMethod(String param) {
            checkNotNull(param);
        }
    }

    public static class EveryTimeNew {

        /** This field makes this class non-utility. */
        @SuppressWarnings({"FieldMayBeStatic", "unused"})
        private final boolean haveState = true;

        private EveryTimeNew() {
        }

        public static EveryTimeNew instance() {
            return new EveryTimeNew();
        }
    }

    @SuppressWarnings("EmptyClass")
    public static class NoConstructor {
    }

    public static class PackagePrivateConstructor {

        private static final PackagePrivateConstructor INSTANCE = new PackagePrivateConstructor();

        /** This field makes this class non-utility. */
        @SuppressWarnings({"FieldCanBeLocal", "unused"})
        private final boolean state;

        PackagePrivateConstructor(boolean state) {
            this.state = state;
        }

        private PackagePrivateConstructor() {
            this(true);
        }

        public static PackagePrivateConstructor instance() {
            return INSTANCE;
        }
    }

    public static class ProtectedConstructor {

        private static final ProtectedConstructor INSTANCE = new ProtectedConstructor();

        /** This field makes this class non-utility. */
        @SuppressWarnings({"FieldCanBeLocal", "unused"})
        private final boolean state;

        protected ProtectedConstructor(boolean state) {
            this.state = state;
        }

        private ProtectedConstructor() {
            this(true);
        }

        public static ProtectedConstructor instance() {
            return INSTANCE;
        }
    }
}
