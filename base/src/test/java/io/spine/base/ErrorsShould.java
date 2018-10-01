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

import com.google.common.testing.NullPointerTester;
import org.junit.Test;

import static io.spine.base.Errors.causeOf;
import static io.spine.base.Errors.fromThrowable;
import static io.spine.base.Identifier.newUuid;
import static io.spine.testing.Tests.assertHasPrivateParameterlessCtor;
import static org.junit.Assert.assertEquals;

public class ErrorsShould {

    @Test
    public void have_utility_ctor() {
        assertHasPrivateParameterlessCtor(Errors.class);
    }

    @Test
    public void pass_null_tolerance_check() {
        new NullPointerTester().testAllPublicStaticMethods(Errors.class);
    }

    @Test
    public void convert_cause_of_throwable_to_Error() {
        int errorCode = 404;
        String errorMessage = newUuid();

        // A Throwable with cause.
        RuntimeException throwable = new IllegalStateException(
                new RuntimeException(errorMessage)
        );

        Error error = causeOf(throwable, errorCode);

        assertEquals(errorCode, error.getCode());
        assertEquals(errorMessage, error.getMessage());
    }

    @Test
    public void convert_throwable_to_Error() {
        String errorMessage = newUuid();
        RuntimeException throwable = new RuntimeException(errorMessage);

        Error error = fromThrowable(throwable);

        assertEquals(errorMessage, error.getMessage());
    }
}
