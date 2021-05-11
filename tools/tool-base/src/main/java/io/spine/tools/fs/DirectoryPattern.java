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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Booleans;
import com.google.errorprone.annotations.Immutable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.spine.tools.fs.FileReference.joiner;
import static io.spine.tools.fs.FileReference.separator;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A pattern to match a directory, or the referenced directory and ones nested into it.
 *
 * <p>For the latter case, the passed value must end with {@link #INCLUDE_NESTED "/*"}.
 * Infix wildcard references are <em>not</em> supported.
 *
 * @see #of(String)
 */
@Immutable
public final class DirectoryPattern implements Comparable<DirectoryPattern> {

    /**
     * The suffix a pattern should have to add nested directories into the reference.
     */
    public static final String INCLUDE_NESTED = "/*";

    private final DirectoryReference directory;
    private final boolean includeNested;

    private DirectoryPattern(DirectoryReference directory, boolean includeNested) {
        this.directory = checkNotNull(directory);
        this.includeNested = includeNested;
    }

    /**
     * Creates a new instance.
     *
     * <p>The following formats are supported:
     * <ul>
     *     <li>The exact directory — {@code foo/bar}.
     *     <li>The directory and all subdirectories — {@code foo/bar/*}.
     * </ul>
     *
     * @param value
     *         the value of the pattern
     * @return a new instance
     */
    public static DirectoryPattern of(String value) {
        checkNotEmptyOrBlank(value);
        boolean includeNested = value.endsWith(INCLUDE_NESTED);
        String directory;
        if (includeNested) {
            int nameEndIndex = value.length() - INCLUDE_NESTED.length();
            directory = value.substring(0, nameEndIndex);
        } else {
            directory = value.endsWith(separator())
                        ? value.substring(0, value.length() - separator().length())
                        : value;
        }
        DirectoryReference reference = DirectoryReference.of(directory);
        return new DirectoryPattern(reference, includeNested);
    }

    /**
     * Creates a list of patterns from the passed values.
     */
    public static ImmutableList<DirectoryPattern> listOf(String... values) {
        checkNotNull(values);
        ImmutableList<DirectoryPattern> result =
                ImmutableList.copyOf(values)
                             .stream()
                             .map(DirectoryPattern::of)
                             .collect(toImmutableList());
        return result;
    }

    /**
     * Checks if the pattern matches the specified directory.
     */
    boolean matches(DirectoryReference target) {
        Optional<Integer> firstElementMatch = firstMatchIndex(target);
        return firstElementMatch.filter(index -> matches(target, index))
                                .isPresent();
    }

    /**
     * Transforms the directory reference according to the pattern.
     *
     * <p>It may prepend the specified reference with the root directories specified
     * in the pattern. So, for a pattern {@code foo/bar/*}, the directory {@code bar/buzz}
     * is transformed to {@code foo/bar/buzz}.
     *
     * @param origin
     *         the reference to transform
     * @return the updated reference
     */
    DirectoryReference transform(DirectoryReference origin) {
        checkState(matches(origin));
        Optional<Integer> firstMatchIndex = firstMatchIndex(origin);
        checkState(firstMatchIndex.isPresent());
        List<String> missingElements =
                directory.elements()
                         .subList(0, firstMatchIndex.get());
        List<String> resultElements = ImmutableList.<String>builder()
                .addAll(missingElements)
                .addAll(origin.elements())
                .build();
        String result = joiner().join(resultElements);
        return DirectoryReference.of(result);
    }

    private boolean matches(DirectoryReference target, int fromIndex) {
        List<String> patternElements = directory.elements();
        List<String> relevantPattern =
                patternElements.subList(fromIndex, patternElements.size());
        List<String> targetElements = target.elements();
        if (relevantPattern.size() > targetElements.size()) {
            return false;
        }
        int lastRelevantTarget =
                includeNested
                ? relevantPattern.size()
                : targetElements.size();
        List<String> relevantTarget = targetElements.subList(0, lastRelevantTarget);
        return relevantPattern.equals(relevantTarget);
    }

    private Optional<Integer> firstMatchIndex(DirectoryReference target) {
        List<String> patternElements = directory.elements();
        String firstTargetElement = target.elements()
                                          .get(0);
        int index = patternElements.indexOf(firstTargetElement);
        return index == -1
               ? Optional.empty()
               : Optional.of(index);
    }

    /**
     * Obtains the directory name used in the pattern.
     */
    @VisibleForTesting
    DirectoryReference directoryName() {
        return directory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DirectoryPattern)) {
            return false;
        }
        DirectoryPattern pattern = (DirectoryPattern) o;
        return includeNested == pattern.includeNested &&
                directory.equals(pattern.directory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(directory, includeNested);
    }

    @Override
    public int compareTo(DirectoryPattern o) {
        int dirResult = directory.compareTo(o.directory);
        if (dirResult != 0) {
            return dirResult;
        }
        return Booleans.compare(includeNested, o.includeNested);
    }
}
