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

package io.spine.tools.fs;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import io.spine.value.StringTypeValue;

import java.nio.file.Path;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A path to a file used in an import statement.
 */
@SuppressWarnings("ComparableImplementedButEqualsNotOverridden") // provided by parent class.
public final class FileReference extends StringTypeValue implements Comparable<FileReference> {

    private static final long serialVersionUID = 0L;
    /** The path separator used in JavaScript imports. Not platform-dependant. */
    private static final String IMPORT_PATH_SEPARATOR = "/";
    private static final String PARENT_DIR = ".." + IMPORT_PATH_SEPARATOR;
    private static final String CURRENT_DIR = '.' + IMPORT_PATH_SEPARATOR;

    private FileReference(String value) {
        super(value);
    }

    /**
     * Creates a new instance using the passed path.
     */
    public static FileReference of(String path) {
        checkNotEmptyOrBlank(path);
        return new FileReference(path);
    }

    /**
     * Creates a new instance using the passed file path.
     */
    public static FileReference of(Path path) {
        checkNotNull(path);
        return of(path.toString());
    }

    /**
     * Obtains the name of the imported file skipping the path to it.
     */
    public String fileName() {
        List<String> elements = elements();
        String fileName = elements.get(elements.size() - 1);
        return fileName;
    }

    /**
     * Obtains the directory used in the path.
     *
     * @return the directory path omitting relative path
     */
    public DirectoryReference directory() {
        String nonRelativePath = withoutRelative();
        int fileNameSeparator = nonRelativePath.lastIndexOf(IMPORT_PATH_SEPARATOR);
        if (fileNameSeparator < 0) {
            return DirectoryReference.currentDir();
        } else {
            String directory = nonRelativePath.substring(0, fileNameSeparator);
            return DirectoryReference.of(directory);
        }
    }

    /**
     * Tells if the path is relative.
     *
     * @return {@code true} if the path starts with the current or parent directory reference
     */
    public boolean isRelative() {
        boolean result = isRelativeToParent() || isRelativeToCurrent();
        return result;
    }

    public List<String> elements() {
        Iterable<String> elements = Splitter.on(separator())
                                            .split(value());
        return ImmutableList.copyOf(elements);
    }

    private String withoutRelative() {
        String result = value();
        if (result.startsWith(CURRENT_DIR)) {
            result = result.substring(CURRENT_DIR.length());
        }
        while (result.startsWith(PARENT_DIR)) {
            result = result.substring(PARENT_DIR.length());
        }
        return result;
    }

    private boolean isRelativeToParent() {
        return value().startsWith(parentDirectory());
    }

    private boolean isRelativeToCurrent() {
        return value().startsWith(currentDirectory());
    }

    /**
     * Obtains the separator used in JavaScript imports.
     */
    public static String separator() {
        return IMPORT_PATH_SEPARATOR;
    }

    /**
     * Obtains the string used to reference the parent directory in imports.
     */
    public static String parentDirectory() {
        return PARENT_DIR;
    }

    /**
     * Obtains the string used to reference the current directory in imports.
     */
    public static String currentDirectory() {
        return CURRENT_DIR;
    }

    @Override
    public int compareTo(FileReference o) {
        return value().compareTo(o.value());
    }
}
