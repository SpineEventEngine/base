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

import io.spine.base.given.AwsLambda;
import io.spine.base.given.VariableControlledEnvironment;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.base.EnvironmentTypes.instantiate;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("`EnvironmentTypes` should")
class EnvironmentTypesTest extends UtilityClassTest<EnvironmentTypes> {

    EnvironmentTypesTest() {
        super(EnvironmentTypes.class);
    }

    @Nested
    @DisplayName("not instantiate an env type")
    class NotInstantiate {

        @Test
        @DisplayName("if it doesn't have a parameterless constructor")
        void noMatchingCtor() {
            assertThrows(IllegalArgumentException.class,
                         () -> EnvironmentTypes.instantiate(ValueDependantEnvironment.class));
        }

        @Test
        @DisplayName("if it is a nested class")
        void nestedClass() {
            assertThrows(IllegalArgumentException.class,
                         () -> EnvironmentTypes.instantiate(NestedEnvironment.class));
        }

        @Test
        @DisplayName("if it is an abstract class")
        void abstractClass() {
            assertThrows(IllegalArgumentException.class,
                         () -> EnvironmentTypes.instantiate(VariableControlledEnvironment.class));
        }

        private final class NestedEnvironment extends EnvironmentType {

            @Override
            protected boolean enabled() {
                return false;
            }
        }
    }

    @Nested
    @DisplayName("instantiate an env type")
    class Instantiate {

        @Test
        @DisplayName("instantiate an env type with a parameterless ctor")
        void allowToInstantiate() {
            Class<Local> local = Local.class;
            EnvironmentType localEnv = instantiate(local);
            assertThat(localEnv.enabled()).isTrue();
        }

        @Test
        @DisplayName("instantiate an env type that has a parameterless ctor among declared ctors")
        void allowToInstantiateMoreThan1Ctor() {
            Class<ConfigurableEnvironment> env = ConfigurableEnvironment.class;
            EnvironmentType envType = instantiate(env);
            assertThat(envType.enabled()).isFalse();
        }

        @Test
        @DisplayName("instantiate an env type that extends a class that has no parameterless ctor")
        void allowToInstantiateExtends() {
            Class<? extends VariableControlledEnvironment> awsLambda = AwsLambda.class;
            EnvironmentType environment = instantiate(awsLambda);
            assertThat(environment).isInstanceOf(AwsLambda.class);
        }
    }

    private static final class Local extends EnvironmentType {

        Local() {
        }

        @Override
        protected boolean enabled() {
            return true;
        }
    }

    @SuppressWarnings("unused" /* need ctors to tests reflection-using functionality. */)
    private static final class ConfigurableEnvironment extends EnvironmentType {

        private final boolean enabled;

        ConfigurableEnvironment(boolean enabled) {
            this.enabled = enabled;
        }

        ConfigurableEnvironment() {
            this.enabled = false;
        }

        @Override
        protected boolean enabled() {
            return enabled;
        }
    }

    private static final class ValueDependantEnvironment extends EnvironmentType {

        private final boolean enabled;

        ValueDependantEnvironment(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        protected boolean enabled() {
            return enabled;
        }
    }
}
