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

import io.spine.code.proto.ref.TypeRef;
import io.spine.option.LifecycleOption;
import io.spine.option.OptionsProto;

import java.util.Optional;
import java.util.function.Function;

/**
 * A parser of the {@link LifecycleOption} values.
 */
public final class EntityLifecycleOption extends MessageOption<LifecycleOption> {

    public EntityLifecycleOption() {
        super(OptionsProto.lifecycle);
    }

    /**
     * Checks if a given message has any of the {@code lifecycle} options specified.
     */
    public boolean hasLifecycle(MessageType type) {
        Optional<LifecycleOption> option = valueFrom(type.descriptor());
        return option.isPresent();
    }

    /**
     * Obtains {@link LifecycleOption#getArchiveUpon()} archive_upon} option value.
     *
     * <p>If the {@code TypeRef} in option doesn't have package (i.e. is a raw type name), the
     * package of enclosing {@code MessageType} is assumed.
     *
     * <p>If the option is set to empty {@code String}, the method returns an empty
     * {@code Optional}.
     */
    public Optional<TypeRef> archiveUpon(MessageType type) {
        Optional<TypeRef> result = optionIfPresent(type, LifecycleOption::getArchiveUpon);
        return result;
    }

    /**
     * Obtains {@link LifecycleOption#getDeleteUpon() delete_upon} option value.
     *
     * <p>If the {@code TypeRef} in option doesn't have package (i.e. is a raw type name), the
     * package of enclosing {@code MessageType} is assumed.
     *
     * <p>If the option is set to empty {@code String}, the method returns an empty
     * {@code Optional}.
     */
    public Optional<TypeRef> deleteUpon(MessageType type) {
        Optional<TypeRef> result = optionIfPresent(type, LifecycleOption::getDeleteUpon);
        return result;
    }

    private Optional<TypeRef> optionIfPresent(MessageType type,
                                              Function<LifecycleOption, String> optionGetter) {
        Optional<LifecycleOption> option = valueFrom(type.descriptor());
        Optional<TypeRef> result =
                option.map(optionGetter)
                      .flatMap(EntityLifecycleOption::typeRefIfPresent)
                      .map(typeRef -> provideWithPackage(typeRef, type));
        return result;
    }

    private static Optional<TypeRef> typeRefIfPresent(String fieldValue) {
        Optional<TypeRef> result =
                Optional.of(fieldValue)
                        .map(String::trim)
                        .filter(str -> !str.isEmpty())
                        .map(TypeRef::parse);
        return result;
    }

    /**
     * Provides the given {@code TypeRef} with the {@code enclosing} message package, if it doesn't
     * have its own.
     */
    private static TypeRef provideWithPackage(TypeRef typeRef, MessageType enclosing) {
        PackageName packageName = PackageName.of(enclosing.descriptor());
        TypeRef result = typeRef.withPackage(packageName);
        return result;
    }
}
