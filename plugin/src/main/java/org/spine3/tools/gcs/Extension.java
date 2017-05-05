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

/**
 * A configuration for the {@link GcsPlugin}.
 *
 * @author Dmytro Grankin
 */
public class Extension {

    private static final String GCS_PLUGIN_EXTENSION = "gcsPlugin";

    private String keyFileContent;
    private String bucket;
    private String cleaningFolder;
    private CleaningThreshold cleaningThreshold;

    static Extension createFor(Project project) {
        return project.getExtensions()
                      .create(GCS_PLUGIN_EXTENSION, Extension.class);
    }

    public String getKeyFileContent() {
        return keyFileContent;
    }

    public void setKeyFileContent(String keyFileContent) {
        this.keyFileContent = keyFileContent;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getCleaningFolder() {
        return cleaningFolder;
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

    public CleaningThreshold getCleaningThreshold() {
        return cleaningThreshold;
    }

    public void setCleaningThreshold(int days) {
        this.cleaningThreshold = new CleaningThreshold(days);
    }
}
