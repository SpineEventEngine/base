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

import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobListOption;
import com.google.cloud.storage.Storage.BucketGetOption;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.Ordering;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.spine3.gradle.SpinePlugin;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;
import static org.spine3.gradle.TaskName.BUILD;
import static org.spine3.gradle.TaskName.CLEAN_GCS;

/**
 * The plugin for Google Cloud Storage, that cleans the folder with the specified period.
 *
 * @author Dmytro Grankin
 */
public class GcsPlugin extends SpinePlugin {

    private static final String PROJECT_ID_KEY = "project_id";

    private Extension extension;

    @Override
    public void apply(Project project) {
        log().debug("Applying the GCS plugin");
        extension = Extension.createFor(project);

        final Action<Task> cleanGcsAction = new Action<Task>() {
            @Override
            public void execute(Task task) {
                cleanGcs();
            }
        };
        final GradleTask task = newTask(CLEAN_GCS, cleanGcsAction).insertBeforeTask(BUILD)
                                                                  .applyNowTo(project);
        log().debug("GCS Gradle plugin initialized with the Gradle task: {}", task);
    }

    private void cleanGcs() {
        final Storage storage = getStorage();
        final Bucket bucket = storage.get(extension.getBucket(), BucketGetOption.fields());

        if (bucket == null) {
            throw new IllegalStateException("Specified bucket was not found.");
        }

        final Page<Blob> blobs = bucket.list(BlobListOption.prefix(extension.getCleaningFolder()));
        if (!blobs.iterateAll()
                  .iterator()
                  .hasNext()) {
            log().info("Folder `{}` is empty. Nothing to clean.", extension.getCleaningFolder());
            return;
        }

        final Date lastCleaningDate = new Date(getMinCreationDate(blobs));
        final Date nextCleaningDate =
                new Date(lastCleaningDate.getTime() + extension.getCleaningInternal()
                                                               .toMillis());
        final Date now = new Date();
        log().debug("Last cleaning date is `{}`.", lastCleaningDate);
        log().debug("Next cleaning date is `{}`.", nextCleaningDate);

        final boolean isCleaningRequired = nextCleaningDate.before(now);
        if (isCleaningRequired) {
            for (Blob blob : blobs.iterateAll()) {
                storage.delete(blob.getBlobId());
            }
            log().info("Folder `{}` in bucket `{}` cleaned.",
                       extension.getCleaningFolder(), extension.getBucket());
        } else {
            log().info("Cleaning is not required yet.");
        }
    }

    private Storage getStorage() {
        final byte[] keyFileBytes = extension.getKeyFileContent()
                                             .getBytes();
        final InputStream serviceAccountFile = new ByteArrayInputStream(keyFileBytes);
        final ServiceAccountCredentials credentials;

        try {
            credentials = ServiceAccountCredentials.fromStream(serviceAccountFile);
        } catch (IOException e) {
            throw new IllegalStateException("Invalid key file content was specified.", e);
        }

        final JSONObject json = new JSONObject(extension.getKeyFileContent());
        final String projectId = (String) json.get(PROJECT_ID_KEY);
        return StorageOptions.newBuilder()
                             .setProjectId(projectId)
                             .setCredentials(credentials)
                             .build()
                             .getService();
    }

    private static long getMinCreationDate(Page<Blob> blobs) {
        final Ordering<Blob> creationDateOrdering = new Ordering<Blob>() {
            @Override
            public int compare(@Nullable Blob left, @Nullable Blob right) {
                checkNotNull(left);
                checkNotNull(right);
                return left.getCreateTime()
                           .compareTo(right.getCreateTime());
            }
        };

        final Blob oldestBlob = creationDateOrdering.min(blobs.iterateAll());
        return oldestBlob.getCreateTime();
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = getLogger(GcsPlugin.class);
    }
}
