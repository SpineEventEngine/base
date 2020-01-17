/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.testing;

/**
 * A storage for the common JUnit 5 test
 * <a href="https://junit.org/junit5/docs/5.0.3/api/org/junit/jupiter/api/DisplayName.html">
 * display names</a>.
 *
 * <p>This class can be used to avoid string literal duplication when assigning {@code DisplayName}
 * to the common test cases.
 */
public final class DisplayNames {

    /**
     * A name for the test cases checking that a class has private parameterless (aka "utility")
     * constructor.
     */
    public static final String HAVE_PARAMETERLESS_CTOR = "have private parameterless constructor";

    /**
     * A name for the test cases checking that class methods do not accept {@code null} for their
     * non-{@linkplain javax.annotation.Nullable nullable} arguments.
     */
    public static final String NOT_ACCEPT_NULLS =
            "not accept nulls for non-Nullable method arguments";

    /**
     * Prevents instantiation of this class.
     */
    private DisplayNames() {
    }
}
