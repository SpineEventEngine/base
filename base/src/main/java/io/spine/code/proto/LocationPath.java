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

package io.spine.code.proto;

import com.google.protobuf.DescriptorProtos.SourceCodeInfo.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Encapsulates a {@linkplain Location#getPathList() location path}.
 *
 * <p>A location path represents {@linkplain Location#getPathList() list of
 * integers}, that used to identify a {@linkplain Location location} in a ".proto" file.
 */
public class LocationPath {

    private final List<Integer> path;

    /**
     * Creates an empty location path.
     */
    public LocationPath() {
        this.path = new ArrayList<>();
    }

    /**
     * Creates a new instance.
     *
     * @param locationPath the list of path items
     */
    public LocationPath(List<Integer> locationPath) {
        this.path = checkPath(locationPath);
    }

    /**
     * Creates an instance by source code location.
     */
    @SuppressWarnings("unused")
        // Included for future use and being able to reference `Location` in Javadoc directly.
    public LocationPath(Location location) {
        this(location.getPathList());
    }

    /**
     * Appends the path item to the end of this path location.
     *
     * @param pathItem the path item
     */
    public void add(Integer pathItem) {
        checkPathItem(pathItem);
        path.add(pathItem);
    }

    /**
     * Appends the location path to the end of this path location.
     *
     * @param locationPath the location path
     */
    public void addAll(LocationPath locationPath) {
        checkNotNull(locationPath);
        path.addAll(checkPath(locationPath.path));
    }

    public List<Integer> getPath() {
        return Collections.unmodifiableList(path);
    }

    private static List<Integer> checkPath(List<Integer> locationPath) {
        checkNotNull(locationPath);
        for (Integer pathItem : locationPath) {
            checkPathItem(pathItem);
        }
        return locationPath;
    }

    private static void checkPathItem(Integer pathItem) {
        checkArgument(pathItem >= 0);
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
