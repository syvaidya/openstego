/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CommonUtil#parseFileList(String, String)}, related to GitHub issue #60:
 * "Support semicolons in filenames"
 */
public class CommonUtilTest {

    private static File createTempDir() throws IOException {
        File dir = Files.createTempDirectory("openstego-test").toFile();
        dir.deleteOnExit();
        return dir;
    }

    private static File createFile(File dir, String name) throws IOException {
        File file = new File(dir, name);
        assertTrue(file.createNewFile(), "Could not create test file " + name);
        file.deleteOnExit();
        return file;
    }

    private static List<String> names(List<File> files) {
        List<String> names = new ArrayList<>();
        for (File file : files) {
            names.add(file.getName());
        }
        Collections.sort(names);
        return names;
    }

    @Test
    public void testNullInput() {
        assertTrue(CommonUtil.parseFileList(null, ";").isEmpty());
    }

    @Test
    public void testSingleFileWithSemicolonInName() throws IOException {
        File dir = createTempDir();
        File file = createFile(dir, "holiday;2023.png");

        List<File> result = CommonUtil.parseFileList(file.getPath(), ";");

        assertEquals(1, result.size());
        assertEquals("holiday;2023.png", result.get(0).getName());
        assertTrue(result.get(0).isFile());
    }

    @Test
    public void testFileWithMultipleSemicolonsInName() throws IOException {
        File dir = createTempDir();
        File file = createFile(dir, "a;b;c.png");

        List<File> result = CommonUtil.parseFileList(file.getPath(), ";");

        assertEquals(1, result.size());
        assertEquals("a;b;c.png", result.get(0).getName());
    }

    @Test
    public void testMultipleNormalFiles() throws IOException {
        File dir = createTempDir();
        File first = createFile(dir, "first.png");
        File second = createFile(dir, "second.png");

        List<File> result = CommonUtil.parseFileList(first.getPath() + ";" + second.getPath(), ";");

        assertEquals(2, result.size());
        assertEquals(Arrays.asList("first.png", "second.png"), names(result));
    }

    @Test
    public void testMixOfSemicolonAndNormalFiles() throws IOException {
        // This is how the GUI file chooser builds the list when several files are selected
        File dir = createTempDir();
        File withSemicolon = createFile(dir, "img;001.png");
        File normal = createFile(dir, "img002.png");

        List<File> result = CommonUtil.parseFileList(withSemicolon.getPath() + ";" + normal.getPath(), ";");

        assertEquals(2, result.size());
        assertEquals(Arrays.asList("img002.png", "img;001.png"), names(result));
    }

    @Test
    public void testWildcardsStillWork() throws IOException {
        File dir = createTempDir();
        createFile(dir, "one.png");
        createFile(dir, "two.png");
        createFile(dir, "three;x.png");
        createFile(dir, "notes.txt");

        List<File> result = CommonUtil.parseFileList(dir.getPath() + File.separator + "*.png", ";");

        assertEquals(Arrays.asList("one.png", "three;x.png", "two.png"), names(result));
    }

    @Test
    public void testNonExistingFileReturnsEmpty() throws IOException {
        File dir = createTempDir();

        List<File> result = CommonUtil.parseFileList(dir.getPath() + File.separator + "missing;file.png", ";");

        assertTrue(result.isEmpty());
    }
}
