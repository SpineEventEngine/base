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

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.protobuf.Message;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import io.spine.tools.check.BugPatternMatcher;
import io.spine.tools.check.Fixer;
import io.spine.validate.ValidatingBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.errorprone.BugPattern.Category.JDK;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.matchers.Description.NO_MATCH;
import static com.google.errorprone.matchers.Matchers.isSubtypeOf;

/**
 * Matches on using ordinary Builder instead of VBuilder for Proto Messages.
 */
@AutoService(BugChecker.class)
@BugPattern(
        name = "UseVBuilder",
        category = JDK,
        summary = UseVBuilder.SUMMARY,
        severity = WARNING,
        link = UseVBuilder.LINK)
public class UseVBuilder extends BugChecker implements MethodInvocationTreeMatcher {

    static final String SUMMARY = "Prefer using Spine Validating Builders instead of ordinary " +
            "Builders for Protobuf Messages";

    static final String LINK =
            "https://github.com/SpineEventEngine/core-java/wiki/Validating-Builders-Generation";

    private static final long serialVersionUID = 0L;

    private static final List<BugPatternMatcher<MethodInvocationTree>> matchers = matchers();

    @Override
    public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
        for (BugPatternMatcher<MethodInvocationTree> matcher : matchers()) {
            if (matcher.matches(tree, state) && !isInVBuilderOrMessage(state)) {
                Fixer<MethodInvocationTree> fixer = matcher.getFixer();
                Optional<Fix> fix = fixer.createFix(tree, state);
                Description description = describeMatch(tree, fix);
                return description;
            }
        }
        return NO_MATCH;
    }

    private static boolean isInVBuilderOrMessage(VisitorState state) {
        ClassTree enclosingClass = state.findEnclosing(ClassTree.class);
        boolean isInVBuilder = inVBuilderMatcher().matches(enclosingClass, state);
        boolean isInMessage = inMessageMatcher().matches(enclosingClass, state);
        return isInVBuilder || isInMessage;
    }

    private static Matcher<Tree> inVBuilderMatcher() {
        Matcher<Tree> matcher = isSubtypeOf(ValidatingBuilder.class);
        return matcher;
    }

    private static Matcher<Tree> inMessageMatcher() {
        Matcher<Tree> matcher = isSubtypeOf(Message.class);
        return matcher;
    }

    private static List<BugPatternMatcher<MethodInvocationTree>> matchers() {
        List<BugPatternMatcher<MethodInvocationTree>> fixers = new ArrayList<>();
        fixers.add(new NewBuilderMatcher());
        fixers.add(new NewBuilderForTypeMatcher());
        fixers.add(new ToBuilderMatcher());
        return fixers;
    }
}
