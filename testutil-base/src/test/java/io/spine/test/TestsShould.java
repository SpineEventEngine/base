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

package io.spine.test;

import org.junit.Test;

import static io.spine.test.Tests.assertHasPrivateParameterlessCtor;
import static io.spine.test.Tests.hasPrivateParameterlessCtor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestsShould {

    @Test
    public void have_private_constructor() {
        assertHasPrivateParameterlessCtor(Tests.class);
    }

    @Test
    public void return_false_if_no_private_but_public_ctor() {
        assertFalse(hasPrivateParameterlessCtor(ClassWithPublicCtor.class));
    }

    @Test
    public void return_false_if_only_ctor_with_args_found() {
        assertFalse(hasPrivateParameterlessCtor(ClassWithCtorWithArgs.class));
    }

    @Test
    public void return_true_if_class_has_private_ctor() {
        assertTrue(hasPrivateParameterlessCtor(ClassWithPrivateCtor.class));
    }

    @Test
    public void return_true_if_class_has_private_throwing_ctor() {
        assertTrue(hasPrivateParameterlessCtor(ClassThrowingExceptionInConstructor.class));
    }

    @Test
    public void return_null_reference() {
        assertNull(Tests.nullRef());
    }

    @Test
    public void assert_equality_of_booleans() {
        assertEquals(true, true);
        assertEquals(false, false);
    }

    @Test(expected = AssertionError.class)
    public void fail_boolean_inequality_assertion() {
        // This should fail.
        Tests.assertEquals(true, false);
    }

    @Test
    public void have_own_boolean_assertion() {
        Tests.assertTrue(true);
    }

    @SuppressWarnings("ConstantConditions") // The call with `false` should always fail.
    @Test(expected = AssertionError.class)
    public void have_own_assertTrue() {
        Tests.assertTrue(false);
    }

    /*
     * Test environment classes
     ***************************/

    private static class ClassWithPrivateCtor {
        @SuppressWarnings("RedundantNoArgConstructor") // We need this constructor for our tests.
        private ClassWithPrivateCtor() {}
    }

    private static class ClassWithPublicCtor {
        @SuppressWarnings("PublicConstructorInNonPublicClass") // It's the purpose of this
                                                               // test class.
        public ClassWithPublicCtor() {}
    }

    private static class ClassThrowingExceptionInConstructor {
        private ClassThrowingExceptionInConstructor() {
            throw new AssertionError("This private constructor must not be called.");
        }
    }

    private static class ClassWithCtorWithArgs {
        @SuppressWarnings("unused")
        private final int id;
        private ClassWithCtorWithArgs(int id) { this.id = id;}
    }
}
