package com.beautyinblocks.kncraft;

import com.beautyinblocks.performance.NearestSelection;
import com.beautyinblocks.kncraft.integration.encounters.EncounterRules;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ParityTest {
    private record Candidate(int identity, double distance) {}
    @Test void linearSelectionMatchesStableSortIncludingNonFiniteValues() {
        Random random = new Random(0x4b4e4352414654L);
        Comparator<Candidate> order = Comparator.comparingDouble(Candidate::distance);
        double[] special = {Double.NaN, -0.0, 0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1, 1, 2};
        for (int trial = 0; trial < 10000; trial++) {
            var actual = new ArrayList<Candidate>();
            for (int i = 0, count = random.nextInt(65); i < count; i++)
                actual.add(new Candidate(i, random.nextBoolean() ? special[random.nextInt(special.length)] : random.nextDouble()));
            var expected = new ArrayList<>(actual);
            expected.sort(order);
            NearestSelection.moveMinimumFirst(actual, order);
            if (!actual.isEmpty()) assertSame(expected.get(0), actual.get(0), "trial " + trial);
            assertEquals(new HashSet<>(expected), new HashSet<>(actual));
        }
    }
    @Test void migrationMarkersAndDimensionScopePreventRepeatScaling() {
        assertTrue(EncounterRules.eligible("witherstormmod:wither_storm", "aether:the_aether", Set.of()));
        assertFalse(EncounterRules.eligible("witherstormmod:wither_storm", "aether:the_aether", Set.of("kncraft_storm_balanced")));
        assertFalse(EncounterRules.eligible("witherstormmod:wither_storm", "nomadictents:tent", Set.of()));
        assertFalse(EncounterRules.eligible("witherstormmod:command_block", "minecraft:overworld", Set.of()));
        assertTrue(EncounterRules.eligible("witherstormmod:command_block", "witherstormmod:bowels", Set.of()));
        assertFalse(EncounterRules.eligible("witherstormmod:command_block", "witherstormmod:bowels", Set.of("kncraft_balanced")));
    }
}
