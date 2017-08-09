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

package io.spine.gradle.compiler.protoc;

import com.google.common.io.Files;
import io.spine.gradle.SpinePlugin;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.AppliedPlugin;
import org.gradle.api.plugins.ExtensionContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.google.common.collect.ImmutableMap.of;

/**
 * @author Dmytro Dashenkov
 */
public class ProtocPluginImporter extends SpinePlugin {

    private static final String PROTOC_CONFIG_FILE_NAME = "protoc_config.gradle";

    @SuppressWarnings("DuplicateStringLiteralInspection") // The same string has different semantics
    private static final String PROTOBUF_PLUGIN_ID = "com.google.protobuf";

    @Override
    public void apply(final Project project) {
        // TODO:2017-08-09:dmytro.dashenkov: Add artifact extension.
        final File tempFolder = Files.createTempDir();
        final File configFile = new File(tempFolder, PROTOC_CONFIG_FILE_NAME);
        try (InputStream in = getClass().getClassLoader()
                                        .getResourceAsStream(PROTOC_CONFIG_FILE_NAME);
             FileOutputStream out = new FileOutputStream(configFile)) {
            int readByte = in.read();
            while (readByte >= 0) {
                out.write(readByte);
                readByte = in.read();
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        project.getPluginManager().withPlugin(PROTOBUF_PLUGIN_ID, new Action<AppliedPlugin>() {
            @Override
            public void execute(AppliedPlugin appliedPlugin) {
                log().debug("Applying protoc_config.gradle ({})", configFile.getAbsolutePath());
                project.apply(of("from", configFile.getAbsolutePath()));
                log().debug("Applied protoc_config.gradle");
            }
        });
    }

    private static void addExtension(Project project, String key, Object value) {
        final ExtensionContainer ext = project.getExtensions();
        if (ext.findByName(key) == null) {
            project.getExtensions()
                   .add(key, value);
        }
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(ProtocPluginImporter.class);
    }
}
