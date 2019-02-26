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

package io.spine.tools.gradle.compiler.protoc;

import io.spine.annotation.Internal;
import io.spine.tools.protoc.GeneratedInterface;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.regex.qual.Regex;

/**
 * An {@link GeneratedInterfaceConfig interface} configuration file pattern
 * {@link FilePatternFactory factory}.
 */
public final class InterfaceFilePatternFactory extends FilePatternFactory<
        GeneratedInterface, InterfacePostfixPattern, InterfacePrefixPattern, InterfaceRegexPattern> {

    /** Prevents direct instantiation. **/
    InterfaceFilePatternFactory() {
        super();
    }

    @Internal
    @Override
    InterfacePostfixPattern newPostfixPattern(@NonNull @Regex String postfix) {
        return new InterfacePostfixPattern(postfix);
    }

    @Internal
    @Override
    InterfacePrefixPattern newPrefixPattern(@NonNull @Regex String prefix) {
        return new InterfacePrefixPattern(prefix);
    }

    @Internal
    @Override
    InterfaceRegexPattern newRegexPattern(@NonNull @Regex String regex) {
        return new InterfaceRegexPattern(regex);
    }
}
