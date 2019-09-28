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

package io.spine.tools.gradle;

import com.google.common.annotations.VisibleForTesting;
import io.spine.code.AbstractDirectory;
import org.gradle.api.Project;

import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A root directory to generate source code into.
 */
public final class GeneratedSourceRoot extends AbstractDirectory {

    @VisibleForTesting
    @SuppressWarnings("DuplicateStringLiteralInspection") // Used in another context.
    static final String GENERATED = "generated";

    private GeneratedSourceRoot(Path path) {
        super(path);
    }

    /**
     * Obtains the generated source code root directory of the given project.
     */
    public static GeneratedSourceRoot of(Project project) {
        checkNotNull(project);
        Path projectDir = project.getProjectDir()
                                 .toPath();
        Path generatedDir = projectDir.resolve(GENERATED);
        return new GeneratedSourceRoot(generatedDir);
    }

    /**
     * Obtains the generated code directory which belongs to the source set with the given name.
     */
    public GeneratedSourceSet sourceSet(String name) {
        checkNotNull(name);
        Path path = path().resolve(name);
        return new GeneratedSourceSet(path);
    }

}
