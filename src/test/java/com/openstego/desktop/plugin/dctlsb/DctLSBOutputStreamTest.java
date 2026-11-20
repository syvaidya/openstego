/*
 * Steganography utility to hide messages into cover files
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.plugin.dctlsb;

import com.openstego.desktop.OpenStegoConfig;
import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.plugin.template.dct.DCTPluginTemplate;
import com.openstego.desktop.util.ImageHolder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DctLSBOutputStreamTest {
    @BeforeAll
    public static void initializeErrorLabels() throws ClassNotFoundException {
        Class.forName(DCTPluginTemplate.class.getName());
    }

    @Test
    public void testBlackPngRecoversPayload() throws Exception {
        byte[] payload = new byte[32];
        new Random(173).nextBytes(payload);
        for (int type : new int[]{BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_INT_ARGB,
                BufferedImage.TYPE_BYTE_GRAY}) {
            try (DctLSBOutputStream writer = writer(payload.length, type)) {
                writer.write(payload);
                BufferedImage image = writer.getImage().getImage();
                assertEquals(type, image.getType());
                assertPngPayload(image, payload);
            }
        }
    }

    @Test
    public void testUnstableHeaderFails() {
        OpenStegoException failure = assertThrows(OpenStegoException.class,
                () -> writer(32, BufferedImage.TYPE_BYTE_BINARY));
        assertTrue(failure.getCause() instanceof IOException);
        assertTrue(failure.getCause().getMessage().contains("Cannot preserve a DctLSB bit"));
    }

    @Test
    public void testIncompletePayloadDoesNotReturnImage() throws Exception {
        byte[] payload = {42};
        try (DctLSBOutputStream writer = writer(payload.length, BufferedImage.TYPE_INT_RGB)) {
            assertThrows(OpenStegoException.class, writer::getImage);
            writer.write(payload);
            assertPngPayload(writer.getImage().getImage(), payload);
        }
    }

    @Test
    public void testFailedPayloadDoesNotReturnImage() throws Exception {
        BufferedImage cover = new BufferedImage(256, 256, BufferedImage.TYPE_BYTE_INDEXED);
        for (int y = 0; y < cover.getHeight(); y++) {
            for (int x = 0; x < cover.getWidth(); x++) {
                cover.setRGB(x, y, 0xff00ff00);
            }
        }
        byte[] payload = new byte[100];
        new Random(173).nextBytes(payload);
        try (DctLSBOutputStream writer = new DctLSBOutputStream(new ImageHolder(cover, null),
                payload.length, "x.bin", config(), BufferedImage.TYPE_BYTE_INDEXED)) {
            IOException failure = assertThrows(IOException.class, () -> writer.write(payload));
            assertTrue(failure.getMessage().contains("Cannot preserve a DctLSB bit"));
            assertThrows(OpenStegoException.class, writer::getImage);
            assertThrows(IOException.class, () -> writer.write(0));
        }
    }

    private static DctLSBOutputStream writer(int length, int type) throws OpenStegoException {
        BufferedImage cover = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        return new DctLSBOutputStream(new ImageHolder(cover, null), length, "x.bin", config(), type);
    }

    private static void assertPngPayload(BufferedImage image, byte[] payload) throws Exception {
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(image, "png", png));
        BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(png.toByteArray()));
        try (DctLSBInputStream reader = new DctLSBInputStream(new ImageHolder(decoded, null), config())) {
            assertEquals("x.bin", reader.getDataHeader().getFileName());
            byte[] recovered = new byte[payload.length];
            assertEquals(payload.length, reader.read(recovered));
            assertArrayEquals(payload, recovered);
        }
    }

    private static OpenStegoConfig config() {
        OpenStegoConfig config = new OpenStegoConfig();
        config.setPassword("review-fixture");
        config.setUseCompression(false);
        config.setUseEncryption(false);
        return config;
    }
}
