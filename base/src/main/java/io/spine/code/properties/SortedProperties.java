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

import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeSet;

/**
 * Customized {@link Properties}, which key set is sorted.
 *
 * <p>The instance of this class is used to maintain the alphanumerical order
 * in the {@code .properties} files generated out of the instance contents.
 *
 * <p>Such a trick simplifies the resulting {@code .properties} file navigation
 * and makes any potential debugging easier.
 */
@SuppressWarnings("ClassExtendsConcreteCollection")
// It's the best and still readable way for customization.
final class SortedProperties extends Properties {

    // Generated automatically.
    private static final long serialVersionUID = 0L;

    @SuppressWarnings("RefusedBequest")
    // as we replace `keys()` with a completely different behavior.
    @Override
    public synchronized Enumeration<Object> keys() {
        return Collections.enumeration(new TreeSet<>(keySet()));
    }
}
