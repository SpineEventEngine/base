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

package io.spine.dart.generate;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

public final class Extension {

    static final String NAME = "protoDart";

    private final RegularFileProperty mainDescriptorSet;
    private final DirectoryProperty destinationDir;
    private final Property<String> packageName;

    Extension(Project project) {
        ObjectFactory objects = project.getObjects();
        this.mainDescriptorSet = objects.fileProperty();
        mainDescriptorSet.convention(project.getLayout()
                                            .getBuildDirectory()
                                            .file("descriptors/main.desc"));
        this.destinationDir = objects.directoryProperty();
        destinationDir.convention(project.getLayout()
                                         .getProjectDirectory()
                                         .dir("lib"));
        this.packageName = objects.property(String.class);
        packageName.convention(project.getProjectDir()
                                      .getName());
    }

    public RegularFileProperty getMainDescriptorSet() {
        return mainDescriptorSet;
    }

    public DirectoryProperty getDestinationDir() {
        return destinationDir;
    }

    public Property<String> getPackageName() {
        return packageName;
    }
}
