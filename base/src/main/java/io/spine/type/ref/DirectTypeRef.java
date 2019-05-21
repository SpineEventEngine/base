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

package io.spine.type.ref;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.protobuf.Descriptors.Descriptor;
import io.spine.code.proto.PackageName;
import io.spine.type.TypeName;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;

/**
 * A direct reference to a proto message type.
 */
public final class DirectTypeRef extends AbstractTypeRef {

    private static final long serialVersionUID = 0L;

    private final @Nullable PackageName packageName;
    private final String simpleTypeName;
    private final String nestedName;

    /**
     * Parses the passed value for the subject of direct type reference.
     *
     * @return an instance if the passed value is not a wildcard reference or an empty string,
     *         and empty {@code Optional} otherwise
     */
    static Optional<TypeRef> parse(String value) {
        checkNotNull(value);
        if (value.trim()
                 .isEmpty()
                || value.contains(InPackage.WILDCARD)
                || value.contains(CompositeTypeRef.SEPARATOR)) {
            return Optional.empty();
        }
        TypeRef result = new DirectTypeRef(value);
        return Optional.of(result);
    }

    private DirectTypeRef(String value) {
        super(value);
        String delimiter = PackageName.delimiter();
        List<String> parts =
                Splitter.on(delimiter)
                        .splitToList(value);
        this.packageName =
                value.contains(delimiter)
                ? toPackageName(parts)
                : null;
        this.simpleTypeName = parts.get(parts.size() - 1);
        this.nestedName = toNestedName(parts);
    }

    /**
     * Attempts to find a package name in the passed type reference.
     *
     * <p>Assumes that package names start with a lowercase letter.
     *
     * @return the package name, if found, or {@code null} otherwise
     */
    private static @Nullable PackageName toPackageName(List<String> parts) {
        List<String> packages =
                parts.stream()
                     .filter(p -> Character.isLowerCase(p.charAt(0)))
                     .collect(toList());
        if (packages.isEmpty()) {
            return null;
        }
        String result =
                Joiner.on(PackageName.delimiter())
                      .join(packages);
        return PackageName.of(result);
    }

    /**
     * Compose a potentially nested type name from the passed parts.
     *
     * <p>Assumes that a type name starts with an uppercase letter.
     */
    private static String toNestedName(List<String> parts) {
        List<String> types =
                parts.stream()
                     .filter(p -> Character.isUpperCase(p.charAt(0)))
                     .collect(toList());
        return Joiner.on(TypeName.NESTED_TYPE_SEPARATOR)
                     .join(types);
    }

    /**
     * Obtains package name used in the reference.
     */
    public Optional<PackageName> packageName() {
        return Optional.ofNullable(packageName);
    }

    /**
     * Verifies if the type reference has a package in the referenced type name.
     */
    public boolean hasPackage() {
        return packageName != null;
    }

    /**
     * Creates a new instance reference a type with the same nested name but in another package.
     */
    public DirectTypeRef withPackage(PackageName anotherPackage) {
        checkNotNull(anotherPackage);
        DirectTypeRef result = new DirectTypeRef(
                anotherPackage.value() + PackageName.delimiter() + this.nestedName
        );
        return result;
    }

    /**
     * Obtains simple type name of the direct type reference.
     *
     * <p>If a reference is for a nested type, returned value contains the most nested name.
     */
    public String simpleTypeName() {
        return simpleTypeName;
    }

    /**
     * Obtains the name of a type inside its package.
     *
     * <p>For a type nested inside another type(s) it will contain all type names in the
     * nesting hierarchy.
     *
     * <p>For a top level type the returned value would be equal to {@link #simpleTypeName}.
     */
    public String nestedTypeName() {
        return this.nestedName;
    }

    /**
     * Verifies if the passed message type matches this type reference.
     */
    @Override
    public boolean test(Descriptor message) {
        if (packageName != null) {
            PackageName packageOfMessage = PackageName.of(message);
            if (!packageName.equals(packageOfMessage)) {
                return false;
            }
        }
        boolean result = value().endsWith(message.getName());
        return result;
    }
}
