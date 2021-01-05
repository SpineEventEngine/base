/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.type.TypeName;
import io.spine.type.TypeUrl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.protobuf.Descriptors.FieldDescriptor.JavaType.MESSAGE;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * An external message {@linkplain io.spine.option.OptionsProto#constraintFor constraint}.
 *
 * <p>Contains descriptors related to a particular external constraint.
 */
final class ExternalMessageConstraint {

    /**
     * The delimiter in a full field name reference.
     */
    private static final String FIELD_NAME_SEPARATOR = ".";

    /**
     * The descriptor for the external constraint message.
     */
    private final Descriptor descriptor;

    /**
     * Descriptors for the target fields of the external constraint.
     */
    private final ImmutableSet<FieldDescriptor> targets;

    /**
     * Creates a new instance.
     *
     * <p>Must be used only in {@link ExternalConstraints}.
     *
     * @param descriptor
     *         the message descriptor of the external constraint
     * @param targetPaths
     *         the paths to the external constraint targets
     */
    ExternalMessageConstraint(Descriptor descriptor, Iterable<String> targetPaths) {
        this.descriptor = checkNotNull(descriptor);
        this.targets = constructTargets(descriptor, checkNotNull(targetPaths));
    }

    Descriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Obtains targets for the external constraint.
     *
     * @return an immutable collection of the targets
     */
    ImmutableSet<FieldDescriptor> getTargets() {
        return targets;
    }

    /**
     * Checks that this constraint targets the field with the given name defined in the given type.
     */
    boolean hasTarget(Descriptor containingType, String fieldName) {
        return targets.stream()
                      .anyMatch(field -> isSame(field, containingType, fieldName));
    }

    private static boolean isSame(FieldDescriptor field, Descriptor containerType,
                                  String fieldName) {
        return field.getContainingType()
                    .getFullName()
                    .equals(containerType.getFullName()) && field.getName().equals(fieldName);
    }

    private static ImmutableSet<FieldDescriptor>
    constructTargets(Descriptor constraint, Iterable<String> targetPaths) {
        ImmutableSet.Builder<FieldDescriptor> targets = ImmutableSet.builder();
        for (String targetPath : targetPaths) {
            FieldDescriptor target = getTargetDescriptor(targetPath);
            checkConstraintFields(constraint, target);
            targets.add(target);
        }
        return targets.build();
    }

    /**
     * Obtains constraint target descriptor by the specified path.
     *
     * @param targetPath
     *         the path to a external constraint target
     * @return the field descriptor
     */
    private static FieldDescriptor getTargetDescriptor(String targetPath) {
        int typeAndFieldNameBound = targetPath.lastIndexOf(FIELD_NAME_SEPARATOR);
        if (typeAndFieldNameBound == -1) {
            String msg = "Invalid external constraint target `%s`. " +
                    "Proper format is `package.TargetMessage.target_field`.";
            throw newIllegalStateException(msg, targetPath);
        }

        String fieldName = targetPath.substring(typeAndFieldNameBound + 1);
        String targetMessageType = targetPath.substring(0, typeAndFieldNameBound);
        Descriptor message = TypeName.of(targetMessageType)
                                     .messageDescriptor();
        FieldDescriptor field = message.findFieldByName(fieldName);
        if (field == null) {
            throw newIllegalStateException("The field '%s' is not found in the '%s' message.",
                                           fieldName, message.getName());
        }
        return checkTargetType(field);
    }

    private static FieldDescriptor checkTargetType(FieldDescriptor targetDescriptor) {
        if (targetDescriptor.getJavaType() != MESSAGE) {
            String errMsg = "External constraint target must be a Message. Specified type is `%s`.";
            throw newIllegalStateException(errMsg, targetDescriptor.getJavaType());
        }
        return targetDescriptor;
    }

    /**
     * Ensures that fields from the external constraint are present in the constraint target.
     *
     * <p>A field is considered present if the target has the field with the same name and type.
     *
     * @param constraint
     *         the constraint descriptor
     * @param target
     *         the target of the constraint
     */
    private static void checkConstraintFields(Descriptor constraint, FieldDescriptor target) {
        for (FieldDescriptor constraintField : constraint.getFields()) {
            Descriptor targetType = target.getMessageType();
            String fieldName = constraintField.getName();
            FieldDescriptor targetField = targetType.findFieldByName(fieldName);
            if (targetField == null) {
                String msg = "The external constraint '%s' declares the field `%s`, " +
                        "which was not found in the `%s` message.";
                throw newIllegalStateException(msg, constraint.getFullName(),
                                               fieldName, targetType.getName());
            }

            boolean isCorrectType = constraintField.getJavaType() == targetField.getJavaType();
            if (!isCorrectType) {
                String errMsg = "`%s` must be of type `%s`.";
                throw newIllegalStateException(errMsg, constraintField.getFullName(),
                                               targetField.getJavaType());
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ExternalMessageConstraint other = (ExternalMessageConstraint) o;

        return typeUrl().equals(other.typeUrl());
    }

    @Override
    public int hashCode() {
        return typeUrl().hashCode();
    }

    private TypeUrl typeUrl() {
        return TypeUrl.from(descriptor);
    }
}
