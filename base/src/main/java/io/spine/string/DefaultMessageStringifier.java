/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.string;

import com.google.protobuf.Message;
import io.spine.json.Json;

/**
 * The default {@code Stringifier} for {@code Message} classes.
 *
 * <p>Suppose we have the following domain type definitions:
 * <pre>  {@code
 * message TaskId {
 *     string value = 1;
 * }
 *
 * message TaskName {
 *     string value = 1;
 * }
 *
 * message Task {
 *     TaskId id = 1;
 *     TaskName name = 2;
 * }}</pre>
 *
 * <p>The Java code for constructing the {@code Task} instance would be:
 *
 * <pre>  {@code
 * TaskId taskId = TaskId.newBuilder().setValue("task-id").build();
 * TaskName taskName = TaskName.newBuilder().setValue("task-name").build();
 * Task task = Task.newBuilder().setId(taskId).setTaskName(taskName).build();
 * }</pre>
 *
 * <p>Obtaining a default stringifier for the {@code Task} object (which is a {@code Message}),
 * and backward conversion would look like:
 *
 * <pre>  {@code
 * Stringifier<Task> taskStringifier = StringifierRegistry.getStringifier(Task.class);
 *
 * // The result of the below call would be {"id":{"value":"task-id"},"name":{"value":"task-name"}}.
 * String json = taskStringifier.reverse().convert(task);
 *
 * // task.equals(taskFromJson) == true.
 * Task taskFromJson = taskStringifier.convert(json);
 * }</pre>
 *
 * @param <T> the message type
 */
final class DefaultMessageStringifier<T extends Message> extends Stringifier<T> {

    private final Class<T> messageClass;

    DefaultMessageStringifier(Class<T> messageType) {
        super();
        this.messageClass = messageType;
    }

    @Override
    protected String toString(T obj) {
        return Json.toCompactJson(obj);
    }

    @Override
    protected T fromString(String s) {
        return Json.fromJson(s, messageClass);
    }
}
