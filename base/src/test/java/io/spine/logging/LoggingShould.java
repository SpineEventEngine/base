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

package io.spine.logging;

import com.google.common.base.Supplier;
import com.google.common.testing.NullPointerTester;
import io.spine.logging.given.LoggingTestEnv.Base;
import io.spine.logging.given.LoggingTestEnv.ChildOne;
import io.spine.logging.given.LoggingTestEnv.ChildTwo;
import org.junit.Test;
import org.slf4j.Logger;

import static io.spine.test.Tests.assertHasPrivateParameterlessCtor;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @author Alexander Yevsyukov
 */
public class LoggingShould {

    @Test
    public void have_utility_ctor() {
        assertHasPrivateParameterlessCtor(Logging.class);
    }
    
    @Test
    public void supply_logger_for_class() {
        Supplier<Logger> supplier = Logging.supplyFor(getClass());
        final Logger logger = supplier.get();

        assertNotNull(logger);
        assertSame(logger, supplier.get());
    }

    @Test
    public void create_logger_for_each_class_in_hierarchy() {
        final Logger baseLogger = new Base().log();
        final Logger childOneLogger = new ChildOne().log();
        final Logger childTwoLogger = new ChildTwo().log();

        assertNotSame(baseLogger, childOneLogger);
        assertNotSame(baseLogger, childTwoLogger);
        assertNotSame(childOneLogger, childTwoLogger);

        assertNotEquals(baseLogger, childOneLogger);
        assertNotEquals(baseLogger, childTwoLogger);
        assertNotEquals(childOneLogger, childTwoLogger);

        assertTrue(baseLogger.getName().contains(Base.class.getName()));
        assertTrue(childOneLogger.getName().contains(ChildOne.class.getName()));
        assertTrue(childTwoLogger.getName().contains(ChildTwo.class.getName()));
    }

    @Test
    public void pass_null_tolerance_check() {
        new NullPointerTester().testAllPublicStaticMethods(Logging.class);
    }
}
