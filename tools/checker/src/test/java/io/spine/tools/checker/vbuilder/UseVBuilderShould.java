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

package io.spine.tools.checker.vbuilder;

import com.google.common.base.Predicates;
import com.google.errorprone.CompilationTestHelper;
import io.spine.tools.checker.vbuilder.UseVBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.function.Predicate;

public class UseVBuilderShould {

    private CompilationTestHelper compilationTestHelper;

    @Before
    public void setUp() {
        compilationTestHelper = CompilationTestHelper.newInstance(UseVBuilder.class, getClass());
    }

    @Test
    public void recognize_positive_cases() {
        Predicate<CharSequence> predicate = Predicates.containsPattern(UseVBuilder.SUMMARY)::apply;
        compilationTestHelper.expectErrorMessage("UseVBuilderError", predicate::test);
        compilationTestHelper.addSourceFile("UseVBuilderPositives.java")
                             .doTest();
    }

    @Test
    public void recognize_negative_cases() {
        compilationTestHelper.addSourceFile("UseVBuilderNegatives.java")
                             .doTest();
    }
}
