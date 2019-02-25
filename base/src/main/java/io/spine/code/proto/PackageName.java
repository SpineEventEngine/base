/*
 * Copyright 2019, TeamDev. All rights reserved.
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

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Descriptors.Descriptor;
import io.spine.type.KnownTypes;
import io.spine.value.StringTypeValue;

import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A name of a Protobuf package.
 */
@Immutable
public final class PackageName extends StringTypeValue {

    private static final long serialVersionUID = 0L;
    private static final String DELIMITER = ".";

    private static final Splitter PACKAGE_SPLITTER = Splitter.on(DELIMITER)
                                                             .omitEmptyStrings();
    private static final PackageName GOOGLE_PROTOBUF = new PackageName("google.protobuf");

    private PackageName(String value) {
        super(value);
    }

    /**
     * Creates a new instance with the passed value.
     */
    public static PackageName of(String value) {
        checkNotNull(value);
        PackageName result = new PackageName(value);
        return result;
    }

    /**
     * Obtains a package name for the passed message type.
     */
    public static PackageName of(Descriptor message) {
        checkNotNull(message);
        PackageName result = of(message.getFile()
                                       .getPackage());
        return result;
    }

    /**
     * Obtains the name of the Google Protobuf library package.
     */
    public static PackageName googleProtobuf() {
        return GOOGLE_PROTOBUF;
    }

    /**
     * Obtains Protobuf package delimiter.
     */
    public static String delimiter() {
        return String.valueOf(DELIMITER);
    }

    /**
     * Verifies if the package represented by this package name is
     * <a href="https://developers.google.com/protocol-buffers/docs/proto3#packages-and-name-resolution">
     * nested</a> in the passed package.
     */
    public boolean isInnerOf(PackageName parentCandidate) {
        checkNotNull(parentCandidate);
        boolean result = value().startsWith(parentCandidate.value());
        return result;
    }

    public PackageName resolve(PackageName other){
        return new PackageName(this.value() + DELIMITER + other.value());
    }

    public boolean contains(PackageName childCandidate) {
        checkNotNull(childCandidate);
        boolean result = value().contains(childCandidate.value());
        return result;
    }

    public boolean canReach(PackageName name) {
        // Packages referenced by their FQN are always reachable.
        if (name.isFqn()) {
            return true;
        }
        return childExists(name);
    }

    private boolean childExists(PackageName child) {
        ImmutableSet<PackageName> allPackages = KnownTypes.instance()
                                                          .packageNames();
        ImmutableList<PackageName> subpackages = subpackages();
        boolean result = false;
        for (PackageName thisRef : subpackages) {
            PackageName resolve = thisRef.resolve(child);
            if (allPackages.contains(resolve)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public boolean isFqn() {
        boolean result = KnownTypes.instance()
                                   .packageNames()
                                   .contains(this);
        return result;
    }

    public ImmutableList<PackageName> subpackages() {
        ImmutableList.Builder<PackageName> result = ImmutableList.builder();
        List<String> split = PACKAGE_SPLITTER.splitToList(this.value());
        @SuppressWarnings("ZeroLengthArrayAllocation")
        String[] subpackages = split.toArray(new String[0]);
        for (int i = 0; i < subpackages.length; i++) {
            String[] parentPackage = Arrays.copyOfRange(subpackages, 0, i + 1);
            String subpackage = String.join(DELIMITER, parentPackage);
            result.add(of(subpackage));
        }
        return result.build();
    }
}
