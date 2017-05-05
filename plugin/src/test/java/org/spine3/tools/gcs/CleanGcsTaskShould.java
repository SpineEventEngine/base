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
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.spine3.gradle.TaskName.CLEAN_GCS;
import static org.spine3.tools.gcs.Given.newProject;

/**
 * @author Dmytro Grankin
 */
public class CleanGcsTaskShould {

    private final Project project = newProject();
    private final CleanGcsTask task = project.getTasks()
                                             .create(CLEAN_GCS.getValue(), CleanGcsTask.class);

    @Test
    public void append_slash_to_folder_name_without_trailing_slash() {
        final String folderName = "just-folder-name";
        task.setCleaningFolder(folderName);
        assertEquals(folderName + '/', task.getCleaningFolder());
    }

    @Test
    public void not_append_slash_to_folder_name_with_trailing_slash() {
        final String folderName = "slash-in-the-end/";
        task.setCleaningFolder(folderName);
        assertEquals(folderName, task.getCleaningFolder());
    }
}
