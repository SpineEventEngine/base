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

package io.spine.code.java;

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import io.spine.value.StringTypeValue;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A Java package name.
 */
public final class PackageName extends StringTypeValue {

    private static final long serialVersionUID = 0L;
    private static final char DELIMITER_CHAR = '.';
    private static final String DELIMITER = String.valueOf(DELIMITER_CHAR);

    private PackageName(String value) {
        super(checkNotEmptyOrBlank(value));
    }

    /**
     * Creates instance for the passed package name.
     *
     * @param value
     *         the package name, which cannot be empty or blank
     * @return new instance
     */
    public static PackageName of(String value) {
        PackageName result = new PackageName(value);
        return result;
    }

    /**
     * Creates an instance for the passed class.
     *
     * @param cls
     *         the class to create the package for
     * @return a new instance
     */
    public static PackageName of(Class cls) {
        return of(cls.getPackage()
                     .getName());
    }

    /**
     * Obtains Java package delimiter as a {@code String}.
     */
    public static String delimiter() {
        return DELIMITER;
    }

    /**
     * Obtains Java package delimiter as a single {@code char}.
     */
    public static char delimiterChar() {
        return DELIMITER_CHAR;
    }

    /**
     * Obtains a Java package name by the passed file descriptor.
     */
    public static PackageName resolve(FileDescriptorProto file) {
        String javaPackage = resolveName(file).trim();
        checkArgument(!javaPackage.isEmpty(),
                      "Message classes generated from the file `%s` belong to the default package.%s"
                    + "Use `option java_package` or `package` to specify the Java package.",
                      file.getName(),
                      System.lineSeparator());
        PackageName result = new PackageName(javaPackage);
        return result;
    }

    private static String resolveName(FileDescriptorProto file) {
        String javaPackage = file.getOptions()
                                 .getJavaPackage();
        if (isNullOrEmpty(javaPackage)) {
            javaPackage = file.getPackage();
        }
        return javaPackage;
    }
}
