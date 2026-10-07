/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.util;

import com.openstego.desktop.OpenStego;
import com.openstego.desktop.OpenStegoErrors;
import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.OpenStegoPlugin;
import org.w3c.dom.Node;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageInputStream;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.*;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Image utilities
 */
public class ImageUtil {

    /**
     * Constructor is private so that this class is not instantiated
     */
    private ImageUtil() {
    }

    /**
     * Default image type in case not provided
     */
    public static final String DEFAULT_IMAGE_TYPE = "png";

    /**
     * Method to generate a random image filled with noise.
     *
     * @param numOfPixels Number of pixels required in the image
     * @return Random image filled with noise
     * @throws OpenStegoException Processing issues
     */
    public static ImageHolder generateRandomImage(int numOfPixels) throws OpenStegoException {
        final double ASPECT_RATIO = 4.0 / 3.0;
        int width;
        int height;
        byte[] rgbValue = new byte[3];
        BufferedImage image;
        SecureRandom random;

        try {
            random = SecureRandom.getInstance("SHA1PRNG");

            width = (int) Math.ceil(Math.sqrt(numOfPixels * ASPECT_RATIO));
            height = (int) Math.ceil(numOfPixels / (double) width);

            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    random.nextBytes(rgbValue);
                    image.setRGB(x, y, CommonUtil.byteToInt(rgbValue[0]) +
                            (CommonUtil.byteToInt(rgbValue[1]) << 8) +
                            (CommonUtil.byteToInt(rgbValue[2]) << 16));
                }
            }

