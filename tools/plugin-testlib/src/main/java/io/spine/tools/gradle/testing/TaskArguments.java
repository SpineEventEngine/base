/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.tools.gradle.testing;

import io.spine.tools.gradle.TaskName;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Create Gradle Runner arguments for a task.
 */
final class TaskArguments {

    /** Gradle command line argument to turn stacktrace output. */
    private static final String STACKTRACE_CLI_OPTION = "--stacktrace";

    /** Gradle command line argument to turn debug level of logging. */
    private static final String DEBUG_CLI_OPTION = "--debug";

    /** Provides type information for list-to-array conversion. */
    private static final String[] OF_STRING = new String[0];

    /** If true debug level of logging will be turned for a task. */
    private final boolean debug;

    static TaskArguments mode(boolean debug) {
        return new TaskArguments(debug);
    }

    private TaskArguments(boolean debug) {
        this.debug = debug;
    }

    String[] of(TaskName taskName) {
        String task = taskName.name();
        List<String> result = newArrayList(task, STACKTRACE_CLI_OPTION);
        if (debug) {
            result.add(DEBUG_CLI_OPTION);
        }
        return result.toArray(OF_STRING);
    }
}
