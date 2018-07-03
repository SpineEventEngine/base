/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.tools.protodoc.given;

import com.google.common.base.Joiner;
import io.spine.tools.gradle.GradleProject;
import io.spine.tools.protodoc.ProtoJavadocPlugin;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.collect.ImmutableList.of;
import static io.spine.tools.gradle.TaskName.FORMAT_PROTO_DOC;
import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

/**
 * A helper class for the {@link ProtoJavadocPlugin} testing.
 *
 * @author Dmytro Grankin
 */
public final class ProtoJavadocPluginTestEnv {

    /**
     * The {@code protoJavadoc.mainGenProtoDir} value from the plugin configuration.
     *
     * <p>This value is located in the test {@code build.gradle}.
     */
    private static final String MAIN_GEN_PROTO_LOCATION = "generated/main/java";

    /**
     * Prevents the utility class instantiation.
     */
    private ProtoJavadocPluginTestEnv() {
    }

    public static void formatAndAssert(String expectedContent, String contentToFormat,
                                TemporaryFolder folder) throws IOException {
        final Path formattedFilePath = format(contentToFormat, folder);
        final List<String> formattedLines = Files.readAllLines(formattedFilePath, UTF_8);
        final String mergedLines = Joiner.on(lineSeparator())
                                         .join(formattedLines);
        assertEquals(expectedContent, mergedLines);
    }

    private static Path format(String fileContent, TemporaryFolder folder) {
        String sourceFile = MAIN_GEN_PROTO_LOCATION + "/TestSource.java";

        executeTask(sourceFile, folder, fileContent);

        Path result = folder.getRoot()
                            .toPath()
                            .resolve(sourceFile);
        return result;
    }

    private static void executeTask(String filePath,
                                    TemporaryFolder folder,
                                    String fileContent) {
        GradleProject project = GradleProject
                .newBuilder()
                .setProjectName("proto-javadoc-test")
                .setProjectFolder(folder.getRoot())
                .createFile(filePath, of(fileContent))
                .build();
        project.executeTask(FORMAT_PROTO_DOC);
    }
}
