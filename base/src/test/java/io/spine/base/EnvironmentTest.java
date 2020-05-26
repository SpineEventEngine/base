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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.base.BaseEnvironmentType.ENV_KEY_TESTS;
import static io.spine.base.BaseEnvironmentType.PRODUCTION;
import static io.spine.base.BaseEnvironmentType.TESTS;
import static io.spine.base.EnvironmentTest.CustomEnvType.LOCAL;
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
    }

    @Test
    @DisplayName("tell that we are under tests if env. variable set to true")
    void environmentVarTrue() {
        Environment.instance()
                   .setTo(TESTS);

        assertThat(environment.envType()).isSameInstanceAs(TESTS);
    }

    @Test
    @DisplayName("tell that we are under tests if env. variable set to 1")
    void environmentVar1() {
        System.setProperty(ENV_KEY_TESTS, "1");

        assertThat(environment.envType()).isSameInstanceAs(TESTS);
    }

    @Test
    @DisplayName("tell that we are under tests if run under known framework")
    void underTestFramework() {
        // As we run this from under JUnit...
        assertThat(environment.envType()).isSameInstanceAs(TESTS);
    }

    @Test
    @DisplayName("tell that we are not under tests if env set to something else")
    void environmentVarUnknownValue() {
        System.setProperty(ENV_KEY_TESTS, "neitherTrueNor1");

        assertThat(environment.envType()).isSameInstanceAs(PRODUCTION);
    }

    @Test
    @DisplayName("turn tests mode on")
    void turnTestsOn() {
        environment.setTo(TESTS);

        assertThat(environment.envType()).isSameInstanceAs(TESTS);
    }

    @Test
    @DisplayName("turn production mode on")
    void turnProductionOn() {
        environment.setTo(PRODUCTION);

        assertThat(environment.envType()).isSameInstanceAs(PRODUCTION);
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
        void mutateKnownEnvTypesOnce() {
            Environment.registerCustom(CustomEnvType.class);

            // Now that `Environment` knows about `LOCAL`, it should use it as fallback.
            assertThat(Environment.instance()
                                  .envType()).isSameInstanceAs(LOCAL);
        }

        @Test
        @DisplayName("throw if a user attempts to create register the same environment twice")
        void throwOnDoubleCreation() {
            Environment.registerCustom(CustomEnvType.class);
            assertThrows(IllegalStateException.class,
                         () -> Environment.registerCustom(CustomEnvType.class));
        }

        @Test
        @DisplayName("fallback to the `TESTS` environment")
        void fallBack() {
            Environment.registerCustom(BuildServerEnvironment.class);
            assertThat(Environment.instance()
                                  .envType())
                    .isSameInstanceAs(TESTS);
        }
    }

    enum CustomEnvType implements EnvironmentType {

        LOCAL {
            @Override
            public boolean currentlyOn() {
                // `LOCAL` is the default custom env type. It should be used as a fallback.
                return true;
            }

            @Override
            public void reset() {
                // NOP.
            }

            @Override
            public void setTo() {
                // NOP.
            }
        },
        STAGING {
            @Override
            public boolean currentlyOn() {
                return System.getProperty(STAGING_ENV_TYPE_KEY)
                             .equalsIgnoreCase(String.valueOf(true));

            }

            @Override
            public void reset() {
                System.clearProperty(STAGING_ENV_TYPE_KEY);
            }

            @Override
            public void setTo() {
                System.setProperty(STAGING_ENV_TYPE_KEY, String.valueOf(true));
            }
        };

        static final String STAGING_ENV_TYPE_KEY = "io.spine.base.EnvironmentTest.is_staging";
    }

    enum BuildServerEnvironment implements EnvironmentType {

        TRAVIS {
            @Override
            public boolean currentlyOn() {
                return false;
            }

            @Override
            public void reset() {
                // NOP.
            }

            @Override
            public void setTo() {
                // NOP.
            }
        }
    }
}
