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

import com.google.common.collect.Lists;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

/**
 * Violations found in a file.
 *
 * @author Alexander Aleksandrov
 * @author Alexander Yevsyukov
 */
class FileViolations {

    private final Path file;
    private final List<CodeStyleViolation> content = Lists.newArrayList();

    FileViolations(Path file) {
        super();
        this.file = file;
    }

    /**
     * Obtains a number of stored violations.
     */
    int size() {
        return content.size();
    }

    /**
     * Logs all violations by invoking the parent check callback.
     */
    void reportViolations(CodeStyleCheck parent) {
        for (CodeStyleViolation v : content) {
            parent.onViolation(file, v);
        }
    }

    /**
     * Saves violations found in a file.
     */
    void save(Collection<CodeStyleViolation> violations) {
        content.addAll(violations);
    }
}
