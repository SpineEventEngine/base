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

package io.spine.tools.protoc;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A selector which signalizes that the configuration should be applied to all messages declared in
 * proto files matching some pattern.
 *
 * @see WithPrefix
 * @see ByRegex
 * @see WithSuffix
 */
public abstract class ByPattern extends MessageSelector {

    private final String pattern;

    ByPattern(String pattern) {
        super();
        this.pattern = checkNotEmptyOrBlank(pattern);
    }

    /**
     * Returns current file pattern.
     */
    String getPattern() {
        return pattern;
    }

    /**
     * Converts current selector to its Protobuf configuration counterpart.
     */
    public abstract FilePattern toProto();

    @Override
    public String toString() {
        return MoreObjects
                .toStringHelper(this)
                .add("pattern", pattern)
                .toString();
    }

    @SuppressWarnings("EqualsGetClass") // we do want to distinguish different file patterns here
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ByPattern selector = (ByPattern) o;
        return Objects.equal(getPattern(), selector.getPattern());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getPattern());
    }
}
