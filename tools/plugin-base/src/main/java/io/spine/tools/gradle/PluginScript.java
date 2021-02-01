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

package io.spine.tools.gradle;

import io.spine.io.Resource;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Gradle project plugin implemented in a {@code .gradle} script.
 *
 * <p>The script file lays in the Bootstrap plugin classpath. The Bootstrap plugin may
 * {@link #apply} this plugin to a project.
 */
public final class PluginScript implements Plugin<Project> {

    private final Resource resourceFile;

    private PluginScript(Resource resourceFile) {
        this.resourceFile = resourceFile;
    }

    public static PluginScript declaredIn(Resource resourceFile) {
        checkNotNull(resourceFile);
        return new PluginScript(resourceFile);
    }

    @Override
    public void apply(Project target) {
        target.apply(config -> config.from(resourceFile.locate()));
    }
}
