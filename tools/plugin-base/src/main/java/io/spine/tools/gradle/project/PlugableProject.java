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

package io.spine.tools.gradle.project;

import io.spine.logging.Logging;
import io.spine.tools.gradle.GradlePlugin;
import io.spine.tools.gradle.PluginScript;
import org.gradle.api.Project;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.plugins.PluginManager;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An implementation of {@link PluginTarget} based on a Gradle {@link Project}.
 */
public final class PlugableProject implements PluginTarget, Logging {

    private final Project project;

    public PlugableProject(Project project) {
        this.project = checkNotNull(project);
    }

    @Override
    public void apply(GradlePlugin plugin) {
        checkNotNull(plugin);

        if (isNotApplied(plugin)) {
            PluginManager pluginManager = project.getPluginManager();
            pluginManager.apply(plugin.implementationClass());
        } else {
            _debug().log("Plugin `%s` is already applied.", plugin.className());
        }
    }

    @Override
    public void apply(PluginScript pluginScript) {
        pluginScript.apply(project);
    }

    @Override
    public boolean isApplied(GradlePlugin plugin) {
        checkNotNull(plugin);
        PluginContainer plugins = project.getPlugins();
        boolean result = plugins.hasPlugin(plugin.implementationClass());
        return result;
    }
}
