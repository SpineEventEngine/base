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
import groovy.time.Duration;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static org.joda.time.DateTime.now;

/**
 * The task, that deletes objects from Google Cloud Storage
 * within {@link #bucketName} and {@link #targetFolder}.
 *
 * An object will be deleted if its age exceeds {@link #threshold}.
 *
 * <p>Configuration example:
 * <pre>{@code
 * cleanGCS {
 *      keyFile = "gcs-key-file.json"
 *      bucketName = "example.com"
 *      targetFolder = "trash"
 *      threshold = TimeCategory.getDays(30)
 * }
 * }</pre>
 *
 * <p>All task parameters are mandatory.
 *
 * @author Dmytro Grankin
 */
public class CleanGcsTask extends DefaultTask {

    /**
     * A key to retrieve a project ID from {@link #keyFile}.
     */
    private static final String PROJECT_ID_KEY = "project_id";

    /**
     * A delimiter for "folders" used in Google Cloud Storage.
     *
     * @see <a href="https://cloud.google.com/storage/docs/naming">Object name considerations</a>
     */
    static final String FOLDER_DELIMITER = "/";

    /**
     * A relative path to a file that contains service account credentials in JSON format.
     *
     * <p>Path should starts from a project root.
     */
    private String keyFile;

    /**
     * A name of bucket containing {@link #targetFolder}.
     */
    private String bucketName;

    /**
     * A name of folder, within which will be cleaning.
     *
     * <p>Essentially it's a prefix for objects. If an object starts with a prefix and
     * {@link #threshold} exceeded, then an object will be deleted.
     */
    private String targetFolder;

    /**
     * A cleaning threshold.
     *
     * <p>If an object age exceeds a threshold, it will be deleted.
     */
    private Duration threshold;

    @TaskAction
    void cleanGcs() {
        checkParameters();
        final Storage storage = getStorage();
        checkBucketExists(storage);

        final Page<Blob> blobs = storage.list(bucketName, BlobListOption.prefix(targetFolder));
        for (Blob blob : blobs.iterateAll()) {
            final long cleaningTrigger = getCreateTime(blob) + threshold.toMilliseconds();
            final boolean isCleaningRequired = now().isAfter(cleaningTrigger);
            if (isCleaningRequired) {
                storage.delete(blob.getBlobId());
                log().info("Object `{}` deleted from bucket `{}`.", blob.getName(), bucketName);
            }
        }
    }

    /**
     * Returns {@link Blob#getCreateTime()} result.
     *
     * <p>Needed exclusively for mocking in test purposes.
     *
     * @param blob the blob
     * @return the creation time of the blob
     */
    @VisibleForTesting
    // Needed to for test needs
    long getCreateTime(Blob blob) {
        return blob.getCreateTime();
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
        checkNotNull(threshold, "`threshold` should be set.");
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
     * <p>If the specified folder not ends with {@link #FOLDER_DELIMITER}, it will be appended.
     *
     * @param targetFolder the target folder
     */
    public void setTargetFolder(String targetFolder) {
        this.targetFolder = targetFolder.endsWith(FOLDER_DELIMITER)
                            ? targetFolder
                            : targetFolder + FOLDER_DELIMITER;
    }

    public void setThreshold(Duration duration) {
        this.threshold = duration;
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

    public Duration getThreshold() {
        return threshold;
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(CleanGcsTask.class);
    }
}
