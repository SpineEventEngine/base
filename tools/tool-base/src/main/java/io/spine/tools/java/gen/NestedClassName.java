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

package io.spine.tools.java.gen;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import io.spine.code.java.ClassName;
import io.spine.code.java.SimpleClassName;
import io.spine.value.StringTypeValue;

import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * A name of a potentially nested class with outer class names separated with dots.
 *
 * <p>A top level class name would have equal to {@link SimpleClassName}.
 */
public final class NestedClassName extends StringTypeValue {

    private static final long serialVersionUID = 0L;

    private static final char NESTED_CLASS_SEPARATOR = '.';

    private static final Splitter nameSplitter = Splitter.on(NESTED_CLASS_SEPARATOR)
                                                         .omitEmptyStrings();

    private NestedClassName(String value) {
        super(value);
    }

    /**
     * Creates a new instance by the fully-qualified name.
     */
    public static NestedClassName from(ClassName className) {
        String nameWithOuter = className.withoutPackage();
        return new NestedClassName(nameWithOuter);
    }

    /**
     * Obtains this class name as a list of simple class names sorted in the nesting order.
     *
     * <p>For example, if this class name is {@code "Container.Job.Builder"}, then the resulting
     * list would be {@code ["Container", "Job", "Builder"]}.
     *
     * <p>If this class name is just a {@link SimpleClassName}, then the only entry of the resulting
     * list is that simple name.
     *
     * @return this name split into simple class names
     */
    public ImmutableList<SimpleClassName> split() {
        String fullName = value();
        ImmutableList<SimpleClassName> result =
                nameSplitter.splitToList(fullName)
                            .stream()
                            .map(SimpleClassName::create)
                            .collect(toImmutableList());
        return result;
    }
}
