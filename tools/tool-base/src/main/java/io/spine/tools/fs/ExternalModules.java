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

package io.spine.tools.fs;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * A collection of {@link ExternalModule}s.
 */
@Immutable
public final class ExternalModules {

    private final ImmutableList<ExternalModule> modules;

    /**
     * Creates a new instance with the passed modules.
     */
    public ExternalModules(Iterable<ExternalModule> modules) {
        checkNotNull(modules);
        this.modules = ImmutableList.copyOf(modules);
    }

    /**
     * Creates a new instance with the passed modules.
     */
    public ExternalModules(ExternalModule... modules) {
        this(ImmutableList.copyOf(modules));
    }

    /**
     * Creates an instance from a raw representation from a Gradle extension.
     */
    public ExternalModules(Map<String, List<String>> modules) {
        this(convert(checkNotNull(modules)));
    }

    private static ImmutableList<ExternalModule> convert(Map<String, List<String>> modules) {
        ImmutableList<ExternalModule> list =
                modules.entrySet()
                       .stream()
                       .map(kv -> new ExternalModule(kv.getKey(), patterns(kv.getValue())))
                       .collect(toImmutableList());
        return list;
    }

    private static ImmutableList<DirectoryPattern> patterns(List<String> rawPatterns) {
        return rawPatterns.stream()
                          .sorted()
                          .map(DirectoryPattern::of)
                          .collect(toImmutableList());
    }

    /**
     * Creates a new instance combining modules of this instance with the passed ones.
     */
    public ExternalModules with(Iterable<ExternalModule> newModules) {
        checkNotNull(newModules);
        ImmutableList<ExternalModule> combined = ImmutableList.<ExternalModule>builder()
                .addAll(this.modules)
                .addAll(newModules)
                .build();
        return new ExternalModules(combined);
    }

    /**
     * Obtains the modules as a list.
     */
    public ImmutableList<ExternalModule> asList() {
        return modules;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExternalModules)) {
            return false;
        }
        ExternalModules other = (ExternalModules) o;
        return modules.equals(other.modules);
    }

    @Override
    public int hashCode() {
        return modules.hashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("modules", modules)
                          .toString();
    }
}
