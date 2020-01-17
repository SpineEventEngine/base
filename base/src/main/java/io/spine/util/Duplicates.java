/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.util;

import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Utilities for working with duplicates in collections.
 */
public final class Duplicates {

    /**
     * Prevents the utility class instantiation.
     */
    private Duplicates() {
    }

    /**
     * Finds all the duplicate elements in the given collection.
     *
     * @param elements
     *         a collection which may contain duplicates
     * @return a set of duplicate elements or an empty set if all the {@code elements} are distinct
     */
    public static ImmutableSet<?> findIn(Collection<?> elements) {
        Set<? super Object> uniques = new HashSet<>();
        ImmutableSet.Builder<? super Object> duplicates = ImmutableSet.builder();
        elements.forEach(potentialDuplicate -> {
            if (uniques.contains(potentialDuplicate)) {
                duplicates.add(potentialDuplicate);
            } else {
                uniques.add(potentialDuplicate);
            }
        });
        return duplicates.build();
    }
}
