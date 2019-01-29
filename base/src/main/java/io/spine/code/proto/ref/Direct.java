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

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.protobuf.Descriptors.Descriptor;
import io.spine.code.proto.PackageName;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.code.proto.ref.BuiltIn.ALL;
import static io.spine.code.proto.ref.BuiltIn.SELF;
import static java.util.stream.Collectors.toList;

/**
 * A direct reference to a proto message type.
 */
final class Direct extends AbstractTypeRef {

    private static final long serialVersionUID = 0L;

    private final @Nullable PackageName packageName;
    private final String simpleTypeName;

    /**
     * Parses the passed value for the subject of direct type reference.
     *
     * @return an instance if the passed value is not a wildcard reference or an empty string,
     *         and empty {@code Optional} otherwise
     */
    static Optional<TypeRef> parse(String value) {
        checkNotNull(value);
        if (value.equals(SELF.value())) {
            return Optional.empty();
        }
        if (value.contains(ALL.value())) {
            return Optional.empty();
        }
        TypeRef result = new Direct(value);
        return Optional.of(result);
    }

    private Direct(String value) {
        super(value);
        String delimiter = PackageName.delimiter();
        List<String> parts =
                Splitter.on(delimiter)
                        .splitToList(value);
        this.packageName =
                value.contains(delimiter)
                ? parsePackage(parts)
                : null;
        this.simpleTypeName = parts.get(parts.size() - 1);
    }

    /**
     * Attempts to find a package name in the passed type reference.
     *
     * <p>Assumes that package names start from a lowercase letter.
     *
     * @return the package name, if found, or {@code null} otherwise
     */
    private static @Nullable PackageName parsePackage(List<String> parts) {
        List<String> packages =
                parts.stream()
                     .filter(p -> Character.isLowerCase(p.charAt(0)))
                     .collect(toList());
        String result =
                Joiner.on(PackageName.delimiter())
                      .join(packages);
        return PackageName.of(result);
    }

    /**
     * Obtains package name used in the reference.
     */
    Optional<PackageName> packageName() {
        return Optional.ofNullable(packageName);
    }

    /**
     * Obtains simple type name of the direct type reference.
     *
     * <p>If a reference is for a nested type, returned value contains the most nested name.
     */
    String simpleTypeName() {
        return this.simpleTypeName;
    }

    @Override
    public boolean test(Descriptor message) {
        if (packageName != null) {
            if (!packageName.equals(PackageName.of(message))) {
                return false;
            }
        }
        boolean result = value().endsWith(message.getName());
        return result;
    }
}
