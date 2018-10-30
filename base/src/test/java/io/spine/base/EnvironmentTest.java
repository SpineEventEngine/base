/*
 * Copyright 2018, TeamDev. All rights reserved.
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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        storedEnvironment = Environment.getInstance()
                                       .createCopy();
    }

    @SuppressWarnings("StaticVariableUsedBeforeInitialization")
    @AfterAll
    static void restoreEnvironment() {
        Environment.getInstance()
                   .restoreFrom(storedEnvironment);
    }

    /* Environment protection END */

    @BeforeEach
    void setUp() {
        environment = Environment.getInstance();
    }

    @AfterEach
    void cleanUp() {
        Environment.getInstance()
                   .reset();
    }

    @Test
    @DisplayName("tell that we are under tests if env variable set to true")
    void tell_that_we_are_under_tests_if_env_var_set_to_true() {
        Environment.getInstance()
                   .setToTests();

        assertTrue(environment.isTests());
        assertFalse(environment.isProduction());
    }

    @Test
    @DisplayName("tell that we are under tests if env variable set to 1")
    void tell_that_we_are_under_tests_if_env_var_set_to_1() {
        System.setProperty(Environment.ENV_KEY_TESTS, "1");

        assertTrue(environment.isTests());
        assertFalse(environment.isProduction());
    }

    @Test
    @DisplayName("tell that we are under tests if run under known framework")
    void tell_that_we_are_under_tests_if_run_under_known_framework() {
        // As we run this from under JUnit...
        assertTrue(environment.isTests());
        assertFalse(environment.isProduction());
    }

    @Test
    @DisplayName("tell that we are not under tests if env set to something else")
    void tell_that_we_are_not_under_tests_if_env_set_to_something_else() {
        System.setProperty(Environment.ENV_KEY_TESTS, "neitherTrueNor1");

        assertFalse(environment.isTests());
        assertTrue(environment.isProduction());
    }

    @Test
    @DisplayName("turn tests mode on")
    void turn_tests_mode_on() {
        environment.setToTests();

        assertTrue(environment.isTests());
        assertFalse(environment.isProduction());
    }

    @Test
    @DisplayName("turn production mode on")
    void turn_production_mode_on() {
        environment.setToProduction();

        assertFalse(environment.isTests());
        assertTrue(environment.isProduction());
    }

    @Test
    @DisplayName("clear environment var on rest")
    void clear_environment_var_on_reset() {
        environment.reset();

        assertNull(System.getProperty(Environment.ENV_KEY_TESTS));
    }
}
