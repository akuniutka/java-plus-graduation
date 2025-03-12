package ru.practicum.ewm.util;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;

public final class TestUtils {

    private TestUtils() {
    }

    public static String loadJson(final String filename, final Class<?> clazz) throws IOException {
        final String expandedFilename = clazz.getSimpleName().toLowerCase() + "/" + filename;
        final ClassPathResource resource = new ClassPathResource(expandedFilename, clazz);
        return Files.readString(resource.getFile().toPath());
    }
}
