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

package io.spine.tools.gcs;

import com.google.api.client.http.HttpTransport;
import com.google.common.annotations.VisibleForTesting;
import io.spine.tools.gradle.SpinePlugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.util.logging.Level;

import static io.spine.tools.gradle.TaskName.CLEAN_GCS;

/**
 * The plugin for working with Google Cloud Storage via Gradle tasks.
 *
 * @author Dmytro Grankin
 */
public class GcsPlugin extends SpinePlugin {

    @Override
    public void apply(Project project) {
        limitHttpLogging();

        final Task task = project.getTasks()
                                 .create(CLEAN_GCS.getValue(), CleanGcsTask.class);
        log().debug("{} added.", task);
    }

    /**
     * Sets {@link HttpTransport} logging level to {@link Level#WARNING}.
     *
     * <p>The goal of this is to limit excessive logging about
     * {@linkplain com.google.api.client.http.HttpRequest HTTP requests} and
     * {@linkplain com.google.api.client.http.HttpResponse HTTP responses}.
     */
    @VisibleForTesting
    static void limitHttpLogging() {
        final String name = HttpTransport.class.getName();
        final java.util.logging.Logger log = java.util.logging.Logger.getLogger(name);
        log.setLevel(Level.WARNING);
    }
}
