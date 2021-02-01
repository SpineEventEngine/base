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

package io.spine.tools.gradle.project;

import io.spine.tools.gradle.GradlePlugin;
import io.spine.tools.gradle.PluginScript;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.function.Consumer;

/**
 * A target of Gradle plugin application.
 *
 * <p>Typically, represented by a Gradle {@link org.gradle.api.Project}.
 */
public interface PluginTarget {

    /**
     * Executes the given {@code action} if the given plugin is applied.
     *
     * <p>If the plugin is already applied, the action is executed at once. If the plugin is NOT
     * applied, the action is only executed when and it the plugin will be applied.
     *
     * @param plugin
     *         the trigger plugin
     * @param action
     *         the action to execute
     */
    <P extends Plugin<Project>> void with(GradlePlugin<P> plugin, Consumer<P> action);

    /**
     * Applies the given plugin.
     */
    void apply(GradlePlugin<?> plugin);

    /**
     * Applies the given plugin script.
     */
    void apply(PluginScript pluginScript);

    /**
     * Checks if the given plugin is already applied.
     */
    boolean isApplied(GradlePlugin<?> plugin);

    /**
     * Checks if the given plugin is not applied yet.
     */
    default boolean isNotApplied(GradlePlugin<?> plugin) {
        return !isApplied(plugin);
    }
}
