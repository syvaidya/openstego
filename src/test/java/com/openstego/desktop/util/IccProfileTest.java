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
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ICC colour profile handling, related to GitHub issue #62:
 * "ICC profile is removed after embedding watermark"
 */
public class IccProfileTest {

    private static final int WIDTH = 320;
    private static final int HEIGHT = 240;

    /**
     * A non-sRGB colour profile (linear RGB) which is available in every JDK
     */
    private static final ICC_Profile PROFILE = ICC_Profile.getInstance(ColorSpace.CS_LINEAR_RGB);

    /**
     * Pixel values of the test cover image (same values every time)
     */
    private static WritableRaster createPixels(ColorModel cm) {
        WritableRaster raster = cm.createCompatibleWritableRaster(WIDTH, HEIGHT);
        Random random = new Random(11);
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                for (int band = 0; band < 3; band++) {
                    raster.setSample(x, y, band, (x + y * band + random.nextInt(40)) & 0xFF);
                }
            }
        }
        return raster;
    }

    /**
     * Creates a cover image (PNG or JPEG) which has the test ICC profile embedded in it
     */
    private static byte[] createCoverWithProfile(String format) throws IOException {
        if ("png".equals(format)) {
            // Write the pixel values as a plain PNG, then insert an iCCP chunk (with the profile) after the IHDR chunk
            ColorModel cm = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR).getColorModel();
            BufferedImage image = new BufferedImage(cm, createPixels(cm), false, null);
            ByteArrayOutputStream png = new ByteArrayOutputStream();
            assertTrue(ImageIO.write(image, "png", png));
            byte[] data = png.toByteArray();
            int ihdrEnd = 8 + 4 + 4 + 13 + 4; // signature + IHDR chunk (length, type, data, CRC)

            ByteArrayOutputStream chunkData = new ByteArrayOutputStream();
            chunkData.write("ICC profile".getBytes(StandardCharsets.ISO_8859_1));
            chunkData.write(0); // null separator
            chunkData.write(0); // compression method: deflate
            Deflater deflater = new Deflater();
            deflater.setInput(PROFILE.getData());
            deflater.finish();
            byte[] buffer = new byte[4096];
            while (!deflater.finished()) {
                chunkData.write(buffer, 0, deflater.deflate(buffer));
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(data, 0, ihdrEnd);
            writePngChunk(out, "iCCP", chunkData.toByteArray());
            out.write(data, ihdrEnd, data.length - ihdrEnd);
            return out.toByteArray();
        }

        // Java's JPEG writer embeds the ICC profile of the image's colour space (APP2 marker)
        ColorModel cm = new ComponentColorModel(new ICC_ColorSpace(PROFILE), false, false, Transparency.OPAQUE,
                DataBuffer.TYPE_BYTE);
        BufferedImage image = new BufferedImage(cm, createPixels(cm), false, null);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(image, format, bos));
        return bos.toByteArray();
    }

    private static void writePngChunk(ByteArrayOutputStream out, String type, byte[] data) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        byte[] typeBytes = type.getBytes(StandardCharsets.ISO_8859_1);
        CRC32 crc = new CRC32();
        crc.update(typeBytes);
        crc.update(data);
        dos.writeInt(data.length);
        dos.write(typeBytes);
        dos.write(data);
        dos.writeInt((int) crc.getValue());
    }

    /**
     * Reads the ICC profile embedded in the given image file data
     */
    private static ICC_Profile readProfile(byte[] imageData) throws IOException {
        try (ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(imageData))) {
            ImageReader reader = ImageIO.getImageReaders(in).next();
            reader.setInput(in);
            return ImageUtil.getIccProfile(reader.getImageMetadata(0));
        }
    }

    private static void assertHasTestProfile(byte[] imageData) throws IOException {
        ICC_Profile profile = readProfile(imageData);
        assertNotNull(profile, "ICC profile is missing");
        assertTrue(Arrays.equals(PROFILE.getData(), profile.getData()), "ICC profile is different");
    }

    private static OpenStego watermarkingStego() throws OpenStegoException {
        DWTDugadPlugin plugin = new DWTDugadPlugin();
        plugin.resetConfig();
        plugin.getConfig().setPassword("secret");
        return new OpenStego(plugin, plugin.getConfig());
    }

    @Test
    public void testNoProfileForPlainImage() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB), "png", bos);
        assertNull(readProfile(bos.toByteArray()));
    }

    @Test
    public void testWatermarkJpegKeepsProfile() throws Exception {
        UserPreferences.init();
        byte[] cover = createCoverWithProfile("jpg");
        assertHasTestProfile(cover);
        OpenStego stego = watermarkingStego();
        byte[] signature = stego.generateSignature();

        byte[] output = stego.embedMark(signature, "sig.sig", cover, "cover.jpg", "output.jpg");

        assertHasTestProfile(output);
        assertTrue(stego.checkMark(output, "output.jpg", signature) > 0.5, "Watermark must still be detected");
    }

    @Test
    public void testWatermarkJpegToPngKeepsProfile() throws Exception {
        byte[] cover = createCoverWithProfile("jpg");
        OpenStego stego = watermarkingStego();
        byte[] signature = stego.generateSignature();

        byte[] output = stego.embedMark(signature, "sig.sig", cover, "cover.jpg", "output.png");

        assertHasTestProfile(output);
        assertTrue(stego.checkMark(output, "output.png", signature) > 0.5);
    }

    @Test
    public void testWatermarkPngKeepsProfile() throws Exception {
        byte[] cover = createCoverWithProfile("png");
        assertHasTestProfile(cover);
        OpenStego stego = watermarkingStego();
        byte[] signature = stego.generateSignature();

        byte[] output = stego.embedMark(signature, "sig.sig", cover, "cover.png", "output.png");

        assertHasTestProfile(output);
    }

    @Test
    public void testWatermarkPngToJpegDoesNotFail() throws Exception {
        // Previously failed with "Unsupported format name: javax_imageio_png_1.0"
        UserPreferences.init();
        byte[] cover = createCoverWithProfile("png");
        OpenStego stego = watermarkingStego();
        byte[] signature = stego.generateSignature();

        byte[] output = stego.embedMark(signature, "sig.sig", cover, "cover.png", "output.jpg");

        assertHasTestProfile(output);
        assertTrue(stego.checkMark(output, "output.jpg", signature) > 0.5);
    }

    @Test
    public void testDataHidingJpegCoverKeepsProfile() throws Exception {
        byte[] cover = createCoverWithProfile("jpg");
        RandomLSBPlugin plugin = new RandomLSBPlugin();
        plugin.resetConfig();
        plugin.getConfig().setUseEncryption(false);
        OpenStego stego = new OpenStego(plugin, plugin.getConfig());
        String message = "Issue #62 test";

        byte[] output = stego.embedData(message.getBytes(StandardCharsets.UTF_8), "msg.txt", cover, "cover.jpg",
                "output.png");

        assertHasTestProfile(output);
        List<?> extracted = stego.extractData(output, "output.png");
        assertEquals(message, new String((byte[]) extracted.get(1), StandardCharsets.UTF_8));
    }

    @Test
    public void testPixelValuesNotColourConverted() throws Exception {
        // Reading a JPEG with an ICC profile must return the stored pixel values, not values converted to sRGB
        byte[] cover = createCoverWithProfile("jpg");
        WritableRaster expected = createPixels(new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR).getColorModel());

        double openStegoDifference = averageDifference(expected, ImageUtil.byteArrayToImage(cover, "c.jpg").getImage());
        double convertedDifference = averageDifference(expected, ImageIO.read(new ByteArrayInputStream(cover)));

        // Some difference is expected due to JPEG compression, but a colour conversion changes the values much more
        assertTrue(openStegoDifference < convertedDifference / 2,
                "Pixel values were changed: " + openStegoDifference + " vs " + convertedDifference);
    }

    private static double averageDifference(Raster expected, BufferedImage image) {
        long total = 0;
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                int rgb = image.getRGB(x, y);
                total += Math.abs(((rgb >> 16) & 0xFF) - expected.getSample(x, y, 0));
                total += Math.abs(((rgb >> 8) & 0xFF) - expected.getSample(x, y, 1));
                total += Math.abs((rgb & 0xFF) - expected.getSample(x, y, 2));
            }
        }
        return total / (3.0 * WIDTH * HEIGHT);
    }
}
