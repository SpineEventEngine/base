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

package io.spine.validate;

import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.base.FieldPath;

import java.util.Collections;
import java.util.Deque;
import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;
import static java.util.Collections.singleton;

/**
 * A chain of field descriptors leading to the field descriptor.
 *
 * <p>This class extends conception of {@link FieldPath}
 * and additionally to field names stores the descriptors for the fields.
 *
 * @author Dmytro Grankin
 */
class DescriptorPath {

    private final Deque<FieldDescriptor> descriptors;
    private final FieldPath fieldPath;

    private DescriptorPath(Iterable<FieldDescriptor> descriptors) {
        this.descriptors = newLinkedList(descriptors);
        this.fieldPath = fieldPathOf(descriptors);
    }

    /**
     * Creates a new instance for the specified root descriptor.
     *
     * @param rootDescriptor the root of path to create
     * @return the descriptor path
     */
    static DescriptorPath newInstance(FieldDescriptor rootDescriptor) {
        return new DescriptorPath(singleton(rootDescriptor));
    }

    /**
     * Obtains empty descriptor path.
     *
     * @return the descriptor path
     */
    static DescriptorPath empty() {
        return new DescriptorPath(Collections.<FieldDescriptor>emptyList());
    }

    /**
     * Obtains {@code DescriptorPath} for the specified child.
     *
     * @param child the child descriptor
     * @return the descriptor path
     */
    DescriptorPath forChild(FieldDescriptor child) {
        final List<FieldDescriptor> newDescriptors = newLinkedList(descriptors);
        newDescriptors.add(child);
        return new DescriptorPath(newDescriptors);
    }

    /**
     * Obtains last element of this path.
     *
     * @return the field descriptor
     */
    FieldDescriptor getLast() {
        return descriptors.getLast();
    }

    /**
     * Obtains field path for the descriptor path.
     *
     * @return the field path
     */
    FieldPath getFieldPath() {
        return fieldPath;
    }

    private static FieldPath fieldPathOf(Iterable<FieldDescriptor> descriptors) {
        final FieldPath.Builder builder = FieldPath.newBuilder();
        for (FieldDescriptor descriptor : descriptors) {
            final String fieldName = descriptor.getName();
            builder.addFieldName(fieldName);
        }
        return builder.build();
    }
}
