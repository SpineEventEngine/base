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
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentMatchers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.collect.Iterables.size;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.spine3.tools.gcs.Given.createCleanGcsTask;
import static org.spine3.tools.gcs.Given.newProject;

/**
 * @author Dmytro Grankin
 */
public class CleanGcsTaskShould {

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

        doReturn(DateTime.now()
                         .minusDays(1)).when(taskSpy)
                                       .getOldestBlobCreationDate(
                                               ArgumentMatchers.<Blob>anyIterable());
    }

    @Test
    public void delete_specified_folder_if_threshold_exceeded() {
        storage.create(BlobInfo.newBuilder(task.getBucketName(), task.getCleaningFolder() + 1)
                               .build());
        storage.create(BlobInfo.newBuilder(task.getBucketName(), task.getCleaningFolder() + 2)
                               .build());
        storage.create(BlobInfo.newBuilder(task.getBucketName(), "text.txt")
                               .build());
        taskSpy.setCleaningThreshold(0);
        taskSpy.cleanGcs();

        assertEquals(1, size(storage.list(task.getBucketName())
                                    .iterateAll()));
    }

    @Test
    public void not_delete_specified_folder_if_threshold_is_not_exceeded() {
        storage.create(BlobInfo.newBuilder(task.getBucketName(), task.getCleaningFolder())
                               .build());
        taskSpy.setCleaningThreshold(10);
        taskSpy.cleanGcs();
        assertEquals(1, size(storage.list(task.getBucketName())
                                    .iterateAll()));
    }

    @Test
    public void do_nothing_if_cleaningFolder_is_not_exists() {
        taskSpy.setCleaningThreshold(0);
        taskSpy.cleanGcs();
        verify(taskSpy, never()).getOldestBlobCreationDate(ArgumentMatchers.<Blob>anyIterable());
    }

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

    @Test
    public void properly_read_keyFile_content() throws IOException {
        final String keyFile = "keys.txt";
        final String keyFileContent = "Key file content.";

        final TemporaryFolder projectDir = new TemporaryFolder();
        projectDir.create();
        final Path keyFilePath = projectDir.getRoot()
                                           .toPath()
                                           .resolve(keyFile);
        final Project project = ProjectBuilder.builder()
                                              .withProjectDir(projectDir.getRoot())
                                              .build();

        Files.write(keyFilePath, keyFileContent.getBytes());
        final CleanGcsTask task = createCleanGcsTask(project);
        task.setKeyFile(keyFile);

        assertEquals(keyFileContent, task.getKeyFileContent());
    }
}
