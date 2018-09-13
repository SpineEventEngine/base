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

package io.spine.tools.protojs.files;

import com.google.common.annotations.VisibleForTesting;
import io.spine.code.DefaultProject;
import org.gradle.api.Project;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The utility for working with Gradle {@link Project} file paths.
 *
 * @author Dmytro Kuzmin
 */
@SuppressWarnings("DuplicateStringLiteralInspection") // Random duplication of the path elements.
public final class ProjectFiles {

    /**
     * Main source set name.
     */
    private static final String MAIN = "main";

    /**
     * Test source set name.
     */
    private static final String TEST = "test";

    /** Prevents the instantiation of this utility class. */
    private ProjectFiles() {
    }

    /**
     * Obtains the root location of the generated JS protos from the "main" source set.
     *
     * @param project
     *         the project for which to get the location
     * @return the location of the "main" JS proto files
     */
    public static Path mainProtoJsLocation(Project project) {
        checkNotNull(project);
        Path location = protoJsLocation(project, MAIN);
        return location;
    }

    /**
     * Obtains the root location of the generated JS protos from the "main" source set.
     *
     * @param projectDir
     *         the project directory used as a root for path calculation
     * @return the location of the "main" JS proto files
     */
    @VisibleForTesting
    public static Path mainProtoJsLocation(File projectDir) {
        checkNotNull(projectDir);
        Path location = protoJsLocation(projectDir, MAIN);
        return location;
    }

    /**
     * Obtains the root location of the generated JS protos from the "test" source set.
     *
     * @param project
     *         the project for which to get the location
     * @return the location of the "test" JS proto files
     */
    public static Path testProtoJsLocation(Project project) {
        checkNotNull(project);
        Path location = protoJsLocation(project, TEST);
        return location;
    }

    /**
     * Obtains the "main" descriptor set file for the project.
     *
     * @param project
     *         the project for which to obtain the file
     * @return the Java {@code File} object representing the main descriptor set file
     */
    public static File mainDescriptorSetFile(Project project) {
        checkNotNull(project);
        DefaultProject defaultProject = DefaultProject.at(project.getProjectDir());
        File file = defaultProject.mainDescriptors();
        return file;
    }

    /**
     * Obtains the "main" descriptor set file for the project directory.
     *
     * @param projectDir
     *         the project directory used as a path calculation root
     * @return the Java {@code File} object representing the main descriptor set file
     */
    @VisibleForTesting
    public static File mainDescriptorSetFile(File projectDir) {
        checkNotNull(projectDir);
        DefaultProject defaultProject = DefaultProject.at(projectDir);
        File file = defaultProject.mainDescriptors();
        return file;
    }

    /**
     * Obtains the "test" descriptor set file for the project.
     *
     * @param project
     *         the project for which to obtain the file
     * @return the Java {@code File} object representing the main descriptor set file
     */
    public static File testDescriptorSetFile(Project project) {
        checkNotNull(project);
        DefaultProject defaultProject = DefaultProject.at(project.getProjectDir());
        File file = defaultProject.testDescriptors();
        return file;
    }

    private static Path protoJsLocation(Project project, String sourceSet) {
        File projectDir = project.getProjectDir();
        return protoJsLocation(projectDir, sourceSet);
    }

    private static Path protoJsLocation(File projectDir, String sourceSet) {
        String absolutePath = projectDir.getAbsolutePath();
        Path location = Paths.get(absolutePath, "proto", sourceSet, "js");
        return location;
    }
}
