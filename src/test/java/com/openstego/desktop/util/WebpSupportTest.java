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
import javax.imageio.ImageWriter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WebP image format support, related to GitHub issue #63: "Support for webp format"
 */
public class WebpSupportTest {

    private static final int WIDTH = 300;
    private static final int HEIGHT = 200;

    private static BufferedImage createImage() {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Random random = new Random(63);
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                image.setRGB(x, y, random.nextInt() & 0x00FFFFFF);
            }
        }
        return image;
    }

    private static byte[] toPng(BufferedImage image) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(image, "png", bos));
        return bos.toByteArray();
    }

    private static OpenStego dataHidingStego() throws OpenStegoException {
        RandomLSBPlugin plugin = new RandomLSBPlugin();
        plugin.resetConfig();
        plugin.getConfig().setUseEncryption(false);
        return new OpenStego(plugin, plugin.getConfig());
    }

    @Test
    public void testWebpReaderAndWriterAvailable() {
        assertTrue(Arrays.asList(ImageIO.getReaderFormatNames()).contains("webp"), "No WebP image reader found");
        assertTrue(Arrays.asList(ImageIO.getWriterFormatNames()).contains("webp"), "No WebP image writer found");
    }

    @Test
    public void testWebpListedAsSupportedFormat() throws OpenStegoException {
        RandomLSBPlugin dataHiding = new RandomLSBPlugin();
        assertTrue(dataHiding.getReadableFileExtensions().contains("webp"));
        // WebP must be offered for data hiding output, as it supports lossless compression
        assertTrue(dataHiding.getWritableFileExtensions().contains("webp"));

        DWTDugadPlugin watermarking = new DWTDugadPlugin();
        assertTrue(watermarking.getReadableFileExtensions().contains("webp"));
        assertTrue(watermarking.getWritableFileExtensions().contains("webp"));
    }

    @Test
    public void testWebpIsWrittenLosslessly() throws Exception {
        BufferedImage image = createImage();

        byte[] webp = ImageUtil.imageToByteArray(new ImageHolder(image, null), "image.webp", new RandomLSBPlugin());
        BufferedImage decoded = ImageUtil.byteArrayToImage(webp, "image.webp").getImage();

        assertEquals(WIDTH, decoded.getWidth());
        assertEquals(HEIGHT, decoded.getHeight());
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                assertEquals(image.getRGB(x, y) & 0x00FFFFFF, decoded.getRGB(x, y) & 0x00FFFFFF,
                        "Pixel changed at (" + x + "," + y + ") - WebP was not written losslessly");
            }
        }
    }

    @Test
    public void testHideAndExtractDataWithWebpOutput() throws Exception {
        String message = "Issue #63: hidden inside a WebP image";
        OpenStego stego = dataHidingStego();

        byte[] stegoData = stego.embedData(message.getBytes(StandardCharsets.UTF_8), "msg.txt",
                toPng(createImage()), "cover.png", "stego.webp");

        assertNotNull(ImageIO.read(new ByteArrayInputStream(stegoData)), "Output is not a readable WebP image");
        List<?> extracted = stego.extractData(stegoData, "stego.webp");
        assertEquals("msg.txt", extracted.get(0));
        assertEquals(message, new String((byte[]) extracted.get(1), StandardCharsets.UTF_8));
    }

    @Test
    public void testWebpCoverImage() throws Exception {
        // Use a WebP image as the cover file and write the stego image as PNG
        byte[] webpCover = ImageUtil.imageToByteArray(new ImageHolder(createImage(), null), "cover.webp",
                new RandomLSBPlugin());
        String message = "WebP cover";
        OpenStego stego = dataHidingStego();

        byte[] stegoData = stego.embedData(message.getBytes(StandardCharsets.UTF_8), "msg.txt", webpCover,
                "cover.webp", "stego.png");

        List<?> extracted = stego.extractData(stegoData, "stego.png");
        assertEquals(message, new String((byte[]) extracted.get(1), StandardCharsets.UTF_8));
    }

    @Test
    public void testWatermarkWithWebp() throws Exception {
        DWTDugadPlugin plugin = new DWTDugadPlugin();
        plugin.resetConfig();
        plugin.getConfig().setPassword("secret");
        OpenStego stego = new OpenStego(plugin, plugin.getConfig());
        byte[] signature = stego.generateSignature();

        byte[] watermarked = stego.embedMark(signature, "sig.sig", toPng(createImage()), "cover.png", "output.webp");

        assertTrue(stego.checkMark(watermarked, "output.webp", signature) > 0.5, "Watermark must be detected");
    }

    @Test
    public void testLosslessParamNotAvailableForJpeg() {
        ImageWriter jpegWriter = ImageIO.getImageWritersByFormatName("jpeg").next();
        assertThrows(IOException.class, () -> ImageUtil.getLosslessWriteParam(jpegWriter));
    }
}
