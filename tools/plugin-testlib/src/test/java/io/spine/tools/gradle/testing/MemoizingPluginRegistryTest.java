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

package io.spine.tools.gradle.testing;

import io.spine.io.Resource;
import io.spine.tools.gradle.GradlePlugin;
import io.spine.tools.gradle.PluginScript;
import org.gradle.api.plugins.JavaPlugin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class MemoizingPluginRegistryTest {

    @Test
    @DisplayName("memoize the given plugin")
    void memoizePlugin() {
        GradlePlugin plugin = GradlePlugin.implementedIn(JavaPlugin.class);
        MemoizingPluginRegistry registry = new MemoizingPluginRegistry();
        registry.apply(plugin);
        assertThat(registry.isApplied(plugin)).isTrue();
        assertThat(registry.plugins()).containsExactly(plugin);
        assertThat(registry.pluginScripts()).isEmpty();
    }

    @Test
    @DisplayName("memoize the given plugin script")
    void memoizePluginScript() {
        PluginScript script = PluginScript.declaredIn(Resource.file(BuildGradle.FILE_NAME));
        MemoizingPluginRegistry registry = new MemoizingPluginRegistry();
        registry.apply(script);
        assertThat(registry.pluginScripts()).containsExactly(script);
        assertThat(registry.plugins()).isEmpty();
    }
}
