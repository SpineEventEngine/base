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
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import java.io.File;

public final class Extension {

    static final String NAME = "protoDart";

    private final RegularFileProperty descriptorSet;
    private final RegularFileProperty typeRegistry;
    private final Property<String> packageName;

    Extension(Project project) {
        ObjectFactory objects = project.getObjects();
        this.descriptorSet = objects.fileProperty();
        descriptorSet.convention(project.getLayout()
                                        .getBuildDirectory()
                                        .file("descriptors/main.desc"));
        this.typeRegistry = objects.fileProperty();
        typeRegistry.convention(project.getLayout()
                                       .getProjectDirectory()
                                       .file("lib/types.dart"));
        this.packageName = objects.property(String.class);
        packageName.convention(project.getProjectDir()
                                      .getName());
    }

    public RegularFileProperty getDescriptorSet() {
        return descriptorSet;
    }

    File descriptorSetFile() {
        return getDescriptorSet().get().getAsFile();
    }

    public RegularFileProperty getTypeRegistry() {
        return typeRegistry;
    }

    File typeRegistryFile() {
        return getTypeRegistry().get().getAsFile();
    }

    public Property<String> getPackageName() {
        return packageName;
    }

    String packageName() {
        return getPackageName().get();
    }
}
