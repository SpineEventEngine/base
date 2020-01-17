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

package io.spine.code.proto;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.SourceCodeInfo;
import com.google.protobuf.DescriptorProtos.SourceCodeInfo.Location;
import com.google.protobuf.Descriptors.Descriptor;
import io.spine.type.MessageType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * Encapsulates a {@linkplain Location#getPathList() location path}.
 *
 * <p>A location path represents {@linkplain Location#getPathList() list of
 * integers} that used to identify a {@linkplain Location location} in a ".proto" file.
 */
public final class LocationPath {

    private final List<Integer> path;

    /**
     * Creates a new instance.
     *
     * @param items the list of path items
     */
    private LocationPath(List<Integer> items) {
        this.path = (List<Integer>) checkPath(items);
    }

    /**
     * Creates an empty location path.
     */
    LocationPath() {
        this(new ArrayList<>());
    }

    /**
     * Creates an instance by source code location.
     */
    @SuppressWarnings("unused") // Included for referencing `Location` in Javadoc.
    LocationPath(Location location) {
        this(location.getPathList());
    }

    /**
     * Creates an instance for the passed message descriptor.
     */
    public static LocationPath fromMessage(Descriptor descriptor) {
        LocationPath path = new LocationPath();
        path.add(FileDescriptorProto.MESSAGE_TYPE_FIELD_NUMBER);
        if (!MessageType.isTopLevel(descriptor)) {
            Deque<Integer> parentPath = new ArrayDeque<>();
            Descriptor containingType = descriptor.getContainingType();
            while (containingType != null) {
                parentPath.addFirst(containingType.getIndex());
                containingType = containingType.getContainingType();
            }
            path.addAll(parentPath);
        }
        path.add(descriptor.getIndex());
        return path;
    }

    /**
     * Appends the path item to the end of this path.
     */
    void add(Integer item) {
        checkItem(item);
        path.add(item);
    }

    /**
     * Appends the location path to the end of this path.
     *
     * @param locationPath the location path
     */
    void addAll(LocationPath locationPath) {
        path.addAll(checkPath(locationPath.path));
    }

    void addAll(Collection<Integer> path) {
        checkNotNull(path);
        this.path.addAll(checkPath(path));
    }

    @VisibleForTesting
    List<Integer> toList() {
        return Collections.unmodifiableList(path);
    }

    private static Collection<Integer> checkPath(Collection<Integer> items) {
        checkNotNull(items);
        items.forEach(LocationPath::checkItem);
        return items;
    }

    private static void checkItem(Integer item) {
        checkArgument(item >= 0);
    }

    /**
     * Converts the instance to the {@code SourceCodeInfo.Location} instance in the given file.
     */
    public SourceCodeInfo.Location toLocation(FileDescriptorProto file) {
        List<Integer> thisPath = toList();
        for (SourceCodeInfo.Location location : file.getSourceCodeInfo()
                                                    .getLocationList()) {
            if (thisPath.equals(location.getPathList())) {
                return location;
            }
        }

        String msg = format("The location with %s path should be present in \"%s\".",
                            this, file.getName());
        throw new IllegalStateException(msg);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LocationPath)) {
            return false;
        }

        LocationPath that = (LocationPath) o;

        return path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public String toString() {
        return path.toString();
    }
}
