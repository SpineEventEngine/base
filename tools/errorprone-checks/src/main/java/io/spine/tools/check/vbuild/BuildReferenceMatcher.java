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

package io.spine.tools.check.vbuild;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.Tree;
import io.spine.protobuf.ValidatingBuilder;

import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.matchers.Matchers.isSubtypeOf;
import static io.spine.tools.check.vbuild.UseVBuild.BUILD;

/**
 * A matcher for the {@link io.spine.tools.check.vbuild.UseVBuild} bug pattern which tracks down
 * the cases where the {@code builder::build} statement is used.
 */
enum BuildReferenceMatcher implements ContextualMatcher<MemberReferenceTree> {

    INSTANCE;

    private static final Matcher<Tree> receiverMatcher = isSubtypeOf(ValidatingBuilder.class);

    @Override
    public boolean outsideMessageContextMatches(MemberReferenceTree tree, VisitorState state) {
        return receiverMatcher.matches(tree.getQualifierExpression(), state)
                && tree.getName()
                       .contentEquals(BUILD);
    }

    @Override
    public ImmutableList<Fix> fixes(MemberReferenceTree tree) {
        String receiver = tree.getQualifierExpression().toString();
        return Stream.of(BuildMethodAlternative.values())
                     .map(BuildMethodAlternative::name)
                     .map(name -> methodReference(receiver, name))
                     .map(replacement -> SuggestedFix.replace(tree, replacement))
                     .collect(toImmutableList());
    }

    private static String methodReference(String receiver, String methodName) {
        return receiver + "::" + methodName;
    }
}
