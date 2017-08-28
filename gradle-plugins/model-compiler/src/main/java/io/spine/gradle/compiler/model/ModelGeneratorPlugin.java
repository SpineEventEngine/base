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

import io.spine.annotation.Experimental;
import io.spine.gradle.SpinePlugin;
import io.spine.tools.model.SpineModel;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static io.spine.gradle.TaskName.CLASSES;
import static io.spine.gradle.TaskName.COMPILE_JAVA;
import static io.spine.gradle.TaskName.GENERATE_MODEL;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.newInputStream;

/**
 * The plugin performing the Spine type model validation and generation.
 *
 * @author Dmytro Dashenkov
 */
@Experimental
public class ModelGeneratorPlugin extends SpinePlugin {

    private static final String RELATIVE_RAW_MODEL_PATH = ".spine/spine_model.ser";

    @Override
    public void apply(Project project) {
        final Path rawModelStorage = rawModelPath(project);
        // Ensure right environment (`main` scope sources with the `java` plugin)
        if (project.getTasks().findByPath(CLASSES.getValue()) != null) {
            newTask(GENERATE_MODEL, action(rawModelStorage)).insertBeforeTask(CLASSES)
                                                            .insertAfterAllTasks(COMPILE_JAVA)
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

    /**
     * Processes the {@link SpineModel} upon the {@linkplain Project Gradle project}.
     *
     * <p>Currently, performs only the model validation. The behavior may change in future.
     *
     * @param model   the Spine model to process
     * @param project the Gradle project to process the model upon
     */
    private static void processModel(SpineModel model, Project project) {
        final ProcessingStage validation = ProcessingStages.validate(project);
        validation.process(model);
    }

    /**
     * The action performing the model processing.
     *
     * <p>The action is executed only is the passed {@code rawModelPath} is present.
     *
     * <p>Reads the {@link SpineModel} from the given file and {@linkplain #processModel processes}
     * the model.
     */
    private static class GeneratorAction implements Action<Task> {

        private final Path rawModelPath;

        private GeneratorAction(Path rawModelPath) {
            this.rawModelPath = rawModelPath;
        }

        @Override
        public void execute(Task task) {
            if (!exists(rawModelPath)) {
                log().warn("No Spine model description found under {}. Completing the task.",
                           rawModelPath);
                return;
            }
            final SpineModel model;
            try (InputStream in = newInputStream(rawModelPath, StandardOpenOption.READ)) {
                model = SpineModel.parseFrom(in);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            processModel(model, task.getProject());
        }
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(ModelGeneratorPlugin.class);
    }
}
