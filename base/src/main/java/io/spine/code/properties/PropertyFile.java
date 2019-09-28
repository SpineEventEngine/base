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

package io.spine.code.properties;

import com.google.common.collect.ImmutableMap;
import io.spine.code.AbstractSourceFile;
import io.spine.code.SourceCodeDirectory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkArgument;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A name of a {@code .properties} file.
 */
public class PropertyFile extends AbstractSourceFile {

    private static final String EXTENSION = ".properties";

    private PropertyFile(Path path) {
        super(path);
    }

    /**
     * Creates a new instance for the passed file name.
     *
     * <p>The passed value must have the standard suffix of property files.
     */
    public static PropertyFile of(String name) {
        checkNotEmptyOrBlank(name);
        checkArgument(name.endsWith(EXTENSION));
        PropertyFile result = new PropertyFile(Paths.get(name));
        return result;
    }

    /**
     * Obtains the instance relative to the passed parent directory.
     */
    public PropertyFile at(SourceCodeDirectory parent) {
        Path newPath = parent.resolve(this);
        PropertyFile result = new PropertyFile(newPath);
        return result;
    }

    /**
     * Loads properties stored in the file.
     */
    public Map<String, String> load() {
        try {
            InputStream inputStream = new FileInputStream(path().toFile());
            Properties properties = new Properties();
            properties.load(inputStream);

            // Convert `Properties` to `Map`.
            ImmutableMap.Builder<String, String> result = ImmutableMap.builder();
            for (String name: properties.stringPropertyNames()) {
                result.put(name, properties.getProperty(name));
            }
            return result.build();
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }
}
