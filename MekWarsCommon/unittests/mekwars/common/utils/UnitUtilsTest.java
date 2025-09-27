package mekwars.common.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import megamek.common.AmmoType.Munitions;
import mekwars.common.util.UnitUtils;
import org.junit.jupiter.api.Test;

public class UnitUtilsTest {

    @Test
    void munitionIsInnerSphereOnly() {
        assertFalse(UnitUtils.isInnerSphereOnlyAmmo(EnumSet.of(Munitions.M_LISTEN_KILL)));
        assertTrue(UnitUtils.isInnerSphereOnlyAmmo(EnumSet.of(Munitions.M_INCENDIARY_LRM)));
    }
}
