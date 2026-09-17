package com.beautyinblocks.kncraft;

import com.beautyinblocks.kncraft.integration.equipment.FeedingPolicy;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FeedingPolicyTest {
    @Test void protectsBothTemperatureExtremesWithoutChangingOrdinaryFood() {
        assertFalse(FeedingPolicy.allows(-50, -.2, 20, 10));
        assertFalse(FeedingPolicy.allows(50, .2, 20, 10));
        assertTrue(FeedingPolicy.allows(-50, .2, 20, 10));
        assertTrue(FeedingPolicy.allows(50, -.2, 20, 10));
        assertTrue(FeedingPolicy.allows(-50, 0, 20, 10));
        assertTrue(FeedingPolicy.allows(0, -.2, 20, 10));
    }
    @Test void avoidsStarvationWhenOnlyCounterproductiveFoodRemains() {
        assertTrue(FeedingPolicy.allows(-50, -.2, 20, 6));
        assertTrue(FeedingPolicy.allows(50, .2, 20, 0));
    }
}
