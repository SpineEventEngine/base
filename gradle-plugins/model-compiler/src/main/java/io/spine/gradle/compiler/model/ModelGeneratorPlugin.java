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

package io.spine.gradle.compiler.model;

import io.spine.gradle.SpinePlugin;
import io.spine.tools.model.SpineModel;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static io.spine.gradle.TaskName.CLASSES;
import static io.spine.gradle.TaskName.COMPILE_JAVA;
import static io.spine.gradle.TaskName.GENERATE_MODEL;

/**
 * @author Dmytro Dashenkov
 */
public class ModelGeneratorPlugin extends SpinePlugin {

    private static final String RELATIVE_RAW_MODEL_PATH = ".spine/spine_model.ser";

    @Override
    public void apply(Project project) {
        final Path rawModelStorage = rawModelPath(project);
        if (project.getTasks().findByPath(CLASSES.getValue()) != null) {
            newTask(GENERATE_MODEL, action(rawModelStorage)).insertBeforeTask(CLASSES)
                                                            .insertAfterTask(COMPILE_JAVA)
                                                            .withInputFiles(rawModelStorage)
                                                            .applyNowTo(project);
        }
    }

    private static Path rawModelPath(Project project) {
        final Path rootDir = project.getRootDir().toPath();
        final Path result = rootDir.resolve(RELATIVE_RAW_MODEL_PATH);
        return result;
    }

    private static Action<Task> action(Path path) {
        return new GeneratorAction(path);
    }

    private static void processModel(SpineModel model) {
        final ProcessingStage validation = ProcessingStages.validate();
        validation.process(model);
    }

    private static class GeneratorAction implements Action<Task> {

        private final Path rawModelPath;

        private GeneratorAction(Path rawModelPath) {
            this.rawModelPath = rawModelPath;
        }

        @Override
        public void execute(Task task) {
            final SpineModel model;
            try (InputStream in = Files.newInputStream(rawModelPath, StandardOpenOption.READ)) {
                model = SpineModel.parseFrom(in);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            processModel(model);
        }
    }
}
