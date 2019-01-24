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

import java.util.Objects;

import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A pattern to match a directory.
 */
public final class DirectoryPattern {

    private static final String INCLUDE_NESTED_PATTERN_ENDING = "/*";

    private final String directoryName;
    private final boolean includeNested;

    private DirectoryPattern(String directoryName, boolean includeNested) {
        this.directoryName = checkNotEmptyOrBlank(directoryName);
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
        String directoryName;
        if (includeNested) {
            int nameEndIndex = value.length() - INCLUDE_NESTED_PATTERN_ENDING.length();
            directoryName = value.substring(0, nameEndIndex);
        } else {
            directoryName = value;
        }
        return new DirectoryPattern(directoryName, includeNested);
    }

    /**
     * Checks if the pattern matches the specified directory.
     */
    boolean matches(String targetDirectory) {
        if (directoryName.equals(targetDirectory)) {
            return true;
        }
        boolean rootMatches = targetDirectory.startsWith(directoryName);
        if (includeNested && rootMatches) {
            return true;
        }
        boolean endingMatches = directoryName.endsWith(targetDirectory);
        return endingMatches;
    }

    /**
     * Obtains the directory name used in the pattern.
     */
    String directoryName() {
        return directoryName;
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
                directoryName.equals(pattern.directoryName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(directoryName, includeNested);
    }
}
