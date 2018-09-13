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

package io.spine.tools.protojs.given;

import io.spine.testing.Verify;
import io.spine.tools.protojs.code.JsOutput;

import static io.spine.testing.Verify.assertContains;
import static io.spine.testing.Verify.assertNotContains;

/**
 * A helper tool for working with generators output.
 *
 * @author Dmytro Kuzmin
 */
public final class Generators {

    /** Prevents instantiation of this utility class. */
    private Generators() {
    }

    public static void assertContains(JsOutput jsOutput, CharSequence toSearch) {
        String codeString = jsOutput.toString();
        Verify.assertContains(toSearch, codeString);
    }

    public static void assertNotContains(JsOutput jsOutput, CharSequence toSearch) {
        String codeString = jsOutput.toString();
        Verify.assertNotContains(toSearch, codeString);
    }
}