            return new ImageHolder(image, null);
        } catch (NoSuchAlgorithmException nsaEx) {
            throw new OpenStegoException(nsaEx);
        }
    }

    /**
     * Method to convert BufferedImage to byte array
     *
     * @param image         Image data
     * @param imageFileName Name of the image file
     * @param plugin        Reference to the plugin
     * @return Image data as byte array
     * @throws OpenStegoException Processing issues
     */
    public static byte[] imageToByteArray(ImageHolder image, String imageFileName, OpenStegoPlugin<?> plugin) throws OpenStegoException {
        ByteArrayOutputStream barrOS = new ByteArrayOutputStream();
        String imageType;

        if (imageFileName != null) {
            imageType = imageFileName.substring(imageFileName.lastIndexOf('.') + 1).toLowerCase();
            if (!plugin.getWritableFileExtensions().contains(imageType)) {
                throw new OpenStegoException(null, OpenStego.NAMESPACE, OpenStegoErrors.IMAGE_TYPE_INVALID, imageType);
            }
            if (imageType.equals("jp2")) {
                imageType = "jpeg 2000";
            }
            writeImage(image, imageType, barrOS);
        } else {
            writeImage(image, DEFAULT_IMAGE_TYPE, barrOS);
        }
        return barrOS.toByteArray();
    }

    /**
     * Method to convert byte array to image
     *
     * @param imageData   Image data as byte array
     * @param imgFileName Name of the image file
     * @return Buffered image
     * @throws OpenStegoException Processing issues
     */
    public static ImageHolder byteArrayToImage(byte[] imageData, String imgFileName) throws OpenStegoException {
        if (imageData == null) {
            return null;
        }

        ImageHolder image = readImage(new ByteArrayInputStream(imageData));
        if (image == null) {
            throw new OpenStegoException(null, OpenStego.NAMESPACE, OpenStegoErrors.IMAGE_FILE_INVALID, imgFileName);
        }
        return image;
    }

    /**
     * Get RGB data array from given image
     *
     * @param image Image
     * @return List with three elements of two-dimensional int's - R, G and B
     */
    @SuppressWarnings("unused")
    public static List<int[][]> getRgbFromImage(BufferedImage image) {
        List<int[][]> rgb = new ArrayList<>();
        int[][] r;
        int[][] g;
        int[][] b;
        int width;
        int height;

        width = image.getWidth();
        height = image.getHeight();

        r = new int[height][width];
        g = new int[height][width];
        b = new int[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                r[i][j] = (image.getRGB(j, i) >> 16) & 0xFF;
                g[i][j] = (image.getRGB(j, i) >> 8) & 0xFF;
                b[i][j] = (image.getRGB(j, i)) & 0xFF;
            }
        }

        rgb.add(r);
        rgb.add(g);
        rgb.add(b);

        return rgb;
    }

    /**
     * Get YUV data from given image's RGB data
     *
     * @param image Image
     * @return List with three elements of two-dimensional int's - Y, U and V
     */
    public static List<int[][]> getYuvFromImage(BufferedImage image) {
        List<int[][]> yuv = new ArrayList<>();
        int[][] y;
        int[][] u;
        int[][] v;
        int[][] aa;
        int a;
        int r;
        int g;
        int b;
        int width;
        int height;

        width = image.getWidth();
        height = image.getHeight();

        y = new int[height][width];
        u = new int[height][width];
        v = new int[height][width];
        aa = new int[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                a = (image.getRGB(j, i) >> 24) & 0xFF;
                r = (image.getRGB(j, i) >> 16) & 0xFF;
                g = (image.getRGB(j, i) >> 8) & 0xFF;
                b = (image.getRGB(j, i)) & 0xFF;

                // Convert RGB to YUV colorspace
                y[i][j] = (int) ((0.299 * r) + (0.587 * g) + (0.114 * b));
                u[i][j] = (int) ((-0.147 * r) - (0.289 * g) + (0.436 * b));
                v[i][j] = (int) ((0.615 * r) - (0.515 * g) - (0.100 * b));
                aa[i][j] = a;
            }
        }

        yuv.add(y);
        yuv.add(u);
        yuv.add(v);
        yuv.add(aa);

        return yuv;
    }

    /**
     * Get image from given RGB data
     *
     * @param rgb List with three elements of two-dimensional int's - R, G and B
     * @return Image
     */
    @SuppressWarnings("unused")
    public static BufferedImage getImageFromRgb(List<int[][]> rgb) {
        BufferedImage image;
        int width;
        int height;
        int[][] r;
        int[][] g;
        int[][] b;

        r = rgb.get(0);
        g = rgb.get(1);
        b = rgb.get(2);

        height = r.length;
        width = r[0].length;
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                image.setRGB(j, i, (r[i][j] << 16) + (g[i][j] << 8) + b[i][j]);
            }
        }

        return image;
    }

    /**
     * Get image (with RGB data) from given YUV data
     *
     * @param yuv     List with three elements of two-dimensional int's - Y, U and V
     * @param imgType Type of image (e.g. BufferedImage.TYPE_INT_RGB)
     * @return Image
     */
    public static BufferedImage getImageFromYuv(List<int[][]> yuv, int imgType) {
        BufferedImage image;
        int width;
        int height;
        int a;
        int r;
        int g;
        int b;
        int[][] y;
        int[][] u;
        int[][] v;
        int[][] aa;

        y = yuv.get(0);
        u = yuv.get(1);
        v = yuv.get(2);
        aa = yuv.get(3);

        height = y.length;
        width = y[0].length;
        image = new BufferedImage(width, height, (imgType == 0 ? BufferedImage.TYPE_INT_RGB : imgType));

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                // Convert YUV back to RGB
                r = pixelRange(y[i][j] + 1.140 * v[i][j]);
                g = pixelRange(y[i][j] - 0.395 * u[i][j] - 0.581 * v[i][j]);
                b = pixelRange(y[i][j] + 2.032 * u[i][j]);
                a = aa[i][j];

                image.setRGB(j, i, (a << 24) + (r << 16) + (g << 8) + b);
            }
        }

        return image;
    }

    /**
     * Utility method to limit the value within [0,255] range
     *
     * @param p Input value
     * @return Limited value
     */
    public static int pixelRange(int p) {
        return ((p > 255) ? 255 : Math.max(p, 0));
    }

    /**
     * Utility method to limit the value within [0,255] range
     *
     * @param p Input value
     * @return Limited value
     */
    public static int pixelRange(double p) {
        return ((p > 255) ? 255 : (p < 0) ? 0 : (int) p);
    }

    /**
     * Method to pad an image such that it becomes perfect square. The padding uses black color
     *
     * @param image Input image
     */
    public static void makeImageSquare(ImageHolder image) {
        int max;

        max = Math.max(image.getImage().getWidth(), image.getImage().getHeight());
        cropImage(image, max, max);
    }

    /**
     * Method crop an image to the given dimensions. If dimensions are more than the input image size, then the image
     * gets padded with black color
     *
     * @param image      Input image
     * @param cropWidth  Width required for cropped image
     * @param cropHeight Height required for cropped image
     */
    public static void cropImage(ImageHolder image, int cropWidth, int cropHeight) {
        BufferedImage retImg;
        int width;
        int height;

        width = image.getImage().getWidth();
        height = image.getImage().getHeight();

        retImg = new BufferedImage(cropWidth, cropHeight, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < cropWidth; i++) {
            for (int j = 0; j < cropHeight; j++) {
                if (i < width && j < height) {
                    retImg.setRGB(i, j, image.getImage().getRGB(i, j));
                } else {
                    retImg.setRGB(i, j, 0);
                }
            }
        }

        image.setImage(retImg);
    }

    /**
     * Method generate difference image between two given images
     *
     * @param leftImage  Left input image
     * @param rightImage Right input image
     * @return Difference image
     * @throws OpenStegoException Processing issues
     */
    public static ImageHolder getDiffImage(ImageHolder leftImage, ImageHolder rightImage) throws OpenStegoException {
        int leftW;
        int leftH;
        int rightW;
        int rightH;
        int min;
        int max;
        int diff;
        BufferedImage diffImage;

        leftW = leftImage.getImage().getWidth();
        leftH = leftImage.getImage().getHeight();
        rightW = rightImage.getImage().getWidth();
        rightH = rightImage.getImage().getHeight();
        if (leftW != rightW || leftH != rightH) {
            throw new OpenStegoException(null, OpenStego.NAMESPACE, OpenStegoErrors.IMAGE_FILE_INVALID);
        }
        diffImage = new BufferedImage(leftW, leftH, BufferedImage.TYPE_INT_RGB);

        min = Math.abs(leftImage.getImage().getRGB(0, 0) - rightImage.getImage().getRGB(0, 0));
        max = min;

        for (int i = 0; i < leftW; i++) {
            for (int j = 0; j < leftH; j++) {
                diff = Math.abs(leftImage.getImage().getRGB(i, j) - rightImage.getImage().getRGB(i, j));
                // error += diff * diff;
                if (diff < min) {
                    min = diff;
                }
                if (diff > max) {
                    max = diff;
                }
            }
        }

        for (int i = 0; i < leftW; i++) {
            for (int j = 0; j < leftH; j++) {
                diff = Math.abs(leftImage.getImage().getRGB(i, j) - rightImage.getImage().getRGB(i, j));
                diffImage.setRGB(i, j, pixelRange((double) (diff - min) / (double) (max - min) * Math.pow(2, 32)));
                // TODO
            }
        }

        return new ImageHolder(diffImage, null);
    }

    private static void writeImage(ImageHolder image, String imageType, OutputStream os) throws OpenStegoException {
        if ("jpeg".equals(imageType) || "jpg".equals(imageType)) {
            writeJpegImage(image, os);
        } else {
            try {
                ImageWriter writer = ImageIO.getImageWritersByFormatName(imageType).next();
                IIOMetadata metadata = image.getMetadata();
                // Keep the ICC colour profile of the cover image (e.g. when converting JPEG to PNG)
                if ("png".equals(imageType)) {
                    metadata = addPngIccProfile(writer, image.getImage(), metadata, getIccProfile(metadata));
                }
                writer.setOutput(ImageIO.createImageOutputStream(os));
                writer.write(null, new IIOImage(image.getImage(), null, metadata), null);
            } catch (IOException e) {
                throw new OpenStegoException(e);
            }
        }
    }

    private static void writeJpegImage(ImageHolder image, OutputStream os) throws OpenStegoException {
        try {
            JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
            jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpegParams.setOptimizeHuffmanTables(true);
            Float qual = UserPreferences.getFloat("image.writer.jpeg.quality");
            if (qual == null) {
                qual = 0.75f;
            }
            jpegParams.setCompressionQuality(qual);

            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
            writer.setOutput(ImageIO.createImageOutputStream(os));

            // If the cover image had an ICC colour profile, attach it to the output image, so that the pixel values are
            // written as-is and the profile is embedded in the JPEG file (APP2 marker)
            BufferedImage outImage = image.getImage();
            ICC_Profile iccProfile = getIccProfile(image.getMetadata());
            if (iccProfile != null) {
                outImage = withColorProfile(outImage, iccProfile);
            }

            IIOMetadata metadata = writer.getDefaultImageMetadata(new ImageTypeSpecifier(outImage), jpegParams);
            String metadataFormatName = metadata.getNativeMetadataFormatName();

            // We only copy over EXIF data from original file. This is possible only if the original file was also a
            // JPEG file (metadata of other formats like PNG cannot be merged into JPEG metadata).
            if (image.getMetadata() != null && metadataFormatName.equals(image.getMetadata().getNativeMetadataFormatName())) {
                Node mdRoot = image.getMetadata().getAsTree(metadataFormatName);
                Node mdNode = mdRoot.getFirstChild();
                while (mdNode != null) {
                    if ("markerSequence".equals(mdNode.getNodeName())) {
                        Node marker = mdNode.getFirstChild();
                        while (marker != null) {
                            Node next = marker.getNextSibling();
                            // Remove all markers other than EXIF (225)
                            if (marker.getAttributes().getNamedItem("MarkerTag") == null
                                    || !"225".equals(marker.getAttributes().getNamedItem("MarkerTag").getNodeValue())) {
                                mdNode.removeChild(marker);
                            }
                            marker = next;
                        }
                        break;
                    }
                    mdNode = mdNode.getNextSibling();
                }
                metadata.mergeTree(metadataFormatName, mdRoot);
            }

            writer.write(null, new IIOImage(outImage, null, metadata), jpegParams);
        } catch (IOException e) {
            throw new OpenStegoException(e);
        }
    }

    private static ImageHolder readImage(InputStream is) throws OpenStegoException {
        try {
            ImageInputStream imageIS = ImageIO.createImageInputStream(is);
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageIS);
            if (!readers.hasNext()) {
                return null;
            }

            ImageReader reader = readers.next();
            reader.setInput(imageIS);
            BufferedImage image;
            ImageTypeSpecifier iccType = getIccImageType(reader);
            if (iccType != null) {
                // Image has an embedded (non-sRGB) ICC colour profile. By default, the JPEG reader converts the pixels
                // to sRGB, which changes the pixel values. Instead, read the original pixel values as-is. The profile
                // remains available in the metadata and is written back to the output image.
                ImageReadParam param = reader.getDefaultReadParam();
                param.setDestination(iccType.createBufferedImage(reader.getWidth(0), reader.getHeight(0)));
                image = copyPixelValues(reader.read(0, param));
            } else {
                image = reader.read(0);
            }
            IIOMetadata metadata;
            try {
                metadata = reader.getImageMetadata(0);
            } catch (IOException e) {
                ImageWriter writer = ImageIO.getImageWriter(reader);
                ImageWriteParam param = writer.getDefaultWriteParam();
                metadata = writer.getDefaultImageMetadata(ImageTypeSpecifier.createFromRenderedImage(image), param);
            }
            return new ImageHolder(image, metadata);
        } catch (IOException e) {
            throw new OpenStegoException(e);
        }
    }

    /**
     * Returns the ICC colour profile stored in the given image metadata (PNG "iCCP" chunk or JPEG "APP2" marker)
     *
     * @param metadata Image metadata
     * @return ICC profile, or null if the metadata does not contain one
     */
    public static ICC_Profile getIccProfile(IIOMetadata metadata) {
        if (metadata == null || metadata.getNativeMetadataFormatName() == null) {
            return null;
        }
        try {
            IIOMetadataNode node = findNode(metadata.getAsTree(metadata.getNativeMetadataFormatName()), "iCCP");
            if (node != null && node.getUserObject() instanceof byte[]) {
                // PNG stores the profile compressed using deflate
                return ICC_Profile.getInstance(inflate((byte[]) node.getUserObject()));
            }
            node = findNode(metadata.getAsTree(metadata.getNativeMetadataFormatName()), "app2ICC");
            if (node != null && node.getUserObject() instanceof ICC_Profile) {
                return (ICC_Profile) node.getUserObject();
            }
        } catch (IllegalArgumentException | IOException | DataFormatException e) {
            // Invalid or unsupported profile - ignore it
        }
        return null;
    }

    /**
     * Returns PNG metadata for the given image containing the given ICC profile. If the metadata already contains an
     * ICC profile, or no profile is given, the metadata is returned as-is.
     */
    private static IIOMetadata addPngIccProfile(ImageWriter writer, BufferedImage image, IIOMetadata metadata,
            ICC_Profile iccProfile) throws IOException {
        String pngFormat = "javax_imageio_png_1.0";
        if (iccProfile == null) {
            return metadata;
        }
        if (metadata != null && pngFormat.equals(metadata.getNativeMetadataFormatName())
                && findNode(metadata.getAsTree(pngFormat), "iCCP") != null) {
            return metadata;
        }

        ImageTypeSpecifier type = new ImageTypeSpecifier(image);
        IIOMetadata pngMetadata = metadata == null ? null : writer.convertImageMetadata(metadata, type, null);
        if (pngMetadata == null || !pngFormat.equals(pngMetadata.getNativeMetadataFormatName())) {
            pngMetadata = writer.getDefaultImageMetadata(type, null);
        }

        IIOMetadataNode iccNode = new IIOMetadataNode("iCCP");
        iccNode.setAttribute("profileName", "ICC profile");
        iccNode.setAttribute("compressionMethod", "deflate");
        iccNode.setUserObject(deflate(iccProfile.getData()));
        IIOMetadataNode root = new IIOMetadataNode(pngFormat);
        root.appendChild(iccNode);
        pngMetadata.mergeTree(pngFormat, root);
        return pngMetadata;
    }

    /**
     * Returns a reader image type which uses an embedded, non-sRGB, RGB colour profile, if the image has one
     */
    private static ImageTypeSpecifier getIccImageType(ImageReader reader) throws IOException {
        Iterator<ImageTypeSpecifier> types = reader.getImageTypes(0);
        while (types != null && types.hasNext()) {
            ImageTypeSpecifier type = types.next();
            ColorSpace cs = type.getColorModel().getColorSpace();
            if (cs instanceof ICC_ColorSpace && !cs.isCS_sRGB() && cs.getType() == ColorSpace.TYPE_RGB
                    && type.getColorModel().getNumColorComponents() == 3) {
                return type;
            }
        }
        return null;
    }

    /**
     * Copies the raw pixel values (without any colour space conversion) into a standard RGB / ARGB image
     */
    private static BufferedImage copyPixelValues(BufferedImage source) {
        boolean alpha = source.getColorModel().hasAlpha();
        BufferedImage target = new BufferedImage(source.getWidth(), source.getHeight(),
                alpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        Raster raster = source.getRaster();
        int[] pixel = new int[raster.getNumBands()];
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                raster.getPixel(x, y, pixel);
                int argb = (pixel[0] << 16) | (pixel[1] << 8) | pixel[2];
                argb |= (alpha ? pixel[3] : 0xFF) << 24;
                target.setRGB(x, y, argb);
            }
        }
        return target;
    }

    /**
     * Returns an image with the same pixel values as the given image, but tagged with the given colour profile
     */
    private static BufferedImage withColorProfile(BufferedImage image, ICC_Profile iccProfile) {
        ColorModel cm = new ComponentColorModel(new ICC_ColorSpace(iccProfile), false, false, Transparency.OPAQUE,
                DataBuffer.TYPE_BYTE);
        WritableRaster raster = cm.createCompatibleWritableRaster(image.getWidth(), image.getHeight());
        int[] pixel = new int[3];
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                pixel[0] = (rgb >> 16) & 0xFF;
                pixel[1] = (rgb >> 8) & 0xFF;
                pixel[2] = rgb & 0xFF;
                raster.setPixel(x, y, pixel);
            }
        }
        return new BufferedImage(cm, raster, false, null);
    }

    private static IIOMetadataNode findNode(Node node, String name) {
        if (node == null) {
            return null;
        }
        if (name.equals(node.getNodeName()) && node instanceof IIOMetadataNode) {
            return (IIOMetadataNode) node;
        }
        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            IIOMetadataNode found = findNode(child, name);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private static byte[] deflate(byte[] data) {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        deflater.finish();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        while (!deflater.finished()) {
            bos.write(buffer, 0, deflater.deflate(buffer));
        }
        deflater.end();
        return bos.toByteArray();
    }

    private static byte[] inflate(byte[] data) throws DataFormatException, IOException {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        while (!inflater.finished()) {
            int n = inflater.inflate(buffer);
            if (n == 0 && (inflater.needsInput() || inflater.needsDictionary())) {
                throw new IOException("Corrupt ICC profile data");
            }
            bos.write(buffer, 0, n);
        }
        inflater.end();
        return bos.toByteArray();
    }
}
