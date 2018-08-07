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

import com.google.common.base.Predicates;
import com.google.errorprone.CompilationTestHelper;
import org.junit.Before;
import org.junit.Test;

import java.util.function.Predicate;

/**
 * This test requires configuring "-Xbootclasspath..." option with the path to the
 * {@code com.google.errorprone.javac} jar.
 *
 * <p>In Gradle it's done automatically via the separate task (see the {@code build.gradle} of this
 * module).
 *
 * <p>To run the test in Idea you need to add VM options manually in the "Edit configurations" tab.
 * Typically, the {@code javac} jar can be found in the Gradle Caches directory in the
 * "modules-2/files-2.1/com.google.errorprone/javac/" folder or its subfolders.
 *
 * <p>After you acquired the path to the existing {@code javac} jar, add the following VM Option:
 * <pre>
 *  -Xbootclasspath/p:`javacPath`
 * </pre>
 * where the `javacPath` is the path to your {@code javac} jar.
 *
 * <p>For the information about how this test suite works, see the Error Prone
 * <a href="https://github.com/google/error-prone/wiki/Writing-a-check#testing-a-bugchecker">
 * guide</a> to testing the custom checks.
 *
 * @author Dmytro Kuzmin
 */
public class UseValidatingBuilderShould {

    private CompilationTestHelper compilationTestHelper;

    @Before
    public void setUp() {
        compilationTestHelper =
                CompilationTestHelper.newInstance(UseValidatingBuilder.class, getClass());
    }

    @Test
    public void recognize_positive_cases() {
        Predicate<CharSequence> predicate =
                Predicates.containsPattern(UseValidatingBuilder.SUMMARY)::apply;
        compilationTestHelper.expectErrorMessage("UseValidatingBuilderError", predicate::test);
        compilationTestHelper.addSourceFile("UseValidatingBuilderPositives.java")
                             .doTest();
    }

    @Test
    public void recognize_negative_cases() {
        compilationTestHelper.addSourceFile("UseValidatingBuilderNegatives.java")
                             .doTest();
    }
}
