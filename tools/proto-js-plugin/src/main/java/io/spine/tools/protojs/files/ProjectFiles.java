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

import org.gradle.api.Project;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.spine.code.proto.FileDescriptors.KNOWN_TYPES;

@SuppressWarnings("DuplicateStringLiteralInspection") // Random duplication of path elements.
public final class ProjectFiles {

    private static final String MAIN = "main";
    private static final String TEST = "test";

    private ProjectFiles() {
    }

    public static Path mainProtoJsLocation(Project project) {
        Path location = protoJsLocation(project, MAIN);
        return location;
    }

    public static Path testProtoJsLocation(Project project) {
        Path location = protoJsLocation(project, TEST);
        return location;
    }

    public static File mainDescriptorSetFile(Project project) {
        File file = descriptorSetFile(project, MAIN);
        return file;
    }

    public static File testDescriptorSetFile(Project project) {
        File file = descriptorSetFile(project, TEST);
        return file;
    }

    private static Path protoJsLocation(Project project, String sourceSet) {
        File projectDir = project.getProjectDir();
        String absolutePath = projectDir.getAbsolutePath();
        return Paths.get(absolutePath, "proto", sourceSet, "js");
    }

    private static File descriptorSetFile(Project project, String sourceSet) {
        File projectDir = project.getProjectDir();
        String absolutePath = projectDir.getAbsolutePath();
        Path path = Paths.get(absolutePath, "build", "descriptors", sourceSet, KNOWN_TYPES);
        File file = path.toFile();
        return file;
    }
}
