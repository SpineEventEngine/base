/*
 * Copyright 2020, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.tools.check.vbuild.given;

import io.spine.base.Error;

/**
 * Contains statements for which the {@link HandleMethodResult} bug pattern should return a match.
 *
 * <p>Comments in this file should not be modified as they serve as indicator for the
 * {@link com.google.errorprone.CompilationTestHelper} Error Prone tool.
 */
class HandleMethodResultPositives {

    void callBuild() {

        // BUG: Diagnostic matches: HandleMethodResult
        Error.newBuilder().vBuild();
    }

    void callGetter() {

        // BUG: Diagnostic matches: HandleMethodResult
        Error.newBuilder().getAttributesCount();
    }

    void callAsMethodReference() {
        Error.Builder builder = Error.newBuilder();

        // BUG: Diagnostic matches: HandleMethodResult
        Runnable faulty = builder::vBuild;
        faulty.run();
    }

    void callNonBuilder() {
        // BUG: Diagnostic matches: HandleMethodResult
        checkMe();
    }

    public String checkMe() {
        return "42";
    }
}
