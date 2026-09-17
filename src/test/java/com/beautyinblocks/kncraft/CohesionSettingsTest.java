package com.beautyinblocks.kncraft;

import com.beautyinblocks.kncraft.integration.climate.CohesionSettings;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CohesionSettingsTest {
    @Test void rejectsUnboundedOrMalformedEffects() {
        for (String amount : new String[]{"NaN", "Infinity", "-Infinity", "0", "0.51", "-0.51"})
            assertThrows(IllegalArgumentException.class, () -> CohesionSettings.meal("minecraft:apple|" + amount + "|1200"));
        for (String duration : new String[]{"0", "19", "2401", "forever"})
            assertThrows(IllegalArgumentException.class, () -> CohesionSettings.meal("minecraft:apple|0.2|" + duration));
        assertThrows(IllegalArgumentException.class, () -> CohesionSettings.meal("invalid ID|0.2|1200"));
        assertThrows(IllegalArgumentException.class, () -> CohesionSettings.insulator("cotton|a:b|item|NaN|1"));
        assertThrows(IllegalArgumentException.class, () -> CohesionSettings.insulator("cotton|a:b|item|-1|1"));
        assertThrows(IllegalArgumentException.class, () -> CohesionSettings.insulator("cotton|a:b|curio|1|1"));
    }
    @Test void preservesDefaultTuningAndBounds() {
        assertEquals(.5, CohesionSettings.insulator("cotton|pamhc2crops:cottonitem|item|1|0.5").heat());
        assertEquals(-.2, CohesionSettings.meal("pamhc2foodcore:icecreamitem|-0.2|1200").amount());
        assertEquals(2400, CohesionSettings.meal("a:b|0.5|2400").duration());
    }
}
