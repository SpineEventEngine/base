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

import com.google.errorprone.annotations.Immutable;
import io.spine.base.given.AppEngine;
import io.spine.base.given.AppEngineStandard;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.base.Tests.ENV_KEY_TESTS;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Environment utility class should")
@SuppressWarnings("AccessOfSystemProperties")
class EnvironmentTest extends UtilityClassTest<Environment> {

    /*
     * Environment protection START
     *
     * We remember the state and restore it after this test suite is complete because other tests
     * may initialize the environment.
     */
    @SuppressWarnings("StaticVariableMayNotBeInitialized")
    private static Environment storedEnvironment;

    private Environment environment;

    EnvironmentTest() {
        super(Environment.class);
    }

    @BeforeAll
    static void storeEnvironment() {
        storedEnvironment = Environment.instance()
                                       .createCopy();
    }

    @SuppressWarnings("StaticVariableUsedBeforeInitialization")
    @AfterAll
    static void restoreEnvironment() {
        Environment.instance()
                   .restoreFrom(storedEnvironment);
    }

    /* Environment protection END */

    @BeforeEach
    void setUp() {
        environment = Environment.instance();
    }

    @AfterEach
    void cleanUp() {
        Environment.instance()
                   .reset();
        System.clearProperty(ENV_KEY_TESTS);
    }

    @Test
    @DisplayName("tell that we are under tests if env. variable set to true")
    void environmentVarTrue() {
        Tests tests = new Tests();
        Environment.instance()
                   .setTo(tests);

        assertThat(environment.is(Tests.class)).isTrue();
    }

    @Test
    @DisplayName("tell that we are under tests if env. variable set to 1")
    void environmentVar1() {
        System.setProperty(ENV_KEY_TESTS, "1");

        assertThat(environment.is(Tests.class)).isTrue();
    }

    @Test
    @DisplayName("tell that we are under tests if run under known framework")
    void underTestFramework() {
        // As we run this from under JUnit...
        assertThat(environment.is(Tests.class)).isTrue();
    }

    @Test
    @DisplayName("tell that we are not under tests if env set to something else")
    void environmentVarUnknownValue() {
        System.setProperty(ENV_KEY_TESTS, "neitherTrueNor1");

        assertThat(environment.is(Production.class)).isTrue();
    }

    @Test
    @DisplayName("tell that we are under tests when a deprecated method is used")
    void underTestFrameworkDeprecated() {
        @SuppressWarnings("deprecation")
        boolean isTests = environment.isTests();
        assertThat(isTests).isTrue();
    }

    @Test
    @DisplayName("tell that we are under tests if explicitly set to tests using a deprecated method")
    @SuppressWarnings("deprecation")
    void explicitlySetTrue() {
        environment.setToTests();

        assertThat(environment.is(Tests.class)).isTrue();
    }

    @Test
    @DisplayName("tell that we are not under tests when a deprecated method is used")
    void inProductionUsingDeprecatedMethod() {
        System.setProperty(ENV_KEY_TESTS, "neitherTrueNor1");

        @SuppressWarnings("deprecation")
        boolean isProduction = environment.isProduction();
        assertThat(isProduction).isTrue();
    }

    @Test
    @DisplayName("turn tests mode on")
    void turnTestsOn() {
        environment.setTo(new Tests());

        assertThat(environment.is(Tests.class)).isTrue();
    }

    @Test
    @DisplayName("turn production mode on")
    void turnProductionOn() {
        environment.setTo(new Production());

        assertThat(environment.is(Production.class)).isTrue();
    }

    @Test
    @DisplayName("turn production mode on using a deprecated method")
    @SuppressWarnings("deprecation")
    void turnProductionOnUsingDeprecatedMethod() {
        environment.setToProduction();

        assertThat(environment.is(Production.class)).isTrue();
    }

    @Test
    @DisplayName("clear environment var on rest")
    void clearOnReset() {
        environment.reset();

        assertNull(System.getProperty(ENV_KEY_TESTS));
    }

