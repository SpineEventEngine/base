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

package io.spine.code.java;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import io.spine.value.StringTypeValue;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A simple name of a Java class including all the simple names of the nesting classes.
 */
public final class NestedClassName extends StringTypeValue {

    private static final long serialVersionUID = 0L;

    private static final Joiner classNameJoiner = Joiner.on('.');
    private static final Joiner underscoreNameJoiner = Joiner.on('_');
    private final ImmutableList<String> names;

    private NestedClassName(List<String> names, String joined) {
        super(checkNotNull(joined));
        this.names = ImmutableList.copyOf(names);
    }

    static NestedClassName from(List<String> names) {
        checkNotNull(names);
        String name = classNameJoiner.join(names);
        return new NestedClassName(names, name);
    }

    public String joinWithUnderscore() {
        return underscoreNameJoiner.join(names);
    }
}
