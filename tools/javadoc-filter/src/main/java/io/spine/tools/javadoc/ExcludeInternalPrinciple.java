/*
 * Copyright 2020, TeamDev. All rights reserved.
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

import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.RootDoc;
import io.spine.annotation.Internal;

import java.util.Collection;

/**
 * Implementation of the {@linkplain ExcludePrinciple} interface for
 * {@linkplain Internal} annotation.
 *
 * <p>Excludes all {@linkplain Internal}-annotated program elements, packages,
 * and their subpackages.
 */
final class ExcludeInternalPrinciple implements ExcludePrinciple {

    private final Collection<PackageDoc> exclusions;
    private final AnnotationAnalyst<Class<Internal>> internalAnalyst =
            new AnnotationAnalyst<>(Internal.class);

    ExcludeInternalPrinciple(RootDoc root) {
        exclusions = getExclusions(root);
    }

    @Override
    public boolean shouldExclude(ProgramElementDoc doc) {
        return inExclusions(doc) || internalAnalyst.hasAnnotation(doc);
    }

    private boolean inExclusions(ProgramElementDoc doc) {
        String docPackageName = doc.containingPackage()
                                   .name();

        for (PackageDoc exclusion : exclusions) {
            if (docPackageName.startsWith(exclusion.name())) {
                return true;
            }
        }

        return false;
    }

    private Collection<PackageDoc> getExclusions(RootDoc root) {
        PackageCollector packageCollector = new PackageCollector(internalAnalyst);
        return packageCollector.collect(root);
    }
}