    @Nested
    @DisplayName("when assigning custom environment types")
    class CustomEnvTypes {

        @Test
        @DisplayName("allow to provide user defined environment types")
        void provideCustomTypes() {
            environment.register(new Staging())
                       .register(new Local());

            // Now that `Environment` knows about `LOCAL`, it should use it as fallback.
            assertThat(environment.is(Local.class)).isTrue();
        }

        @Test
        @DisplayName("fallback to the `TESTS` environment")
        void fallBack() {
            Environment.instance()
                       .register(new Travis());
            assertThat(environment.is(Travis.class)).isFalse();
            assertThat(environment.is(Tests.class)).isTrue();
        }
    }

    @Test
    @DisplayName("follow assignment-compatibility when determining the type")
    void polymorphicEnv() {
        environment.register(new AppEngineStandard());

        AppEngineStandard.enable();
        assertThat(environment.is(AppEngine.class)).isTrue();
        AppEngineStandard.clear();
    }

    @Test
    @DisplayName("detect the current environment correctly using the `type` method")
    void determineUsingType() {
        assertThat(environment.is(Tests.class)).isTrue();
    }

    @Test
    @DisplayName("detect the current custom environment in presence of custom types")
    void determineUsingTypeInPresenceOfCustom() {
        environment.register(new Staging())
                   .register(new Local());

        assertThat(environment.is(Local.class)).isTrue();
    }

    @Test
    @DisplayName("return the instance of the default environment type")
    void returnInstance() {
        assertThat(environment.type()).isInstanceOf(Tests.class);
    }

    @Test
    @DisplayName("return the instance of a custom environment type")
    void returnCustomInstance() {
        environment.register(Local.class)
                   .register(Staging.class);

        assertThat(environment.type()).isInstanceOf(Local.class);
    }

    @Nested
    @DisplayName("disallow to register types by class if")
    class ProhibitRegistering {

        @Test
        @DisplayName("they have a public ctor")
        void registerCustomPublicCtor() {
            assertThrows(IllegalArgumentException.class, () -> environment.register(Travis.class));
        }

        @Test
        @DisplayName("they have a private ctor")
        void registerCustomPrivateCtor() {
            assertThrows(IllegalArgumentException.class,
                         () -> environment.register(HiddenEnvironment.class));
        }

        @Test
        @DisplayName("they have a protected ctor")
        void registerCustomPackagePrivateCtor() {
            assertThrows(IllegalArgumentException.class,
                         () -> environment.register(ProtectedEnvironment.class));
        }

        @Test
        @DisplayName("they do not have a parameterless ctor")
        void registerCustomCtorWithParameters() {
            assertThrows(IllegalArgumentException.class,
                         () -> environment.register(ValueDependantEnvironment.class));
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
    }

    static final class Local extends EnvironmentType {

        /**
         * A package-private parameterless ctor allows to register this type by class.
         */
        Local() {
        }

        @Override
        public boolean enabled() {
            // `LOCAL` is the default custom env type. It should be used as a fallback.
            return true;
        }
    }

    static final class Staging extends EnvironmentType {

        static final String STAGING_ENV_TYPE_KEY = "io.spine.base.EnvironmentTest.is_staging";

        /**
         * A package-private parameterless ctor allows to register this type by class.
         */
        Staging() {
        }

        @Override
        public boolean enabled() {
            return String.valueOf(true)
                         .equalsIgnoreCase(System.getProperty(STAGING_ENV_TYPE_KEY));
        }
    }

    @Immutable
    @SuppressWarnings("unused" /* The only variant is used. */)
    static final class Travis extends EnvironmentType {

        @SuppressWarnings("WeakerAccess" /* Environment types with public ctors shouldn't be registrable by their classes, this one tests for it. */)
        public Travis() {
        }

        @Override
        public boolean enabled() {
            return false;
        }
    }
}
