package io.spine.test;

import com.google.common.testing.NullPointerTester;
import org.junit.Test;

public class TestValuesShould {

    @Test
    public void have_utility_ctor() {
        Tests.assertHasPrivateParameterlessCtor(TestValues.class);
    }

    @Test
    public void pass_null_tolerance_check() {
        new NullPointerTester()
                .testAllPublicStaticMethods(TestValues.class);
    }
}
