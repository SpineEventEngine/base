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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

/**
 * An abstract code style violations storage.
 *
 * @author Alexander Aleksandrov
 * @author Alexander Yevsyukov
 */
public abstract class LineStorage {

    private final Multimap<Path, CodeStyleViolation> content = HashMultimap.create();

    /**
     * Obtains a number of stored violations.
     */
    public int size() {
        return content.size();
    }

    /**
     * Obtains file-to-violation entries.
     */
    protected Collection<Map.Entry<Path, CodeStyleViolation>> entries() {
        return content.entries();
    }

    /**
     * Logs all violations from storage.
     */
    public abstract void logViolations();

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

    protected Logger log() {
        return LoggerFactory.getLogger(getClass());
    }
}
