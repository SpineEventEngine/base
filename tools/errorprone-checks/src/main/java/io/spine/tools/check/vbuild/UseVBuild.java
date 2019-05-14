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

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.MethodInvocationTree;
import io.spine.tools.check.BugPatternMatcher;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.matchers.Description.NO_MATCH;

/**
 * An ErrorProne check which warns users to prefer
 * {@link io.spine.protobuf.ValidatingBuilder#vBuild()} over
 * {@link io.spine.protobuf.ValidatingBuilder#build()}.
 *
 * <p>Unlink {@code build()}, {@code vBuild()} ensures that the constructed message is valid. This
 * is what the user wants in most cases. If, however, for some reason, the validation is unwanted,
 * the user in encouraged to use {@code buildPartial()} in order to make the intent explicit.
 */
// TODO:2019-05-13:dmytro.dashenkov: Add a link to documentation.
@AutoService(BugChecker.class)
@BugPattern(
        name = "UseVBuild",
        summary = UseVBuild.SUMMARY,
        severity = WARNING,
        linkType = NONE
)
public class UseVBuild extends BugChecker implements MethodInvocationTreeMatcher {

    private static final long serialVersionUID = 0L;

    static final String NAME = UseVBuild.class.getSimpleName();
    static final String SUMMARY = "Prefer using vBuild() instead of the build().";

    @Override
    public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
        BugPatternMatcher<MethodInvocationTree> matcher = BuildMatcher.INSTANCE;
        boolean matches = matcher.matches(tree, state);
        if (matches) {
            ImmutableList<Fix> fixes = matcher
                    .fixers()
                    .stream()
                    .map(fixer -> fixer.suggestFix(tree))
                    .collect(toImmutableList());
            Description description = Description
                    .builder(tree, UseVBuild.class.getSimpleName(), null, WARNING, SUMMARY)
                    .addAllFixes(fixes)
                    .build();
            return description;
        } else {
            return NO_MATCH;
        }
    }
}
