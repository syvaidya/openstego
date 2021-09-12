/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.plugin.randlsb;

import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.plugin.lsb.LSBConfig;
import com.openstego.desktop.plugin.lsb.LSBDataHeader;
import com.openstego.desktop.plugin.lsb.LSBErrors;
import com.openstego.desktop.plugin.lsb.LSBPlugin;
import com.openstego.desktop.util.ImageHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test class for {@link com.openstego.desktop.plugin.randlsb.RandomLSBOutputStream}
 */
public class RandomLSBOutputStreamTest {

    @BeforeEach
    public void setup() {
        RandomLSBPlugin plugin = new RandomLSBPlugin();
        assertNotNull(plugin);
    }

    @Test
    public void testBestCase() throws Exception {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageHolder holder = new ImageHolder(image, null);
        LSBConfig config = new LSBConfig();
        String msg = "abcde";

        try (RandomLSBOutputStream os = new RandomLSBOutputStream(holder, 5, "test.txt", config)) {
            assertNotNull(os);
            // Write simple message
            os.write(msg.getBytes(StandardCharsets.UTF_8));
            os.flush();

            holder = os.getImage();
        }

        // Extract data back using RandomLSBInputStream and compare with original
        try (RandomLSBInputStream is = new RandomLSBInputStream(holder, new LSBConfig())) {
            LSBDataHeader header = is.getDataHeader();
            assertEquals(1, header.getChannelBitsUsed());
            assertEquals(5, header.getDataLength());
            assertEquals("test.txt", header.getFileName());
            byte[] extMsg = new byte[5];
            int n = is.read(extMsg);
            assertEquals(msg.length(), n);
            assertEquals(msg, new String(extMsg, StandardCharsets.UTF_8));
        }
    }

    @Test
    public void testNullImage() throws Exception {
        ImageHolder holder = new ImageHolder(null, null);
        try (RandomLSBOutputStream ignored = new RandomLSBOutputStream(holder, 100, "test.txt", null)) {
            fail("Did not throw OpenStegoException");
        } catch (OpenStegoException e) {
            assertEquals(LSBPlugin.NAMESPACE, e.getNamespace());
            assertEquals(LSBErrors.NULL_IMAGE_ARGUMENT, e.getErrorCode());

            // Try with null holder
            try (RandomLSBOutputStream ignored = new RandomLSBOutputStream(null, 100, "test.txt", null)) {
                fail("Did not throw OpenStegoException");
            } catch (OpenStegoException oe) {
                assertEquals(LSBPlugin.NAMESPACE, oe.getNamespace());
                assertEquals(LSBErrors.NULL_IMAGE_ARGUMENT, oe.getErrorCode());
            }
        }
    }

    @Test
    public void testNonRGBImage() throws Exception {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        ImageHolder holder = new ImageHolder(image, null);
        LSBConfig config = new LSBConfig();
        try (RandomLSBOutputStream os = new RandomLSBOutputStream(holder, 100, "test.txt", config)) {
            // Image type should be converted to RGB
            assertEquals(BufferedImage.TYPE_INT_RGB, os.getImage().getImage().getType());
            assertNull(os.getImage().getMetadata());
        }
    }

    @Test
    public void testImageCapacity() throws Exception {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        ImageHolder holder = new ImageHolder(image, null);
        LSBConfig config = new LSBConfig();
        // With 100x100 image and 3bits per channel used for data, approximately 90000/8 bytes can be embedded
        // Check that 11k bytes are ok, but 12k bytes fails
        try (RandomLSBOutputStream os = new RandomLSBOutputStream(holder, 11000, "test.txt", config)) {
            assertNotNull(os);
        }
        try (RandomLSBOutputStream ignored = new RandomLSBOutputStream(holder, 12000, "test.txt", config)) {
            fail("Did not throw OpenStegoException");
        } catch (OpenStegoException oe) {
            assertEquals(LSBPlugin.NAMESPACE, oe.getNamespace());
            assertEquals(LSBErrors.IMAGE_SIZE_INSUFFICIENT, oe.getErrorCode());
        }
    }

}
