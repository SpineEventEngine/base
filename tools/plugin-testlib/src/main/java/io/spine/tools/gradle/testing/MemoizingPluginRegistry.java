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

package io.spine.tools.gradle.testing;

import com.google.common.collect.ImmutableSet;
import io.spine.tools.gradle.GradlePlugin;
import io.spine.tools.gradle.PluginScript;
import io.spine.tools.gradle.project.PluginTarget;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.Set;
import java.util.function.Consumer;

import static com.google.common.collect.Sets.newHashSet;

/**
 * A test implementation of {@link PluginTarget}.
 *
 * <p>Memoizes the applied plugins.
 */
public final class MemoizingPluginRegistry implements PluginTarget {

    private final Set<GradlePlugin<?>> plugins = newHashSet();
    private final Set<PluginScript> pluginScripts = newHashSet();

    @Override
    public void apply(GradlePlugin<?> plugin) {
        plugins.add(plugin);
    }

    @Override
    public void apply(PluginScript pluginScript) {
        pluginScripts.add(pluginScript);
    }

    @Override
    public boolean isApplied(GradlePlugin<?> plugin) {
        return plugins.contains(plugin);
    }

    public ImmutableSet<GradlePlugin<?>> plugins() {
        return ImmutableSet.copyOf(plugins);
    }

    public ImmutableSet<PluginScript> pluginScripts() {
        return ImmutableSet.copyOf(pluginScripts);
    }

    @Override
    public <P extends Plugin<Project>> void with(GradlePlugin<P> plugin, Consumer<P> action) {
        // Do nothing.
    }
}
