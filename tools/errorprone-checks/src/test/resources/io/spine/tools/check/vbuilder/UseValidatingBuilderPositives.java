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

package io.spine.tools.check.vbuilder;

import io.spine.base.FieldPath;
import io.spine.base.Error;

import static io.spine.base.FieldPath.newBuilder;

/**
 * Contains statements for which the {@link UseValidatingBuilder} bug pattern should return a match.
 *
 * <p>Comments in this file should not be modified as they serve as indicator for the
 * {@link com.google.errorprone.CompilationTestHelper} Error Prone tool.
 */
class UseValidatingBuilderPositives {

    Error value = Error.getDefaultInstance();

    void callNewBuilder() {

        // BUG: Diagnostic matches: UseValidatingBuilderError
        Error.newBuilder();
    }

    void callNewBuilderWithArg() {

        // BUG: Diagnostic matches: UseValidatingBuilderError
        Error.newBuilder(value);
    }

    void callNewBuilderForType() {

        // BUG: Diagnostic matches: UseValidatingBuilderError
        value.newBuilderForType();
    }

    void callToBuilder() {

        // BUG: Diagnostic matches: UseValidatingBuilderError
        value.toBuilder();
    }

    void callNewBuilderStaticImported() {

        // BUG: Diagnostic matches: UseValidatingBuilderError
        newBuilder();
    }

    void callNewBuilderWithArgStaticImported() {
        FieldPath defaultInstance = FieldPath.getDefaultInstance();

        // BUG: Diagnostic matches: UseValidatingBuilderError
        newBuilder(defaultInstance);
    }
}
