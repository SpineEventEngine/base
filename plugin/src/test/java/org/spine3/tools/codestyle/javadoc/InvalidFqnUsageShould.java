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

package org.spine3.tools.codestyle.javadoc;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InvalidFqnUsageShould {
    public static final String expectedLink = "invalidFqnLink";

    private InvalidFqnUsage invalidFqnUsage;
    public InvalidFqnUsage setUpInvalidFqnUsage(String actualUsage, int index){
        final InvalidFqnUsage invalidFqnUsage = new InvalidFqnUsage(actualUsage);
        invalidFqnUsage.setIndex(index);
        return invalidFqnUsage;
    }

    @Test
    public void getActualUsage() throws Exception {
        invalidFqnUsage = setUpInvalidFqnUsage(expectedLink, 1);
        assertEquals(expectedLink, invalidFqnUsage.getActualUsage());
    }

    @Test
    public void getIndex() throws Exception {
        invalidFqnUsage = setUpInvalidFqnUsage(expectedLink, 1);
        assertEquals(1, invalidFqnUsage.getIndex());
    }

    @Test
    public void setIndex() throws Exception {
        invalidFqnUsage = setUpInvalidFqnUsage(expectedLink, 2);
        invalidFqnUsage.setIndex(3);
        assertEquals(3, invalidFqnUsage.getIndex());
    }

    @Test
    public void override_toString() throws Exception {
        invalidFqnUsage = setUpInvalidFqnUsage(expectedLink, 1);
        final String result = "InvalidFqnUsage{actualUsage=invalidFqnLink}";
        assertEquals(result, invalidFqnUsage.toString());
    }

}
