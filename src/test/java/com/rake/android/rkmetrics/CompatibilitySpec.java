package com.rake.android.rkmetrics;

import com.rake.android.rkmetrics.android.Compatibility;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class CompatibilitySpec {
    @Test
    public void test_ApiLevel_fromInt() {
        assertEquals(
            Compatibility.ApiLevel.ICE_CREAM_SANDWICH,
            Compatibility.ApiLevel.fromInt(14));
    }

    @Test
    public void test_ApiLevel_fromInt_default() {
        assertEquals(
            Compatibility.ApiLevel.DEFAULT,
            Compatibility.ApiLevel.fromInt(-1)
        );

        assertEquals(
            Compatibility.ApiLevel.DEFAULT,
            Compatibility.ApiLevel.fromInt(0)
        );
        assertEquals(
            Compatibility.ApiLevel.DEFAULT,
            Compatibility.ApiLevel.fromInt(499)
        );
        assertEquals(
            Compatibility.ApiLevel.DEFAULT,
            Compatibility.ApiLevel.fromInt(1014010)
        );
    }
}
