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

package io.spine.base;

import com.google.common.base.Splitter;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;

import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utilities for working with {@link FieldPath} instances.
 *
 * @author Dmytro Dashenkov
 */
public final class FieldPaths {

    private static final Splitter dotSplitter = Splitter.on('.').trimResults();

    /**
     * Prevents the utility class instantiation.
     */
    private FieldPaths() {
    }

    /**
     * Parses the given field path into a {@link FieldPath}.
     *
     * @param stringPath
     *         non-empty field path
     * @return parsed field path
     */
    public static FieldPath parse(String stringPath) {
        checkNotNull(stringPath);
        checkArgument(!stringPath.isEmpty(), "Path must not be empty.");

        List<String> pathElements = dotSplitter.splitToList(stringPath);
        FieldPath result = FieldPath
                .newBuilder()
                .addAllFieldName(pathElements)
                .build();
        return result;
    }

    /**
     * Obtains the value of the field at the given field path from the given value holder.
     *
     * <p>For example, if the given path is {@code protocol.name} and the given value holder is of
     * type {@link io.spine.net.Uri io.spine.net.Uri}, the method invocation is equivalent to
     * {@code uri.getSchema().getName()}.
     *
     * @param holder
     *         message to obtain the (nested) field value from
     * @param path
     *         non-empty field path
     * @return the value of the field
     */
    public static Object fieldAt(Message holder, FieldPath path) {
        checkNotNull(holder);
        checkNotNull(path);
        checkArgument(path.getFieldNameCount() > 0, "Field path must not be empty.");

        Message message = holder;
        Object currentValue = message;
        for (Iterator<String> iterator = path.getFieldNameList().iterator(); iterator.hasNext(); ) {
            String fieldName = iterator.next();
            FieldDescriptor field = message.getDescriptorForType()
                                           .findFieldByName(fieldName);
            checkArgument(field != null, "Field `%s` is not found.", fieldName);
            currentValue = message.getField(field);
            if (currentValue instanceof Message) {
                message = (Message) currentValue;
            } else {
                checkArgument(!iterator.hasNext(), "%s is not a message.", currentValue);
            }
        }
        return currentValue;
    }
}
