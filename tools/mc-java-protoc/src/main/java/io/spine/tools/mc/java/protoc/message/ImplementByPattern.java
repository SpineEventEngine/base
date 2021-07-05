/*
 * Copyright 2021, TeamDev. All rights reserved.
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

package io.spine.tools.mc.java.protoc.message;

import com.google.common.collect.ImmutableList;
import io.spine.tools.mc.java.protoc.CompilerOutput;
import io.spine.tools.mc.java.protoc.FilePatternMatcher;
import io.spine.tools.mc.java.protoc.PatternMatcher;
import io.spine.tools.protoc.FilePattern;
import io.spine.tools.protoc.JavaClassName;
import io.spine.tools.protoc.Pattern;
import io.spine.type.MessageType;

import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Preconditions2.checkNotDefaultArg;

/**
 * Generates interfaces for Protobuf messages that match supplied pattern.
 *
 * <p>The message class implements an interface if:
 * <ul>
 *     <li>the message is a {@link MessageType#isTopLevel() top level} declaration;
 *     <li>either the type matches the type pattern or the file where the type is declared matches
 *         the file pattern.
 * </ul>
 */
final class ImplementByPattern extends ImplementInterface {

    private final Predicate<MessageType> matcher;

    /**
     * Creates a new {@code ImplementByPattern} with the given interface name and file pattern.
     *
     * <p>Top-level messages declared in files which match this pattern will implement
     * the given interface.
     */
    ImplementByPattern(JavaClassName interfaceName, FilePattern pattern) {
        super(interfaceName);
        checkNotDefaultArg(pattern);
        this.matcher = new FilePatternMatcher(pattern);
    }

    /**
     * Creates a new {@code ImplementByPattern} with the given interface name and pattern.
     *
     * <p>Top-level messages matching this pattern will implement the given interface.
     */
    ImplementByPattern(JavaClassName interfaceName, Pattern pattern) {
        super(interfaceName);
        checkNotDefaultArg(pattern);
        this.matcher = new PatternMatcher(pattern);
    }

    @Override
    public InterfaceParameters interfaceParameters(MessageType type) {
        return InterfaceParameters.empty();
    }

    @Override
    public ImmutableList<CompilerOutput> generateFor(MessageType type) {
        checkNotNull(type);
        if (type.isTopLevel() && matcher.test(type)) {
            return super.generateFor(type);
        }
        return ImmutableList.of();
    }
}
