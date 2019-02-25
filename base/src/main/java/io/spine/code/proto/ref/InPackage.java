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

package io.spine.code.proto.ref;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Descriptors.Descriptor;
import io.spine.code.proto.PackageName;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * Reference to all message types in a proto package.
 */
final class InPackage extends AbstractTypeRef {

    private static final long serialVersionUID = 0L;

    /**
     * Specifies all types within a package.
     */
    static final String WILDCARD = "*";

    /**
     * A suffix for referencing all types in a proto package.
     */
    private static final String WILDCARD_SUFFIX = '.' + WILDCARD;

    /**
     * The name of the referenced proto package.
     */
    private final PackageName packageName;

    /**
     * Obtains a reference to a proto package from the passed string.
     *
     * <p>The reference to all proto types in a package is a package name followed
     * with the {@code ".*"} suffix (e.g. {@code "spine.base.*"}).
     *
     * @param value
     *         the string to parse
     * @return a reference to all proto types in a package,
     *         or empty {@code Optional} if the passed value is not a valid package reference
     */
    public static Optional<TypeRef> parse(String value) {
        checkNotNull(value);
        if (value.endsWith(WILDCARD_SUFFIX) && !WILDCARD_SUFFIX.equals(value)) {
            return Optional.of(new InPackage(value));
        }
        return Optional.empty();
    }

    @Override
    public TypeRef ensurePackage(PackageName aPackage) {
        // Check whether WE already ARE an FQN.
        // If it is -> return it;
        if (packageName.isFqn()) {
            return this;
        }
        // Else find out which one exactly is ours.
        ImmutableList<PackageName> subpackages = aPackage.subpackages();
        for (PackageName subpackage : subpackages) {
            PackageName candidate = subpackage.resolve(packageName);
            if (candidate.isFqn()) {
                return new InPackage(candidate);
            }
        }
        throw newIllegalArgumentException("Could not find a package with name %s.", packageName);
    }

    private InPackage(String value) {
        super(value);
        String packageName = packageStatement(value);
        this.packageName = PackageName.of(packageName);
    }

    private InPackage(PackageName packageName){
        super(packageName.value());
        this.packageName = packageName;
    }

    @Override
    public Optional<PackageName> packageName() {
        return Optional.of(packageName);
    }

    private static String packageStatement(String value) {
        int suffixIndex = value.lastIndexOf(WILDCARD_SUFFIX);
        return value.substring(0, suffixIndex);
    }

    /**
     * Tests that the passed message <em>directly</em> belongs to the referenced package.
     */
    @Override
    public boolean test(Descriptor message) {
        PackageName packageOfMessage = PackageName.of(message);
        boolean result = packageOfMessage.isInnerOf(packageName);
        return result;
    }
}
