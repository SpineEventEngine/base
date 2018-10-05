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

package io.spine.js.gradle;

import org.gradle.api.Task;

import static com.google.common.base.Preconditions.checkState;

/**
 * An extension for the {@link ProtoJsPlugin} which allows to obtain the {@code generateJsonParsers}
 * task to configure before and after which tasks it will be executed.
 */
public class Extension {

    private Task generateParsersTask;

    /**
     * Returns the {@code generateJsonParsers} task configured by the {@link ProtoJsPlugin}.
     */
    @SuppressWarnings("unused") // Used in project applying the plugin.
    public Task generateParsersTask() {
        checkState(generateParsersTask != null,
                   "The 'generateJsonParsers' task was not configured by the ProtoJS plugin");
        return generateParsersTask;
    }

    /**
     * Makes the extension read-only for all plugin users.
     */
    void setGenerateParsersTask(Task generateParsersTask) {
        this.generateParsersTask = generateParsersTask;
    }
}
