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

package io.spine.reflect;

import com.google.errorprone.annotations.Immutable;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides additional run-time information about a Java package.
 *
 * @author Alexander Yevsyukov
 */
@Immutable
public final class PackageInfo implements Comparable<PackageInfo> {

    /**
     * Java package which this instance analyzes.
     *
     * @implNote Even though, that {@code Package} objects are not immutable,
     *           the data we use from them (e.g. {@linkplain Package#getName() name}) is immutable.
     *           That's why it's OK to tell Error Prone that the field is immutable.
     */
    @SuppressWarnings("Immutable") // see implNote
    private final Package value;

    private PackageInfo(Package value) {
        this.value = value;
    }

    /**
     * Obtains an instance for the passed package value.
     */
    public static PackageInfo of(Package value) {
        checkNotNull(value);
        PackageInfo result = new PackageInfo(value);
        return result;
    }

    /**
     * Obtains an instance with the package of the passed class.
     */
    public static PackageInfo of(Class<?> cls) {
        checkNotNull(cls);
        PackageInfo result = new PackageInfo(cls.getPackage());
        return result;
    }

    /**
     * Obtains the value stored in the node.
     */
    public Package getValue() {
        return value;
    }

    /** Returns {@code true} if the enclosed package value is equal to the passed instance. */
    boolean isAbout(Package p) {
        checkNotNull(p);
        boolean result = value.equals(p);
        return result;
    }

    /**
     * Returns the name of the package.
     */
    @Override
    public String toString() {
        return value.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PackageInfo)) {
            return false;
        }
        PackageInfo node = (PackageInfo) o;
        return Objects.equals(value, node.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public int compareTo(PackageInfo o) {
        return value.getName()
                    .compareTo(o.getValue()
                                .getName());
    }
}
