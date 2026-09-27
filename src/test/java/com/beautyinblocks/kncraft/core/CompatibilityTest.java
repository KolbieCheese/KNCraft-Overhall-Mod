package com.beautyinblocks.kncraft.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.VersionRange;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CompatibilityTest {
    @Test void auditedVersionsAgreeWithForgeDependencies() throws Exception {
        String metadata = Files.readString(Path.of("src/main/resources/META-INF/mods.toml"));
        for (var entry : Compatibility.SUPPORTED_VERSIONS.entrySet()) {
            var declaration = Pattern.compile("modId=\"" + Pattern.quote(entry.getKey())
                + "\"\\s+mandatory=false\\s+versionRange=\"([^\"]+)\"").matcher(metadata);
            assertTrue(declaration.find(), entry.getKey());
            var range = VersionRange.createFromVersionSpec(declaration.group(1));
            for (String version : entry.getValue()) {
                assertTrue(Compatibility.supported(entry.getKey(), version));
                assertTrue(range.containsVersion(new DefaultArtifactVersion(version)), entry.getKey() + " " + version);
            }
            for (String version : new String[]{"0.0.0", "999.0.0"}) {
                assertFalse(Compatibility.supported(entry.getKey(), version));
                assertFalse(range.containsVersion(new DefaultArtifactVersion(version)));
            }
            assertFalse(Compatibility.supported(entry.getKey(), "absent"));
        }
        assertFalse(Compatibility.supported("unknown_mod", "1.0.0"));
    }

    @Test void updatesDoNotAdmitUnauditedIntermediateOrFutureReleases() throws Exception {
        assertTrue(Compatibility.supported("cold_sweat", "2.4.3"));
        assertTrue(Compatibility.supported("cold_sweat", "2.4.3.2"));
        assertTrue(Compatibility.supported("sophisticatedcore", "1.5.1.2335"));
        assertTrue(Compatibility.supported("sophisticatedcore", "1.5.2.2346"));
        String metadata = Files.readString(Path.of("src/main/resources/META-INF/mods.toml"));
        for (var candidate : new String[][]{{"cold_sweat", "2.4.3.1"}, {"cold_sweat", "2.4.3.3"},
                {"sophisticatedcore", "1.5.2.2345"}, {"sophisticatedcore", "1.5.2.2347"}}) {
            assertFalse(Compatibility.supported(candidate[0], candidate[1]));
            var declaration = Pattern.compile("modId=\"" + candidate[0]
                + "\"\\s+mandatory=false\\s+versionRange=\"([^\"]+)\"").matcher(metadata);
            assertTrue(declaration.find());
            assertFalse(VersionRange.createFromVersionSpec(declaration.group(1))
                .containsVersion(new DefaultArtifactVersion(candidate[1])));
        }
    }
}
