/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

package io.spine.tools;

import com.google.common.testing.NullPointerTester;
import io.spine.test.Tests;
import io.spine.util.Preconditions2;
import org.junit.Test;

import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * @author Alexander Yevsyukov
 */
public class Preconditions2Should {

    @Test
    public void have_utility_ctor() {
        Tests.assertHasPrivateParameterlessCtor(Preconditions2.class);
    }

    @Test
    public void check_nullity() {
        new NullPointerTester().testAllPublicStaticMethods(Preconditions2.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void prohibit_empty_string() {
        checkNotEmptyOrBlank("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void prohibit_blank_string() {
        checkNotEmptyOrBlank(" ");
    }
}
