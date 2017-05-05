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
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.Ordering;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The task, that deletes the specified folder if it's age exceeds {@link CleaningThreshold}.
 *
 * @author Dmytro Grankin
 */
public class CleanGcsTask extends DefaultTask {

    private static final String PROJECT_ID_KEY = "project_id";

    private String keyFileContent;
    private String bucketName;
    private String cleaningFolder;
    private CleaningThreshold cleaningThreshold;

    @TaskAction
    private void cleanGcs() {
        checkParameters();
        final Storage storage = getStorage();
        final Bucket bucket = storage.get(bucketName);

        if (bucket == null) {
            throw new IllegalStateException("Specified bucketName is not exists.");
        }

        final Page<Blob> blobs = bucket.list(BlobListOption.prefix(cleaningFolder));
        final Iterable<Blob> allBlobs = blobs.iterateAll();
        if (!allBlobs.iterator()
                     .hasNext()) {
            log().info("Folder `{}` is not exists. Nothing to clean.", cleaningFolder);
            return;
        }

        final Blob oldestBlob = getOldestBlob(allBlobs);
        final DateTime oldestBlobCreation = new DateTime(oldestBlob.getCreateTime());
        final DateTime cleaningTrigger = oldestBlobCreation.plus(cleaningThreshold.toMillis());
        final boolean isCleaningRequired = cleaningTrigger.isBeforeNow();
        if (isCleaningRequired) {
            for (Blob blob : allBlobs) {
                storage.delete(blob.getBlobId());
            }
            log().info("Folder `{}` in bucketName `{}` deleted.", cleaningFolder, bucketName);
        } else {
            log().info("Cleaning is not required yet and will be triggered after {}.",
                       cleaningTrigger);
        }
    }

    private Storage getStorage() {
        final byte[] keyFileBytes = keyFileContent.getBytes();
        final InputStream serviceAccountFile = new ByteArrayInputStream(keyFileBytes);
        final ServiceAccountCredentials credentials;

        try {
            credentials = ServiceAccountCredentials.fromStream(serviceAccountFile);
        } catch (IOException e) {
            throw new IllegalStateException("Invalid key file content was specified.", e);
        }

        final JSONObject json = new JSONObject(keyFileContent);
        final String projectId = (String) json.get(PROJECT_ID_KEY);
        return StorageOptions.newBuilder()
                             .setProjectId(projectId)
                             .setCredentials(credentials)
                             .build()
                             .getService();
    }

    private static Blob getOldestBlob(Iterable<Blob> blobs) {
        checkArgument(blobs.iterator()
                           .hasNext());
        final Ordering<Blob> creationDateOrdering = new Ordering<Blob>() {
            @Override
            public int compare(@Nullable Blob left, @Nullable Blob right) {
                checkNotNull(left);
                checkNotNull(right);
                return left.getCreateTime()
                           .compareTo(right.getCreateTime());
            }
        };

        return creationDateOrdering.min(blobs);
    }

    private void checkParameters() {
        checkNotNull(keyFileContent, "`keyFileContent` should be set.");
        checkNotNull(bucketName,"`bucketName` should be set.");
        checkNotNull(cleaningFolder, "`cleaningFolder` should be set.");
        checkNotNull(cleaningThreshold, "`cleaningThreshold` should be set.");
    }

    public void setKeyFileContent(String keyFileContent) {
        this.keyFileContent = keyFileContent;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    /**
     * Sets the cleaning folder.
     *
     * <p>If the specified cleaning folder not ends with a slash, it will be appended.
     *
     * @param cleaningFolder the cleaning folder
     */
    public void setCleaningFolder(String cleaningFolder) {
        this.cleaningFolder = cleaningFolder.endsWith("/")
                              ? cleaningFolder
                              : cleaningFolder + '/';
    }

    public void setCleaningThreshold(int days) {
        this.cleaningThreshold = new CleaningThreshold(days);
    }

    public String getKeyFileContent() {
        return keyFileContent;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getCleaningFolder() {
        return cleaningFolder;
    }

    public CleaningThreshold getCleaningThreshold() {
        return cleaningThreshold;
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = org.slf4j.LoggerFactory.getLogger(CleanGcsTask.class);
    }
}
