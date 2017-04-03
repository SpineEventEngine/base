/*
 * Copyright 2016, TeamDev Ltd. All rights reserved.
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
package org.spine3.tools.codestyle.javadoc;

import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * Utility class to save and address results of fully qualified name javadoc check.
 *
 * @author Alexander Aleksandrov
 */
public class InvalidResultStorage {

    private static final Map<Path, List<Optional<InvalidFqnUsage>>> resultStorage = new HashMap<>();

    public Map<Path, List<Optional<InvalidFqnUsage>>> getResults() {
        return resultStorage;
    }

    public int getLinkTotal() {
        int total = 0;
        for (List<Optional<InvalidFqnUsage>> l : resultStorage.values()) {
            total += l.size();
        }
        return total;
    }

    @SuppressWarnings("MethodWithMultipleLoops")//we need it to go through a map
    public void logInvalidFqnUsages() {
        final Iterator iterator = resultStorage.entrySet()
                                         .iterator();
        while (iterator.hasNext()) {
            Map.Entry<Path, List<Optional<InvalidFqnUsage>>> pair = (Map.Entry) iterator.next();
            for (Optional<InvalidFqnUsage> link : pair.getValue()) {
                if (link.isPresent()) {
                    final InvalidFqnUsage invalidFqnUsage = link.get();
                    final String msg = format(
                            " Wrong link format found: %s on %s line in %s",
                            invalidFqnUsage.getActualUsage(),
                            invalidFqnUsage.getIndex(),
                            pair.getKey());
                    log().error(msg);
                }
            }
            iterator.remove();
        }
    }

    /**
     * Add a new record to storage if it is already exist or creates a new one in case if it's not.
     *
     * @param path file path that contain wrong fomated links
     * @param list list of invalid fully qualified names usages
     */
    public void save(Path path, List<Optional<InvalidFqnUsage>> list) {
        resultStorage.put(path, list);
    }

    private static Logger log() {
        return InvalidResultStorage.LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(InvalidResultStorage.class);
    }
}
