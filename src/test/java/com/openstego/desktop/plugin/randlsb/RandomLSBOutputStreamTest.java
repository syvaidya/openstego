/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.plugin.randlsb;

import org.junit.Test;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;

import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.plugin.lsb.LSBConfig;
import com.openstego.desktop.plugin.lsb.LSBDataHeader;
import com.openstego.desktop.plugin.lsb.LSBErrors;
import com.openstego.desktop.plugin.lsb.LSBPlugin;
import com.openstego.desktop.util.ImageHolder;

import org.junit.Assert;
import org.junit.Before;

/**
 * Unit test class for {@link com.openstego.desktop.plugin.randlsb.RandomLSBOutputStream}
 */
public class RandomLSBOutputStreamTest {

    @Before
    public void setup() {
        RandomLSBPlugin plugin = new RandomLSBPlugin();
        Assert.assertNotNull(plugin);
    }

    @Test
    public void testBestCase() throws Exception {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageHolder holder = new ImageHolder(image, null);
        LSBConfig config = new LSBConfig();
        String msg = "abcde";

        try (RandomLSBOutputStream os = new RandomLSBOutputStream(holder, 5, "test.txt", config)) {
            Assert.assertNotNull(os);
            // Write simple message
            os.write(msg.getBytes(StandardCharsets.UTF_8));
            os.flush();

            holder = os.getImage();
        }

        // Extract data back using RandomLSBInputStream and compare with original
        try (RandomLSBInputStream is = new RandomLSBInputStream(holder, new LSBConfig())) {
            LSBDataHeader header = is.getDataHeader();
            Assert.assertEquals(1, header.getChannelBitsUsed());
            Assert.assertEquals(5, header.getDataLength());
            Assert.assertEquals("test.txt", header.getFileName());
            byte[] extMsg = new byte[5];
            is.read(extMsg);
            Assert.assertEquals(msg, new String(extMsg, StandardCharsets.UTF_8));
        }
    }

    @Test
    public void testNullImage() throws Exception {
        ImageHolder holder = new ImageHolder(null, null);
        try (RandomLSBOutputStream os = new RandomLSBOutputStream(holder, 100, "test.txt", null)) {
        } catch (OpenStegoException e) {
            Assert.assertEquals(LSBPlugin.NAMESPACE, e.getNamespace());
            Assert.assertEquals(LSBErrors.NULL_IMAGE_ARGUMENT, e.getErrorCode());

            // Try with null holder
            try (RandomLSBOutputStream os = new RandomLSBOutputStream(null, 100, "test.txt", null)) {
            } catch (OpenStegoException oe) {
                Assert.assertEquals(LSBPlugin.NAMESPACE, oe.getNamespace());
                Assert.assertEquals(LSBErrors.NULL_IMAGE_ARGUMENT, oe.getErrorCode());
                return;
            }
        }
        Assert.fail("Exception not thrown");
    }

    @Test
    public void testNonRGBImage() throws Exception {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        ImageHolder holder = new ImageHolder(image, null);
        LSBConfig config = new LSBConfig();
        try (RandomLSBOutputStream os = new RandomLSBOutputStream(holder, 100, "test.txt", config)) {
            // Image type should be converted to RGB
            Assert.assertEquals(BufferedImage.TYPE_INT_RGB, os.getImage().getImage().getType());
            Assert.assertNull(os.getImage().getMetadata());
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
            Assert.assertNotNull(os);
        }
        try (RandomLSBOutputStream os = new RandomLSBOutputStream(holder, 12000, "test.txt", config)) {
        } catch (OpenStegoException oe) {
            Assert.assertEquals(LSBPlugin.NAMESPACE, oe.getNamespace());
            Assert.assertEquals(LSBErrors.IMAGE_SIZE_INSUFFICIENT, oe.getErrorCode());
            return;
        }
        Assert.fail("Exception not thrown");
    }

}
