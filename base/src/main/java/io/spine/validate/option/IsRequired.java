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

package io.spine.validate.option;

import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Descriptors.OneofDescriptor;
import io.spine.tools.code.proto.OneofDeclaration;
import io.spine.validate.Constraint;

import java.util.Optional;

import static io.spine.option.OptionsProto.isRequired;

/**
 * A {@code oneof} validation option which constrains the target {@code oneof} group to be set.
 *
 * <p>If the value of the option is {@code true}, one of the fields in the group must be set.
 */
@Immutable
public class IsRequired implements ValidatingOption<Boolean, OneofDeclaration, OneofDescriptor> {

    @Override
    public Constraint constraintFor(OneofDeclaration field) {
        return new IsRequiredConstraint(field);
    }

    @Override
    public Optional<Boolean> valueFrom(OneofDescriptor descriptor) {
        boolean value = descriptor.getOptions()
                                  .getExtension(isRequired);
        return value ? Optional.of(true) : Optional.empty();
    }
}
