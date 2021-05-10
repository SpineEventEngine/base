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

package io.spine.tools.javadoc;

import com.google.common.collect.ImmutableSet;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.RootDoc;
import io.spine.annotation.Internal;

import java.util.Set;

/**
 * Implementation of the {@linkplain Filter} interface for excluding documentation of elements
 * with {@linkplain Internal} annotation.
 *
 * <p>Excludes all {@linkplain Internal}-annotated program elements, packages,
 * and their subpackages.
 */
final class ExcludeInternal implements Filter {

    private final AnnotationCheck<Class<Internal>> internalAnnotation =
            new AnnotationCheck<>(Internal.class);

    /**
     * Packages to be excluded in the passed documentation root.
     */
    private final Set<PackageDoc> excludedPackages;

    ExcludeInternal(RootDoc root) {
        PackageCollector packageCollector = new PackageCollector(internalAnnotation);
        Set<PackageDoc> collected = packageCollector.collect(root);
        this.excludedPackages = ImmutableSet.copyOf(collected);
    }

    @Override
    public boolean test(ProgramElementDoc element) {
        return internalAnnotation.test(element) || inExclusions(element);
    }

    /**
     * Tells if a package of the passed element is one of the {@link #excludedPackages},
     * or is a sub-package of one of them.
     */
    private boolean inExclusions(ProgramElementDoc element) {
        String packageName = element.containingPackage().name();
        for (PackageDoc exclusion : excludedPackages) {
            if (packageName.startsWith(exclusion.name())) {
                return true;
            }
        }
        return false;
    }
}
