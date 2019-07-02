/*
 * Copyright 2019, TeamDev. All rights reserved.
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
package io.spine.code.properties;

import io.spine.repackaged.com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A utility class for writing to {@code .properties} file.
 */
public class PropertiesWriter {

    private final String folder;
    private final String fullPath;

    /**
     * Creates a new instance.
     *
     * @param folder   a folder in which the file is (or will be) located
     * @param fileName a name of the file to write to
     */
    public PropertiesWriter(String folder, String fileName) {
        this.folder = folder;
        this.fullPath = folder + File.separator + fileName;
    }

    /**
     * Updates the content of the file with the passed map.
     *
     * <p>If the file already exists, the passed entries will be merged with the file content.
     * If the file already has an key from the passed map, its value will <em>NOT</em>
     * be added, and warning will be logged.
     *
     * {@code .properties} file rewriting its contents if it already exists.
     *
     * @param map a map containing properties to write to the file
     */
    public void write(Map<String, String> map) {
        Logger log = log();
        log.debug("Preparing properties file {}", fullPath);
        File rootDir = new File(folder);
        createParentFolders(rootDir);

        Properties props = new SortedProperties();
        File file = new File(fullPath);
        prepareTargetFile(props, file);

        log.debug("Preparing properties (size: {})", map.size());
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (!props.containsKey(key)) {
                props.setProperty(key, value);
            } else {
                String currentValue = props.getProperty(key);
                if (!currentValue.equals(value)) {
                    log.warn("Entry with the key `{}` already exists. Value: `{}`." +
                                       " New value `{}` was not set.", key, currentValue, value);
                }
            }
        }
        log.debug("Preparing properties complete. Size is {}.", props.size());
        log.debug("Prepared properties: {}", props);

        try {
            log.debug("Writing properties file {}", fullPath);
            BufferedWriter bufferedWriter = Files.newWriter(file, UTF_8);
            props.store(bufferedWriter, /*comments=*/null);
            bufferedWriter.close();
            log.debug("Properties file written successfully");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void prepareTargetFile(Properties props, File file) {
        log().debug("Preparing the target file");
        if (file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                props.load(fis);
            } catch (@SuppressWarnings("OverlyBroadCatchBlock") IOException e) {
                String errMsg = "Error loading the properties from the file: ";
                throw new IllegalStateException(errMsg + file.getAbsolutePath(), e);
            }
        } else {
            createParentFolders(file);
        }
    }

    private static void createParentFolders(File file) {
        try {
            Files.createParentDirs(file);
        } catch (IOException e) {
            String errMsg = "Cannot create the parent folders at ";
            throw new IllegalStateException(errMsg + file.getAbsolutePath(), e);
        }
    }

    private Logger log() {
        return LoggerFactory.getLogger(getClass().getName());
    }
}
