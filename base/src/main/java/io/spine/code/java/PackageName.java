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

package io.spine.code.java;

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import io.spine.value.StringTypeValue;

import java.io.File;
import java.nio.file.Paths;

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
        super(value);
    }

    /**
     * Creates instance for the passed package name.
     *
     * @param value
     *         package name, which cannot be empty or blank
     * @return new instance
     */
    public static PackageName of(String value) {
        checkNotEmptyOrBlank(value);
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
     * Obtains Java package delimiter.
     */
    public static String delimiter() {
        return DELIMITER;
    }

    /**
     * Obtains a Java package name by the passed file descriptor.
     */
    public static PackageName resolve(FileDescriptorProto file) {
        String javaPackage = resolveName(file);
        PackageName result = new PackageName(javaPackage.trim());
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

    /**
     * Obtains file system folder path for the package.
     */
    public Directory toDirectory() {
        String packageDir = value().replace(DELIMITER_CHAR, File.separatorChar);
        Directory result = Directory.at(Paths.get(packageDir));
        return result;
    }
}
