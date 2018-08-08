/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.tools.gradle.compiler.given;

import io.spine.tools.gradle.compiler.SpineCheckerPlugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

/**
 * This class contains various {@link SpineCheckerPlugin} descendants that emulate plugin behaviour
 * for different use-cases and allow to avoid any real artifact dependencies.
 *
 * @author Dmytro Kuzmin
 */
public class SpineCheckerPluginTestEnv {

    /** Prevents instantiation of this utility class. */
    private SpineCheckerPluginTestEnv() {
    }

    public static class ResolvingSpineCheckerPlugin extends SpineCheckerPlugin {

        @Override
        protected boolean
        isSpineCheckerVersionResolvable(String version, Configuration configuration) {
            return true;
        }

        @Override
        protected boolean hasErrorPronePlugin(Project project) {
            return true;
        }

        @Override
        protected void addConfigurePreprocessorAction(Configuration preprocessorConfig,
                                                      Project project) {
            // NO-OP.
        }
    }

    public static class NonResolvingSpineCheckerPlugin extends SpineCheckerPlugin {

        @Override
        protected boolean
        isSpineCheckerVersionResolvable(String version, Configuration configuration) {
            return false;
        }

        @Override
        protected boolean hasErrorPronePlugin(Project project) {
            return true;
        }


        @Override
        protected void addConfigurePreprocessorAction(Configuration preprocessorConfig,
                                                      Project project) {
            // NO-OP.
        }
    }

    public static class SpineCheckerPluginWithoutErrorProne extends SpineCheckerPlugin {

        @Override
        protected boolean
        isSpineCheckerVersionResolvable(String version, Configuration configuration) {
            return true;
        }

        @Override
        protected boolean hasErrorPronePlugin(Project project) {
            return false;
        }

        @Override
        protected void addConfigurePreprocessorAction(Configuration preprocessorConfig,
                                                      Project project) {
            // NO-OP.
        }
    }
}
