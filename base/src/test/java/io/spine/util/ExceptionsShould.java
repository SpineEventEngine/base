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

package io.spine.util;

import com.google.common.testing.NullPointerTester;
import io.spine.test.TestValues;
import org.junit.Test;

import static io.spine.test.Tests.assertHasPrivateParameterlessCtor;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static io.spine.util.Exceptions.newIllegalStateException;
import static io.spine.util.Exceptions.unsupported;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Alexander Litus
 * @author Alexander Yevsyukov
 */
@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
public class ExceptionsShould {

    @Test
    public void have_private_ctor() {
        assertHasPrivateParameterlessCtor(Exceptions.class);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void create_and_throw_unsupported_operation_exception() {
        unsupported();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void create_and_throw_unsupported_operation_exception_with_message() {
        unsupported(TestValues.randomString());
    }

    @Test
    public void throw_formatted_unsupported_exception() {
        final String arg1 = getClass().getCanonicalName();
        final long arg2 = 100500L;
        try {
            unsupported("%s %d", arg1, arg2);
            fail();
        } catch (UnsupportedOperationException e) {
            final String exceptionMessage = e.getMessage();
            assertTrue(exceptionMessage.contains(arg1));
            assertTrue(exceptionMessage.contains(String.valueOf(arg2)));
        }
    }

    @Test
    public void pass_the_null_tolerance_check() {
        new NullPointerTester()
                .setDefault(Exception.class, new RuntimeException(""))
                .setDefault(Throwable.class, new Error())
                .testAllPublicStaticMethods(Exceptions.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throw_formatted_IAE() {
        newIllegalArgumentException("%d, %d, %s kaboom", 1, 2, "three");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throw_formatted_IAE_with_cause() {
        newIllegalArgumentException(new RuntimeException("checking"), "%s", "stuff");
    }

    @Test(expected = IllegalStateException.class)
    public void throw_formatted_ISE() {
        newIllegalStateException("%s check %s", "state", "failed");
    }

    @Test(expected = IllegalStateException.class)
    public void throw_formatted_ISE_with_cause() {
        newIllegalStateException(new RuntimeException(getClass().getSimpleName()),
                                            "%s %s", "taram", "param");
    }
}
