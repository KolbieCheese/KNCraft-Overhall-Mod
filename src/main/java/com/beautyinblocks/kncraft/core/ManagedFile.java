package com.beautyinblocks.kncraft.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.HexFormat;

/** Update only files whose previous installed bytes are still intact. */
public final class ManagedFile {
    public static String hash(byte[] bytes) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)); }
        catch (java.security.NoSuchAlgorithmException ex) { throw new AssertionError(ex); }
    }
    public static boolean install(Path path, byte[] content, String previousHash) throws IOException {
        if (Files.exists(path)) {
            String current = hash(Files.readAllBytes(path));
            if (current.equals(hash(content))) return true;
            if (!current.equals(previousHash)) return false;
        }
        Files.createDirectories(path.getParent());
        Path temp = Files.createTempFile(path.getParent(), ".kncraft-", ".tmp");
        try {
            Files.write(temp, content);
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
        } finally { Files.deleteIfExists(temp); }
        return true;
    }
    private ManagedFile() {}
}
