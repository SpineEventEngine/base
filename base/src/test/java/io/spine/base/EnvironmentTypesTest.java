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

package io.spine.base;

import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.base.EnvironmentTypes.checkCanRegisterByClass;
import static io.spine.base.EnvironmentTypes.instantiate;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("`EnvironmentTypes` should")
class EnvironmentTypesTest extends UtilityClassTest<EnvironmentTypes> {

    EnvironmentTypesTest() {
        super(EnvironmentTypes.class);
    }

    @Test
    @DisplayName("allow to register an env type with a package-private parameterless ctor")
    void allowToRegister() {
        Class<Local> local = Local.class;
        assertThat(checkCanRegisterByClass(Local.class)).isSameInstanceAs(local);
    }

    @Test
    @DisplayName("allow to instantiate an env type with a package-private parameterless ctor")
    void allowToInstantiate() {
        Class<Local> local = Local.class;
        EnvironmentType localEnv = instantiate(local);
        assertThat(localEnv.enabled()).isTrue();
    }

    @DisplayName("Disallow to register env types by classes if they do not have a" +
            "package-private parameterless constructor")
    @ParameterizedTest
    @MethodSource("envTypesAndMethods")
    void checkCanRegister(Class<? extends EnvironmentType> environmentType) {
        assertThrows(IllegalArgumentException.class,
                     () -> EnvironmentTypes.checkCanRegisterByClass(environmentType));
    }

    @DisplayName("Disallow to instantaite env types by classes if they do not have a" +
            "package-private parameterless constructor")
    @ParameterizedTest
    @MethodSource("envTypesAndMethods")
    void checkCanInstantiate(Class<? extends EnvironmentType> environmentType) {
        assertThrows(IllegalArgumentException.class,
                     () -> EnvironmentTypes.instantiate(environmentType));
    }

    private static Stream<Arguments> envTypesAndMethods() {
        Stream<Class<? extends EnvironmentType>> envTypes = Stream.of(
                ValueDependantEnvironment.class,
                HiddenEnvironment.class,
                ProtectedEnvironment.class,
                VisibleEnvironment.class
        );
        return envTypes.map(Arguments::arguments);
    }

    private class ValueDependantEnvironment extends EnvironmentType {

        private final boolean enabled;

        ValueDependantEnvironment(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        protected boolean enabled() {
            return enabled;
        }
    }

    private class HiddenEnvironment extends EnvironmentType {

        private HiddenEnvironment() {
        }

        @Override
        protected boolean enabled() {
            return false;
        }
    }

    private class ProtectedEnvironment extends EnvironmentType {

        protected ProtectedEnvironment() {
        }

        @Override
        protected boolean enabled() {
            return false;
        }
    }

    private class VisibleEnvironment extends EnvironmentType {

        @SuppressWarnings("PublicConstructorInNonPublicClass")
        public VisibleEnvironment() {
        }

        @Override
        protected boolean enabled() {
            return true;
        }
    }

    private static class Local extends EnvironmentType {

        Local() {
        }

        @Override
        protected boolean enabled() {
            return true;
        }
    }
}
