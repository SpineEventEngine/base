/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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
package io.spine.tools.codestyle;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.nio.file.Path;
import java.util.Map;

/**
 * An abstract code style violations storage.
 *
 * @author Alexander Aleksandrov
 * @author Alexander Yevsyukov
 */
public class LineStorage {

    private final Multimap<Path, CodeStyleViolation> content = HashMultimap.create();

    LineStorage() {
        super();
    }

    /**
     * Obtains a number of stored violations.
     */
    public int size() {
        return content.size();
    }

    /**
     * Logs all violations from storage.
     *
     * @param parent the parent check
     */
    public void reportViolations(CodeStyleCheck parent) {
        for (Map.Entry<Path, CodeStyleViolation> entry : content.entries()) {
            final CodeStyleViolation v = entry.getValue();
            final Path file = entry.getKey();
            parent.onViolation(file, v);
        }
    }

    /**
     * Saves violations found in a file.
     */
    void save(Path file, Iterable<CodeStyleViolation> violations) {
        content.putAll(file, violations);
    }

    /**
     * Clears the content of the storage.
     */
    void clear() {
        content.clear();
    }
}
