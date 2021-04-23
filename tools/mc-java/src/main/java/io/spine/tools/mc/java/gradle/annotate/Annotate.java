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

package io.spine.tools.mc.java.gradle.annotate;

import io.spine.tools.mc.java.code.annotation.ModuleAnnotator;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A task action which performs generated code annotation.
 */
final class Annotate implements Action<Task> {

    private final ProtoAnnotatorPlugin plugin;
    private final Project project;
    private final boolean productionTask;

    Annotate(ProtoAnnotatorPlugin plugin, Project project, boolean productionTask) {
        this.plugin = checkNotNull(plugin);
        this.project = checkNotNull(project);
        this.productionTask = productionTask;
    }

    @Override
    public void execute(Task task) {
        ModuleAnnotatorFactory factory = new ModuleAnnotatorFactory(project, productionTask);
        File descriptorSetFile = factory.descriptorSetFile();
        if (descriptorSetFile.exists()) {
            ModuleAnnotator annotator = factory.createAnnotator();
            annotator.annotate();
        } else {
            plugin.logMissingDescriptorSetFile(descriptorSetFile);
        }
    }
}
