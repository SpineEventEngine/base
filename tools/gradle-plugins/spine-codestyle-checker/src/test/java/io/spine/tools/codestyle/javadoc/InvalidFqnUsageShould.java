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

package io.spine.tools.codestyle.javadoc;

import io.spine.tools.codestyle.CodeStyleViolation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Alexander Aleksandrov
 */
public class InvalidFqnUsageShould {

    private static final String expectedLink = "invalidFqnLink";

    private CodeStyleViolation codeStyleViolation;

    public CodeStyleViolation setUpInvalidFqnUsage(String actualUsage, int index) {
        final CodeStyleViolation codeStyleViolation =
                new CodeStyleViolation(actualUsage).withLineNumber(index);
        return codeStyleViolation;
    }

    @Test
    public void get_actual_usage() {
        codeStyleViolation = setUpInvalidFqnUsage(expectedLink, 1);
        assertEquals(expectedLink, codeStyleViolation.getCodeLine());
    }

    @Test
    public void get_index() {
        codeStyleViolation = setUpInvalidFqnUsage(expectedLink, 1);
        assertEquals(1, codeStyleViolation.getLineNumber());
    }

    @Test
    public void set_index() {
        codeStyleViolation = setUpInvalidFqnUsage(expectedLink, 2).withLineNumber(3);
        assertEquals(3, codeStyleViolation.getLineNumber());
    }

    @Test
    public void override_toString() {
        codeStyleViolation = setUpInvalidFqnUsage(expectedLink, 1);
        assertTrue(codeStyleViolation.toString()
                                     .contains(expectedLink));
    }
}
