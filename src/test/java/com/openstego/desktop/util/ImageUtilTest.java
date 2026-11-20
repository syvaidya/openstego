/*
 * Steganography utility to hide messages into cover files
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.util;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImageUtilTest {
    @Test
    public void testYuvWithoutAlphaIsOpaque() {
        int[][] y = {{0, 100, 255}, {64, 128, 200}};
        int[][] u = {{0, 0, 0}, {10, -20, 127}};
        int[][] v = {{0, 0, 0}, {20, -30, -128}};
        int[] expected = {0xff000000, 0xff646464, 0xffffffff, 0xff563054, 0xff5d9957, 0xff36e0ff};

        for (int type : new int[]{0, BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_INT_ARGB}) {
            BufferedImage image = ImageUtil.getImageFromYuv(Arrays.asList(y, u, v), type);

            assertEquals(3, image.getWidth());
            assertEquals(2, image.getHeight());
            assertEquals(type == 0 ? BufferedImage.TYPE_INT_RGB : type, image.getType());
            assertArrayEquals(expected, image.getRGB(0, 0, 3, 2, null, 0, 3));
        }
    }

    @Test
    public void testYuvPreservesSuppliedAlpha() {
        int[][] y = {{0, 100, 255}, {64, 128, 200}};
        int[][] u = {{0, 0, 0}, {10, -20, 127}};
        int[][] v = {{0, 0, 0}, {20, -30, -128}};
        int[][] alpha = {{0, 1, 17}, {127, 254, 255}};

        BufferedImage image = ImageUtil.getImageFromYuv(Arrays.asList(y, u, v, alpha), BufferedImage.TYPE_INT_ARGB);

        assertArrayEquals(new int[]{0x00000000, 0x01646464, 0x11ffffff, 0x7f563054, 0xfe5d9957, 0xff36e0ff},
                image.getRGB(0, 0, 3, 2, null, 0, 3));
    }
}
