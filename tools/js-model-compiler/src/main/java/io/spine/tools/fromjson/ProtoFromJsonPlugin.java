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

package io.spine.tools.fromjson;

import io.spine.tools.gradle.SpinePlugin;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;

import static io.spine.tools.gradle.TaskName.ADD_FROM_JSON;
import static io.spine.tools.gradle.TaskName.COMPILE_PROTO_TO_JS;
import static io.spine.tools.gradle.TaskName.COPY_MODULE_SOURCES;

public class ProtoFromJsonPlugin extends SpinePlugin {

    @Override
    public void apply(Project project) {
        Action<Task> task = newAction(project);
        newTask(ADD_FROM_JSON, task)
                .insertAfterTask(COMPILE_PROTO_TO_JS)
                .insertBeforeTask(COPY_MODULE_SOURCES)
                .applyNowTo(project);
    }

    private static Action<Task> newAction(Project project) {
        return task -> generateFromJsonForProto(project);
    }

    private static void generateFromJsonForProto(Project project) {
        System.out.println("Applying Js Model Compiler");
        ProtoFromJsonGenerator generator = ProtoFromJsonGenerator.createFor(project);
        if (generator.hasMessagesToProcess()) {
            System.out.println("Js Model Compiler has messages to process");
            generator.createFromJsonForProtos();
        }
    }
}
