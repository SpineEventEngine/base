/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

import org.junit.Test;
import io.spine.tools.codestyle.CodeStyleViolation;

import static org.junit.Assert.assertEquals;

/**
 * @author Alexander Aleksandrov
 */
public class InvalidFqnUsageShould {

    public static final String expectedLink = "invalidFqnLink";
    private CodeStyleViolation codeStyleViolation;

    public CodeStyleViolation setUpInvalidFqnUsage(String actualUsage, int index){
        final CodeStyleViolation codeStyleViolation = new CodeStyleViolation(actualUsage);
        codeStyleViolation.setIndex(index);
        return codeStyleViolation;
    }

    @Test
    public void get_actual_usage() throws Exception {
        codeStyleViolation = setUpInvalidFqnUsage(expectedLink, 1);
        assertEquals(expectedLink, codeStyleViolation.getActualUsage());
    }

    @Test
    public void get_index() throws Exception {
        codeStyleViolation = setUpInvalidFqnUsage(expectedLink, 1);
        assertEquals(1, codeStyleViolation.getIndex());
    }

    @Test
    public void set_index() throws Exception {
        codeStyleViolation = setUpInvalidFqnUsage(expectedLink, 2);
        codeStyleViolation.setIndex(3);
        assertEquals(3, codeStyleViolation.getIndex());
    }

    @Test
    public void override_toString() throws Exception {
        codeStyleViolation = setUpInvalidFqnUsage(expectedLink, 1);
        final String result = "CodeStyleViolation{actualUsage=invalidFqnLink}";
        assertEquals(result, codeStyleViolation.toString());
    }

}
