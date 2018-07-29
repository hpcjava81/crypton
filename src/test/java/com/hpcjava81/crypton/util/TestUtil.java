package com.hpcjava81.crypton.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestUtil {

    public static void deleteFilesIn(Path path) throws IOException {
        File[] files = path.toFile().listFiles();
        if (files != null) {
            for (File file : files) {
                Files.delete(file.toPath());
            }
        }

    }

}
