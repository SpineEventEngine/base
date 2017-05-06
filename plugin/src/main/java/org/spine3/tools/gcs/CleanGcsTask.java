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
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Ordering;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * The task, that deletes the specified folder if it's age exceeds {@link CleaningThreshold}.
 *
 * <p>To use the task, all parameters must be specified.
 *
 * @author Dmytro Grankin
 */
public class CleanGcsTask extends DefaultTask {

    private static final String PROJECT_ID_KEY = "project_id";

    /**
     * File that contains service account credentials in JSON format.
     */
    private String keyFile;
    private String bucketName;
    private String targetFolder;
    private CleaningThreshold cleaningThreshold;

    @TaskAction
    void cleanGcs() {
        checkParameters();
        final Storage storage = getStorage();
        checkBucketExists(storage);

        final Page<Blob> blobs = storage.list(bucketName, BlobListOption.prefix(targetFolder));
        final Iterable<Blob> allBlobs = blobs.iterateAll();
        if (!allBlobs.iterator()
                     .hasNext()) {
            log().info("Folder `{}` is not exists. Nothing to clean.", targetFolder);
            return;
        }

        final DateTime oldestBlobCreation = getOldestBlobCreationDate(allBlobs);
        final DateTime cleaningTrigger = oldestBlobCreation.plus(cleaningThreshold.toMillis());
        final boolean isCleaningRequired = cleaningTrigger.isBeforeNow();
        if (isCleaningRequired) {
            for (Blob blob : allBlobs) {
                storage.delete(blob.getBlobId());
            }
            log().info("Folder `{}` in bucketName `{}` deleted.", targetFolder, bucketName);
        } else {
            log().info("Cleaning is not required until {}.", cleaningTrigger);
        }
    }

    @VisibleForTesting
    void checkBucketExists(Storage storage) {
        final Bucket bucket = storage.get(bucketName);
        if (bucket == null) {
            final String msg = format("Bucket `%s` is not exists.", bucketName);
            throw new IllegalStateException(msg);
        }
    }

    @VisibleForTesting
    Storage getStorage() {
        final String keyFileContent = getKeyFileContent();
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

    @VisibleForTesting
    DateTime getOldestBlobCreationDate(Iterable<Blob> blobs) {
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

        final Blob oldestBlob = creationDateOrdering.min(blobs);
        return new DateTime(oldestBlob.getCreateTime());
    }

    @VisibleForTesting
    String getKeyFileContent() {
        final File file = getProject().file(keyFile);
        final Path keyFilePath = Paths.get(file.getAbsolutePath());
        final byte[] keyFileBytes;
        try {
            keyFileBytes = Files.readAllBytes(keyFilePath);
            return new String(keyFileBytes);
        } catch (IOException e) {
            final String msg = format("Unable to read key file `%s`.", keyFile);
            throw new IllegalStateException(msg, e);
        }
    }

    private void checkParameters() {
        checkNotNull(keyFile, "`keyFile` should be set.");
        checkNotNull(bucketName, "`bucketName` should be set.");
        checkNotNull(targetFolder, "`targetFolder` should be set.");
        checkNotNull(cleaningThreshold, "`cleaningThreshold` should be set.");
    }

    public void setKeyFile(String keyFile) {
        this.keyFile = keyFile;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    /**
     * Sets the target folder.
     *
     * <p>If the specified folder not ends with a slash, it will be appended.
     *
     * @param targetFolder the target folder
     */
    public void setTargetFolder(String targetFolder) {
        this.targetFolder = targetFolder.endsWith("/")
                              ? targetFolder
                              : targetFolder + '/';
    }

    public void setCleaningThreshold(int days) {
        this.cleaningThreshold = new CleaningThreshold(days);
    }

    public String getKeyFile() {
        return keyFile;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getTargetFolder() {
        return targetFolder;
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
