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
package org.spine3.gradle.reflections;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import org.gradle.api.Project;

import static java.io.File.separator;

/**
 * A configuration for the {@link ReflectionsPlugin}.
 *
 * @author Alex Tymchenko
 */
@SuppressWarnings("PublicField")    // as this is a Gradle extension.
public class Extension {

    static final String REFLECTIONS_PLUGIN_EXTENSION = "reflectionsPlugin";

    /**
     * The absolute path to the target directory which contains generated `.xml` file
     * with reflections.
     */
    public String targetDir;

    static String getTargetDir(Project project) {
        final String path = reflectionsPlugin(project).targetDir;
        if (path == null || path.isEmpty()) {
            return project.getProjectDir() + separator +
                    Joiner.on(separator)
                          .join(new String[]{"src", "generated", "resources", "META-INF",
                                  "reflections"});
        } else {
            return path;
        }
    }

    @VisibleForTesting      // it should have been `private`.
    static Extension reflectionsPlugin(Project project) {
        return (Extension) project.getExtensions()
                                  .getByName(REFLECTIONS_PLUGIN_EXTENSION);
    }

}
