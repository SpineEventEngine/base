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

package io.spine.js.generate.resolve;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import io.spine.code.js.DirectoryReference;
import io.spine.code.js.ImportPath;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A pattern to match a directory.
 */
public final class DirectoryPattern {

    private static final String INCLUDE_NESTED_PATTERN_ENDING = "/*";

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
    @SuppressWarnings("ResultOfMethodCallIgnored" /* The result can be ignored. */)
    public static DirectoryPattern of(String value) {
        checkNotEmptyOrBlank(value);
        boolean includeNested = value.endsWith(INCLUDE_NESTED_PATTERN_ENDING);
        String directory;
        if (includeNested) {
            int nameEndIndex = value.length() - INCLUDE_NESTED_PATTERN_ENDING.length();
            directory = value.substring(0, nameEndIndex);
        } else {
            directory = value;
        }
        DirectoryReference reference = DirectoryReference.of(directory);
        return new DirectoryPattern(reference, includeNested);
    }

    /**
     * Checks if the pattern matches the specified directory.
     */
    boolean matches(DirectoryReference target) {
        Optional<Integer> firstElementMatch = firstMatchIndex(target);
        if (!firstElementMatch.isPresent()) {
            return false;
        }
        if (includeNested) {
            return true;
        }
        return matches(target, firstElementMatch.get());
    }

    private boolean matches(DirectoryReference target, int fromIndex) {
        List<String> targetElements = target.elements();
        List<String> patternElements = directory.elements();
        List<String> relevantPatternElements = patternElements.subList(fromIndex,
                                                                       patternElements.size());
        return relevantPatternElements.equals(targetElements);
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
     * Transforms the directory reference according to the pattern.
     *
     * @param origin
     *         the reference to transform
     * @return the updated reference
     */
    DirectoryReference transform(DirectoryReference origin) {
        checkState(matches(origin));
        Optional<Integer> firstMatchIndex = firstMatchIndex(origin);
        checkState(firstMatchIndex.isPresent());
        List<String> elementsToAdd = directory.elements()
                                              .subList(0, firstMatchIndex.get());
        List<String> resultElements = Lists.newArrayList();
        resultElements.addAll(elementsToAdd);
        resultElements.addAll(origin.elements());
        String joined = Joiner.on(ImportPath.separator())
                              .join(resultElements);
        return DirectoryReference.of(joined);
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
}
