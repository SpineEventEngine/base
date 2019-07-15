/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.testing.logging;

import io.spine.testing.TestValues;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("AssertingHandler should")
class AssertingHandlerTest {

    private static final Logger logger = Logger.getLogger(AssertingHandlerTest.class.getName());

    private AssertingHandler handler;

    @BeforeEach
    void setupHandler() {
        handler = new AssertingHandler();
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);
        logger.setLevel(Level.INFO);
    }

    @AfterEach
    void clearHandler() {
        logger.removeHandler(handler);
    }

    @Test
    @DisplayName("assert no logs if nothing logged")
    void noLogs() {
        assertDoesNotThrow(handler::isEmpty);
    }

    @Test
    @DisplayName("throw `AssertionError` if there were logs")
    void throwIfLogged() {
        logger.info("Testing assertion");
        assertThrows(AssertionError.class, () -> handler.isEmpty());
    }

    @Test
    @DisplayName("obtain `StringSubject` for the first record")
    void textAssertion() {
        String msg = TestValues.randomString();
        logger.info(msg);
        handler.textOutput()
               .contains(msg);
    }
}
