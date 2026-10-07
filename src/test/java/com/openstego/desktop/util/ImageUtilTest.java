/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.util;

import com.openstego.desktop.OpenStego;
import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.plugin.dwtdugad.DWTDugadPlugin;
import com.openstego.desktop.plugin.randlsb.RandomLSBPlugin;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for alpha channel (transparency) handling and image quality, related to GitHub issue #58:
 * "PNG transparency of cover file lost after data hiding, and loss of image quality in digital watermarking"
 */
public class ImageUtilTest {

    private static final int WIDTH = 300;
    private static final int HEIGHT = 240;

    /**
     * Creates a PNG cover image with random colours. If transparent, the left third is fully transparent, the middle
     * third is semi-transparent and the right third is opaque.
     */
    private static byte[] createPng(boolean transparent) throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT,
                transparent ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        Random random = new Random(42);
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                int alpha = 0xFF;
                if (transparent) {
                    alpha = x < WIDTH / 3 ? 0x00 : (x < 2 * WIDTH / 3 ? 0x80 : 0xFF);
                }
                image.setRGB(x, y, (alpha << 24) | (random.nextInt() & 0x00FFFFFF));
            }
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", bos);
        return bos.toByteArray();
    }

    private static BufferedImage read(byte[] data) throws IOException {
        return ImageIO.read(new ByteArrayInputStream(data));
    }

    private static OpenStego dataHidingStego() throws OpenStegoException {
        RandomLSBPlugin plugin = new RandomLSBPlugin();
        plugin.resetConfig();
        plugin.getConfig().setUseEncryption(false);
        return new OpenStego(plugin, plugin.getConfig());
    }

    private static OpenStego watermarkingStego() throws OpenStegoException {
        DWTDugadPlugin plugin = new DWTDugadPlugin();
        plugin.resetConfig();
        plugin.getConfig().setPassword("secret");
        return new OpenStego(plugin, plugin.getConfig());
    }

    private static double psnr(BufferedImage a, BufferedImage b) {
        double squaredError = 0;
        long count = 0;
        for (int x = 0; x < a.getWidth(); x++) {
            for (int y = 0; y < a.getHeight(); y++) {
                int p = a.getRGB(x, y);
                int q = b.getRGB(x, y);
                for (int shift = 0; shift < 24; shift += 8) {
                    int diff = ((p >> shift) & 0xFF) - ((q >> shift) & 0xFF);
                    squaredError += diff * diff;
                    count++;
                }
            }
        }
        double mse = squaredError / count;
        return mse == 0 ? Double.POSITIVE_INFINITY : 10 * Math.log10(255.0 * 255.0 / mse);
    }

    @Test
    public void testHasAlpha() {
        assertTrue(ImageUtil.hasAlpha(new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB)));
        assertTrue(ImageUtil.hasAlpha(new BufferedImage(2, 2, BufferedImage.TYPE_4BYTE_ABGR)));
        assertFalse(ImageUtil.hasAlpha(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB)));
        assertFalse(ImageUtil.hasAlpha(null));
    }

    @Test
    public void testRemoveAlphaKeepsRgbBitsExactly() {
        BufferedImage image = new BufferedImage(2, 1, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, 0x00123457); // fully transparent
        image.setRGB(1, 0, 0x80ABCDEF); // semi-transparent

        BufferedImage rgb = ImageUtil.removeAlpha(image);

        assertEquals(BufferedImage.TYPE_INT_RGB, rgb.getType());
        assertEquals(0x123457, rgb.getRGB(0, 0) & 0x00FFFFFF);
        assertEquals(0xABCDEF, rgb.getRGB(1, 0) & 0x00FFFFFF);
    }

    @Test
    public void testDataHidingPreservesPngTransparency() throws Exception {
        byte[] cover = createPng(true);
        String message = "Issue #58 test message";
        OpenStego stego = dataHidingStego();

        byte[] stegoData = stego.embedData(message.getBytes(StandardCharsets.UTF_8), "msg.txt", cover, "cover.png",
                "stego.png");

        BufferedImage coverImage = read(cover);
        BufferedImage stegoImage = read(stegoData);
        assertTrue(stegoImage.getColorModel().hasAlpha(), "Stego image must keep its alpha channel");
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                assertEquals(coverImage.getRGB(x, y) >>> 24, stegoImage.getRGB(x, y) >>> 24,
                        "Alpha changed at (" + x + "," + y + ")");
            }
        }

        // Hidden data must still be extractable
        List<?> extracted = stego.extractData(stegoData, "stego.png");
        assertEquals("msg.txt", extracted.get(0));
        assertEquals(message, new String((byte[]) extracted.get(1), StandardCharsets.UTF_8));
    }

    @Test
    public void testDataHidingOpaqueImageUnchanged() throws Exception {
        byte[] cover = createPng(false);
        String message = "opaque";
        OpenStego stego = dataHidingStego();

        byte[] stegoData = stego.embedData(message.getBytes(StandardCharsets.UTF_8), "msg.txt", cover, "cover.png",
                "stego.png");

        assertFalse(read(stegoData).getColorModel().hasAlpha());
        List<?> extracted = stego.extractData(stegoData, "stego.png");
        assertEquals(message, new String((byte[]) extracted.get(1), StandardCharsets.UTF_8));
    }

    @Test
    public void testDataHidingTransparentCoverToBmp() throws Exception {
        // BMP cannot store transparency, but hiding data must still work
        byte[] cover = createPng(true);
        String message = "bmp output";
        OpenStego stego = dataHidingStego();

        byte[] stegoData = stego.embedData(message.getBytes(StandardCharsets.UTF_8), "msg.txt", cover, "cover.png",
                "stego.bmp");

        List<?> extracted = stego.extractData(stegoData, "stego.bmp");
        assertEquals(message, new String((byte[]) extracted.get(1), StandardCharsets.UTF_8));
    }

    @Test
    public void testWatermarkTransparentCoverToBmpDoesNotFail() throws Exception {
        // Previously failed with "Image can not be encoded with compression type BI_RGB and 32 bits per pixel"
        byte[] cover = createPng(true);
        OpenStego stego = watermarkingStego();
        byte[] signature = stego.generateSignature();

        byte[] stegoData = stego.embedMark(signature, "sig.sig", cover, "cover.png", "stego.bmp");

        assertNotNull(read(stegoData));
        assertTrue(stego.checkMark(stegoData, "stego.bmp", signature) > 0.5, "Watermark must still be detected");
    }

    @Test
    public void testWatermarkPreservesPngTransparency() throws Exception {
        byte[] cover = createPng(true);
        OpenStego stego = watermarkingStego();
        byte[] signature = stego.generateSignature();

        byte[] stegoData = stego.embedMark(signature, "sig.sig", cover, "cover.png", "stego.png");

        BufferedImage coverImage = read(cover);
        BufferedImage stegoImage = read(stegoData);
        assertTrue(stegoImage.getColorModel().hasAlpha());
        assertEquals(coverImage.getRGB(0, 0) >>> 24, stegoImage.getRGB(0, 0) >>> 24);
        assertEquals(coverImage.getRGB(WIDTH / 2, 0) >>> 24, stegoImage.getRGB(WIDTH / 2, 0) >>> 24);
    }

    @Test
    public void testYuvRoundTripQuality() {
        // Converting RGB -> YUV -> RGB without any modification should be (almost) lossless
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
        Random random = new Random(7);
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 64; y++) {
                image.setRGB(x, y, random.nextInt() & 0x00FFFFFF);
            }
        }

        BufferedImage roundTrip = ImageUtil.getImageFromYuv(ImageUtil.getYuvFromImage(image), BufferedImage.TYPE_INT_RGB);

        // Before the fix (truncation instead of rounding) this was around 45 dB
        assertTrue(psnr(image, roundTrip) > 50.0, "YUV round trip PSNR too low: " + psnr(image, roundTrip));
    }
}
