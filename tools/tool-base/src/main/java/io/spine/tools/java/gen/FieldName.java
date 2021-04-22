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

package io.spine.tools.java.gen;

import io.spine.code.AbstractFieldName;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Character.toUpperCase;

/**
 * A name of a field declared in a Java class.
 */
public final class FieldName extends AbstractFieldName {

    private static final long serialVersionUID = 0L;
    private static final FieldName SERIAL_VERSION_UID = new FieldName("serialVersionUID");

    private FieldName(String value) {
        super(value);
    }

    /**
     * Creates Java field name that corresponds to the passed Proto field name.
     */
    public static FieldName from(io.spine.code.proto.FieldName protoField) {
        checkNotNull(protoField);
        String fieldName = protoField.javaCase();
        FieldName result = new FieldName(fieldName);
        return result;
    }

    /** Obtains this name starting with a capital letter. */
    public String capitalize() {
        String name = value();
        String result = toUpperCase(name.charAt(0)) + name.substring(1);
        return result;
    }

    public static FieldName serialVersionUID() {
        return SERIAL_VERSION_UID;
    }
}
