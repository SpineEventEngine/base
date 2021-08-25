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

import com.google.errorprone.annotations.Immutable;
import io.spine.code.fs.AbstractDirectory;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.fs.DirectoryName.build;

/**
 * The root directory for build output.
 */
@Immutable
public final class BuildRoot extends AbstractDirectory {

    private BuildRoot(DefaultPaths module) {
        super(module.path()
                    .resolve(build.value()));
    }

    /**
     * Creates an instance under the specified project root directory.
     */
    static BuildRoot of(DefaultPaths project) {
        checkNotNull(project);
        return new BuildRoot(project);
    }

    /**
     * Obtains the instance referencing the {@code build/descriptors} directory.
     */
    public DescriptorsDir descriptors() {
        return new DescriptorsDir(this);
    }
}
