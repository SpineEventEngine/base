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

package io.spine.tools.protoc.plugin;

import io.spine.tools.protoc.FilePattern;
import io.spine.type.MessageType;

import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link FilePattern} predicate that returns {@code true} if supplied Protobuf
 * {@link MessageType type} matches pattern's value.
 */
public final class FilePatternMatcher implements Predicate<MessageType> {

    private final FilePattern pattern;

    public FilePatternMatcher(FilePattern filePattern) {
        checkNotNull(filePattern);
        this.pattern = filePattern;
    }

    @Override
    public boolean test(MessageType type) {
        checkNotNull(type);
        String protoFileName = type.declaringFileName()
                                   .value();
        switch (pattern.getValueCase()) {
            case SUFFIX:
                return protoFileName.endsWith(pattern.getSuffix());
            case PREFIX:
                return protoFileName.startsWith(pattern.getPrefix());
            case REGEX:
                return protoFileName.matches(pattern.getRegex());
            case VALUE_NOT_SET:
            default:
                return false;
        }
    }
}
