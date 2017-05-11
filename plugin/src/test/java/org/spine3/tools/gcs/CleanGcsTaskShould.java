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

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.contrib.nio.testing.LocalStorageHelper;
import groovy.time.Duration;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.collect.Iterables.size;
import static groovy.time.TimeCategory.getDays;
import static groovy.time.TimeCategory.getMilliseconds;
import static org.joda.time.DateTime.now;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.spine3.tools.gcs.CleanGcsTask.FOLDER_DELIMITER;
import static org.spine3.tools.gcs.Given.createCleanGcsTask;
import static org.spine3.tools.gcs.Given.newProject;

/**
 * @author Dmytro Grankin
 */
public class CleanGcsTaskShould {

    private static final DateTime BLOB_CREATION_TIME = now();

    private final Project project = newProject();
    private final CleanGcsTask task = createCleanGcsTask(project);
    private final CleanGcsTask taskSpy = spy(task);
    private final Storage storage = LocalStorageHelper.getOptions()
                                                      .getService();

    @Before
    public void setUp() throws Exception {
        doReturn(storage).when(taskSpy)
                         .getStorage();

        // FakeStorageRpc does not support bucket creation.
        doNothing().when(taskSpy)
                   .checkBucketExists(storage);

        doReturn(BLOB_CREATION_TIME.getMillis()).when(taskSpy)
                                                .getCreateTime(any(Blob.class));
    }

    @Test
    public void delete_object_if_its_age_exceeds_threshold() {
        final BlobInfo blobInfo = BlobInfo.newBuilder(task.getBucketName(), task.getTargetFolder())
                                          .build();
        storage.create(blobInfo);

        final Duration threshold = getMilliseconds(0);
        taskSpy.setThreshold(threshold);
        taskSpy.cleanGcs();

        assertEquals(0, size(storage.list(task.getBucketName())
                                    .iterateAll()));
    }

    @Test
    public void not_delete_object_if_its_age_not_exceeds_threshold() {
        final BlobInfo blobInfo = BlobInfo.newBuilder(task.getBucketName(), task.getTargetFolder())
                                          .build();
        storage.create(blobInfo);

        final Duration threshold = getDays(1);
        taskSpy.setThreshold(threshold);
        taskSpy.cleanGcs();

        assertEquals(1, size(storage.list(task.getBucketName())
                                    .iterateAll()));
    }

    @Test
    public void not_delete_object_from_non_target_folder() {
        final String nonTargetFolder = "non-target-folder";
        final BlobInfo blobInfo = BlobInfo.newBuilder(task.getBucketName(), nonTargetFolder)
                                          .build();
        storage.create(blobInfo);

        final Duration threshold = getMilliseconds(0);
        taskSpy.setThreshold(threshold);
        taskSpy.cleanGcs();

        assertEquals(1, size(storage.list(task.getBucketName())
                                    .iterateAll()));
    }

    @Test
    public void append_delimiter_to_folder_name_without_trailing_delimiter() {
        final String folderName = "just-folder-name";
        task.setTargetFolder(folderName);
        assertEquals(folderName + FOLDER_DELIMITER, task.getTargetFolder());
    }

    @Test
    public void not_append_delimiter_to_folder_name_with_trailing_delimiter() {
        final String folderName = "delimiter-at-the-end" + FOLDER_DELIMITER;
        task.setTargetFolder(folderName);
        assertEquals(folderName, task.getTargetFolder());
    }

    @Test
    public void properly_read_keyFile_content() throws IOException {
        final String authKeyPath = "keys.txt";
        final String authKeyContent = "Key file content.";

        final TemporaryFolder projectDir = new TemporaryFolder();
        projectDir.create();
        final Path keyFilePath = projectDir.getRoot()
                                           .toPath()
                                           .resolve(authKeyPath);
        final Project project = ProjectBuilder.builder()
                                              .withProjectDir(projectDir.getRoot())
                                              .build();

        Files.write(keyFilePath, authKeyContent.getBytes());
        final CleanGcsTask task = createCleanGcsTask(project);
        task.setAuthKeyPath(authKeyPath);

        assertEquals(authKeyContent, task.getKeyFileContent());
    }
}
