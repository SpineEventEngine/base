/*
 * Copyright 2016, TeamDev Ltd. All rights reserved.
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
package io.spine.tools.protobufjavadoc;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * A utility that walks recursively through all files from project directory and
 * validates them depending on {@link JavadocFileFormatter} implementation passed to constructor.
 *
 * @author Alexander Aleksandrov
 */
public class FileChecker {

    private static final String DIRECTORY_TO_CHECK = "/generated/main/java";
    private final FileVisitor<Path> visitor;
    private final JavadocFileFormatter validator;

    public FileChecker(JavadocFileFormatter validator) {
        this.visitor = new RecursiveFileChecker(validator);
        this.validator = validator;
    }

    /**
     * Creates a gradle action task for this instance of file checker.
     *
     * @param project target project that should be checked
     * @return {@code Action<Task>} for gradle.
     */
    public Action<Task> actionFor(final Project project) {
        log().debug("Preparing an action for the {} validator", validator.getClass()
                                                                         .getCanonicalName());

        return new ValidatorAction(project);
    }

    private class ValidatorAction implements Action<Task> {

        private final Project project;

        private ValidatorAction(Project project) {
            this.project = project;
        }

        @Override
        public void execute(Task task) {
            log().debug("Finding the directory for project: {}.", project);
            final String projectDir = project.getProjectDir()
                                             .getAbsolutePath() + DIRECTORY_TO_CHECK;
            log().debug("Project directory: {}", projectDir);
            findCases(projectDir);
        }
    }

    private void findCases(String path) {
        final File file = new File(path);
        if (file.exists()) {
            checkRecursively(file.toPath());
        } else {
            log().debug("No more files left to format");
        }
    }

    private void checkRecursively(Path path) {
        try {
            log().debug("Starting to format the files recursively in {}", path.toString());
            Files.walkFileTree(path, visitor);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to validate the folder with its contents: " +
                                                    path, e);
        }
    }

    /**
     * Custom {@linkplain java.nio.file.FileVisitor visitor} which checks the contents of
     * the walked folder recursively.
     */
    private static class RecursiveFileChecker extends SimpleFileVisitor<Path> {

        private final JavadocFileFormatter validator;

        private RecursiveFileChecker(JavadocFileFormatter validator) {
            super();
            this.validator = validator;
        }

        @Override
        public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
            super.visitFile(path, attrs);
            log().debug("Performing validation for the file: {}", path);
            validator.format(path);
            return FileVisitResult.CONTINUE;
        }

        @SuppressWarnings("MethodDoesntCallSuperMethod")
        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            log().error("Error walking down the file tree for file: {}", file);
            return FileVisitResult.TERMINATE;
        }
    }

    private static Logger log() {
        return FileChecker.LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(FileChecker.class);
    }
}
