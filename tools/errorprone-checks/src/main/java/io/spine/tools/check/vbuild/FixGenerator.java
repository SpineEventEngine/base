/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.tools.check.vbuild;

import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.MethodInvocationTree;

/**
 * A generator for the common {@link com.google.errorprone.BugPattern} {@linkplain Fix fixes}
 * related to the {@link io.spine.validate.ValidatingBuilder} usage.
 *
 * <p>This class should only be used from the Error Prone
 * {@link com.google.errorprone.bugpatterns.BugChecker} context, where the code scanners can provide
 * proper {@link MethodInvocationTree} and {@link VisitorState} for its initialization.
 *
 * @see io.spine.tools.check.vbuilder.UseValidatingBuilder
 */
class FixGenerator {

    private static final String V_BUILD = "vBuild";

    private final MethodInvocationTree tree;

    private FixGenerator(MethodInvocationTree tree) {
        this.tree = tree;
    }

    /**
     * Creates the {@code FixGenerator} instance for the given expression and visitor state.
     *
     * @param tree  the expression {@code Tree}
     * @return the {@code FixGenerator} instance for the given expression
     */
    static FixGenerator createFor(MethodInvocationTree tree) {
        return new FixGenerator(tree);
    }

    /**
     * Creates a fix which replaces the current expression with the {@code ...VBuilder.newBuilder()}
     * expression.
     *
     * <p>This method assumes that the {@linkplain #tree current expression} is the call on some of
     * the {@link com.google.protobuf.Message} class descendants.
     *
     * @return the {@code Fix} which can be later displayed to the user via the Error Prone tools
     */
    Fix vBuildCall() {
        Fix fix = SuggestedFix.replace(tree.getMethodSelect(), V_BUILD);
        return fix;
    }
}
