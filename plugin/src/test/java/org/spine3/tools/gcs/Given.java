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

package org.spine3.tools.gcs;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;

import static org.spine3.gradle.TaskName.BUILD;
import static org.spine3.gradle.TaskName.CLEAN_GCS;

/**
 * @author Dmytro Grankin
 */
@SuppressWarnings("UtilityClass")
public class Given {

    static final String GCS_PLUGIN_ID = "org.spine3.tools.gcs-plugin";
    private static final String KEY_FILE = "Required just for pass the check.";
    private static final String BUCKET_NAME = "test-bucket.com";
    private static final String CLEANING_FOLDER = "clean-me";

    private Given() {
        // Prevent instantiation of this utility class.
    }

    static Project newProject() {
        final Project project = ProjectBuilder.builder()
                                              .build();
        project.task(BUILD.getValue());
        return project;
    }

    static CleanGcsTask createCleanGcsTask(Project project) {
        final CleanGcsTask task = project.getTasks()
                                         .create(CLEAN_GCS.getValue(), CleanGcsTask.class);
        task.setKeyFile(KEY_FILE);
        task.setBucketName(BUCKET_NAME);
        task.setCleaningFolder(CLEANING_FOLDER);
        return task;
    }
}
