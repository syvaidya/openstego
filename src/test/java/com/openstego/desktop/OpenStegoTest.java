package com.openstego.desktop;

import com.openstego.desktop.util.CommonUtil;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for {@link OpenStego}
 */
public class OpenStegoTest extends MockitoTest {

    @Mock
    private OpenStegoPlugin<?> mockPlugin;

    @Test
    public void testConstructor() throws OpenStegoException {
        OpenStegoConfig config = new OpenStegoConfig();
        OpenStego os = new OpenStego(mockPlugin, config); // Should not throw any exception
        assertNotNull(os);
        assertEquals(config, os.getConfig());

        // Case - bad input 1
        try {
            new OpenStego(null, null);
            fail("Did not throw OpenStegoException");
        } catch (OpenStegoException e) {
            assertEquals(OpenStegoErrors.NO_PLUGIN_SPECIFIED, e.getErrorCode());
        }

        // Case - bad input 2
        try {
            new OpenStego(mockPlugin, null);
            fail("Did not throw AssertionError");
        } catch (AssertionError e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testEmbedData() throws OpenStegoException {
        byte[] msg = "message".getBytes(StandardCharsets.UTF_8);
        byte[] cover = "cover data".getBytes(StandardCharsets.UTF_8);
        String msgFileName = "message.txt";
        String coverFileName = "cover.in";
        String stegoFileName = "stego.out";

        doReturn(Collections.singletonList(OpenStegoPlugin.Purpose.DATA_HIDING)).when(mockPlugin).getPurposes();

        OpenStegoConfig config = new OpenStegoConfig();
        config.setUseCompression(false);
        config.setUseEncryption(false);
        OpenStego os = new OpenStego(mockPlugin, config);
        os.embedData(msg, msgFileName, cover, coverFileName, stegoFileName);

        // Plugin should be called with same values as-is
        verify(mockPlugin, times(1)).embedData(msg, msgFileName, cover, coverFileName, stegoFileName);
    }

    @Test
    public void testEmbedData_withCompressionAndEncryption() throws OpenStegoException, IOException {
        byte[] msg = "message".getBytes(StandardCharsets.UTF_8);
        byte[] cover = "cover data".getBytes(StandardCharsets.UTF_8);
        String msgFileName = "message.txt";
        String coverFileName = "cover.in";
        String stegoFileName = "stego.out";

        doReturn(Collections.singletonList(OpenStegoPlugin.Purpose.DATA_HIDING)).when(mockPlugin).getPurposes();

        OpenStegoConfig config = new OpenStegoConfig();
        config.setUseCompression(true);
        config.setUseEncryption(true);
        config.setPassword("test");
        OpenStego os = new OpenStego(mockPlugin, config);
        os.embedData(msg, msgFileName, cover, coverFileName, stegoFileName);

        // Plugin should be called with compressed and encrypted message
        ArgumentCaptor<byte[]> msgCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(mockPlugin, times(1)).embedData(msgCaptor.capture(), argThat(msgFileName::equals), argThat(cover::equals),
                argThat(coverFileName::equals), argThat(stegoFileName::equals));

        // Decrypt and decompress message and compare with original
        OpenStegoCrypto crypto = new OpenStegoCrypto(config.getPassword(), config.getEncryptionAlgorithm());
        byte[] outputMsg = crypto.decrypt(msgCaptor.getValue());
        try (ByteArrayInputStream bis = new ByteArrayInputStream(outputMsg); GZIPInputStream zis = new GZIPInputStream(bis)) {
            outputMsg = CommonUtil.streamToBytes(zis);
        }
        assertArrayEquals(msg, outputMsg);
    }

    @Test
    public void testEmbedData_exception() throws OpenStegoException {
        byte[] msg = "message".getBytes(StandardCharsets.UTF_8);
        byte[] cover = "cover data".getBytes(StandardCharsets.UTF_8);
        String msgFileName = "message.txt";
        String coverFileName = "cover.in";
        String stegoFileName = "stego.out";

        doReturn(
                Collections.singletonList(OpenStegoPlugin.Purpose.WATERMARKING),
                Collections.singletonList(OpenStegoPlugin.Purpose.DATA_HIDING)
        ).when(mockPlugin).getPurposes();

        doThrow(
                new OpenStegoException(null, OpenStego.NAMESPACE, OpenStegoErrors.INVALID_CRYPT_ALGO),
                new RuntimeException()
        ).when(mockPlugin).embedData(any(byte[].class), anyString(), any(byte[].class), anyString(), anyString());

        OpenStego os = new OpenStego(mockPlugin, new OpenStegoConfig());

        // Case - plugin does not support data hiding
        try {
            os.embedData(msg, msgFileName, cover, coverFileName, stegoFileName);
            fail("Did not throw exception");
        } catch (OpenStegoException e) {
            assertEquals(OpenStegoErrors.PLUGIN_DOES_NOT_SUPPORT_DH, e.getErrorCode());
        }

        // Case - handled exception (e.g. invalid crypto algo)
        try {
            os.embedData(msg, msgFileName, cover, coverFileName, stegoFileName);
            fail("Did not throw exception");
        } catch (OpenStegoException e) {
            assertEquals(OpenStegoErrors.INVALID_CRYPT_ALGO, e.getErrorCode());
        }

        // Case - unhandled exception (e.g. runtime exception)
        try {
            os.embedData(msg, msgFileName, cover, coverFileName, stegoFileName);
            fail("Did not throw exception");
        } catch (OpenStegoException e) {
            assertEquals(OpenStegoException.UNHANDLED_EXCEPTION, e.getErrorCode());
        }
    }

    @Test
    public void testEmbedDataFile() throws OpenStegoException, IOException {
        Path msgFilePath = createTempFile("message", ".txt", "message");
        Path coverFilePath = createTempFile("cover", ".in", "cover data");
        String stegoFileName = "stego.out";

        doReturn(Collections.singletonList(OpenStegoPlugin.Purpose.DATA_HIDING)).when(mockPlugin).getPurposes();

        OpenStegoConfig config = new OpenStegoConfig();
        config.setUseCompression(false);
        config.setUseEncryption(false);
        OpenStego os = new OpenStego(mockPlugin, config);

        try {
            os.embedData(msgFilePath.toFile(), coverFilePath.toFile(), stegoFileName);
            verify(mockPlugin, times(1)).embedData(
                    argThat(v -> "message".equals(new String(v, StandardCharsets.UTF_8))),
                    argThat(v -> v.startsWith("message") && v.endsWith(".txt")),
                    argThat(v -> "cover data".equals(new String(v, StandardCharsets.UTF_8))),
                    argThat(v -> v.startsWith("cover") && v.endsWith(".in")),
                    argThat(stegoFileName::equals)
            );
        } finally {
            Files.delete(msgFilePath);
            Files.delete(coverFilePath);
        }
    }

    @Test
    public void testEmbedDataFile_exception() throws OpenStegoException {
        File msgFile = new File("non-existent-msg.txt");
        File coverFile = new File("non-existent-cover.txt");
        String stegoFileName = "stego.out";

        OpenStego os = new OpenStego(mockPlugin, new OpenStegoConfig());

        // Case - IOException
        try {
            os.embedData(msgFile, coverFile, stegoFileName);
            fail("Did not throw exception");
        } catch (OpenStegoException e) {
            assertTrue(e.getCause() instanceof IOException);
        }
    }

    @Test
    public void testEmbedMark() throws OpenStegoException {
        byte[] sig = "signature".getBytes(StandardCharsets.UTF_8);
        byte[] cover = "cover data".getBytes(StandardCharsets.UTF_8);
        String sigFileName = "general.sig";
        String coverFileName = "cover.in";
        String stegoFileName = "stego.out";

        doReturn(Collections.singletonList(OpenStegoPlugin.Purpose.WATERMARKING)).when(mockPlugin).getPurposes();

        OpenStego os = new OpenStego(mockPlugin, new OpenStegoConfig());
        os.embedMark(sig, sigFileName, cover, coverFileName, stegoFileName);

        // Plugin should be called with same values as-is
        verify(mockPlugin, times(1)).embedData(sig, sigFileName, cover, coverFileName, stegoFileName);
    }

    @Test
    public void testEmbedMark_exception() throws OpenStegoException {
        byte[] sig = "signature".getBytes(StandardCharsets.UTF_8);
        byte[] cover = "cover data".getBytes(StandardCharsets.UTF_8);
        String sigFileName = "general.sig";
        String coverFileName = "cover.in";
        String stegoFileName = "stego.out";

        doReturn(
                Collections.singletonList(OpenStegoPlugin.Purpose.DATA_HIDING),
                Collections.singletonList(OpenStegoPlugin.Purpose.WATERMARKING)
        ).when(mockPlugin).getPurposes();

        doThrow(
                new OpenStegoException(null, OpenStego.NAMESPACE, OpenStegoErrors.INVALID_CRYPT_ALGO),
                new RuntimeException()
        ).when(mockPlugin).embedData(any(byte[].class), anyString(), any(byte[].class), anyString(), anyString());

        OpenStego os = new OpenStego(mockPlugin, new OpenStegoConfig());

        // Case - plugin does not support watermarking
        try {
            os.embedMark(sig, sigFileName, cover, coverFileName, stegoFileName);
            fail("Did not throw exception");
        } catch (OpenStegoException e) {
            assertEquals(OpenStegoErrors.PLUGIN_DOES_NOT_SUPPORT_WM, e.getErrorCode());
        }

        // Case - handled exception (e.g. invalid crypto algo)
        try {
            os.embedMark(sig, sigFileName, cover, coverFileName, stegoFileName);
            fail("Did not throw exception");
        } catch (OpenStegoException e) {
            assertEquals(OpenStegoErrors.INVALID_CRYPT_ALGO, e.getErrorCode());
        }

        // Case - unhandled exception (e.g. runtime exception)
        try {
            os.embedMark(sig, sigFileName, cover, coverFileName, stegoFileName);
            fail("Did not throw exception");
        } catch (OpenStegoException e) {
            assertEquals(OpenStegoException.UNHANDLED_EXCEPTION, e.getErrorCode());
        }
    }

    @Test
    public void testEmbedMarkFile() throws OpenStegoException, IOException {
        Path sigFilePath = createTempFile("general", ".sig", "signature");
        Path coverFilePath = createTempFile("cover", ".in", "cover data");
        String stegoFileName = "stego.out";

        doReturn(Collections.singletonList(OpenStegoPlugin.Purpose.WATERMARKING)).when(mockPlugin).getPurposes();
        OpenStego os = new OpenStego(mockPlugin, new OpenStegoConfig());

        try {
            os.embedMark(sigFilePath.toFile(), coverFilePath.toFile(), stegoFileName);
            verify(mockPlugin, times(1)).embedData(
                    argThat(v -> "signature".equals(new String(v, StandardCharsets.UTF_8))),
                    argThat(v -> v.startsWith("general") && v.endsWith(".sig")),
                    argThat(v -> "cover data".equals(new String(v, StandardCharsets.UTF_8))),
                    argThat(v -> v.startsWith("cover") && v.endsWith(".in")),
                    argThat(stegoFileName::equals)
            );
        } finally {
            Files.delete(sigFilePath);
            Files.delete(coverFilePath);
        }
    }

    @Test
    public void testEmbedMarkFile_exception() throws OpenStegoException {
        File sigFile = new File("non-existent-msg.txt");
        File coverFile = new File("non-existent-cover.txt");
        String stegoFileName = "stego.out";

        OpenStego os = new OpenStego(mockPlugin, new OpenStegoConfig());

        // Case - IOException
        try {
            os.embedMark(sigFile, coverFile, stegoFileName);
            fail("Did not throw exception");
        } catch (OpenStegoException e) {
            assertTrue(e.getCause() instanceof IOException);
        }
    }

    @Test
    public void testExtractData() throws OpenStegoException {
        byte[] stegoData = "stego data".getBytes(StandardCharsets.UTF_8);
        String stegoFileName = "stego.out";

        doReturn(Collections.singletonList(OpenStegoPlugin.Purpose.DATA_HIDING)).when(mockPlugin).getPurposes();

        OpenStegoConfig config = new OpenStegoConfig();
        config.setUseCompression(false);
        config.setUseEncryption(false);
        OpenStego os = new OpenStego(mockPlugin, config);
        os.extractData(stegoData, stegoFileName);

        // Plugin should be called with same values as-is
        verify(mockPlugin, times(1)).extractData(stegoData, stegoFileName, null);
    }

    @Test
    public void testExtractData_withCompressionAndEncryption() throws OpenStegoException {
        byte[] stegoData = "stego data".getBytes(StandardCharsets.UTF_8);
        String stegoFileName = "stego.out";

        doReturn(Collections.singletonList(OpenStegoPlugin.Purpose.DATA_HIDING)).when(mockPlugin).getPurposes();
        OpenStegoConfig config = new OpenStegoConfig();
        config.setUseCompression(true);
        config.setUseEncryption(true);
        config.setPassword("test");

        // Encrypt and compress stego data when plugin's extractData method is called
        OpenStegoCrypto crypto = new OpenStegoCrypto(config.getPassword(), config.getEncryptionAlgorithm());
        doAnswer(inv -> {
            byte[] sd;
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); GZIPOutputStream zos = new GZIPOutputStream(bos)) {
                zos.write(inv.getArgument(0));
                zos.finish();
                sd = bos.toByteArray();
            }
            return crypto.encrypt(sd);
        }).when(mockPlugin).extractData(any(byte[].class), anyString(), nullable(byte[].class));
        doReturn("message.txt").when(mockPlugin).extractMsgFileName(any(byte[].class), anyString());

        OpenStego os = new OpenStego(mockPlugin, config);
        List<?> output = os.extractData(stegoData, stegoFileName);

        // Plugin methods should be called once
        verify(mockPlugin, times(1)).extractData(any(byte[].class), anyString(), nullable(byte[].class));
        verify(mockPlugin, times(1)).extractMsgFileName(any(byte[].class), anyString());

        assertEquals(2, output.size());
        assertEquals("message.txt", output.get(0));
        assertArrayEquals(stegoData, (byte[]) output.get(1));
    }

    @Test
    public void testExtractData_exception() throws OpenStegoException {
        byte[] stegoData = "stego data".getBytes(StandardCharsets.UTF_8);
        String stegoFileName = "stego.out";

        doReturn(
                Collections.singletonList(OpenStegoPlugin.Purpose.WATERMARKING),
                Collections.singletonList(OpenStegoPlugin.Purpose.DATA_HIDING)
        ).when(mockPlugin).getPurposes();

        doReturn(
                "corrupt data".getBytes(StandardCharsets.UTF_8)
        ).doThrow(
                new OpenStegoException(null, OpenStego.NAMESPACE, OpenStegoErrors.INVALID_CRYPT_ALGO),
                new RuntimeException()
        ).when(mockPlugin).extractData(any(byte[].class), anyString(), nullable(byte[].class));

        OpenStego os = new OpenStego(mockPlugin, new OpenStegoConfig());

        // Case - plugin does not support data hiding
        try {
            os.extractData(stegoData, stegoFileName);
            fail("Did not throw exception");
        } catch (OpenStegoException e) {
            assertEquals(OpenStegoErrors.PLUGIN_DOES_NOT_SUPPORT_DH, e.getErrorCode());
        }

        // Case - Compressed data is corrupted
        try {
            os.extractData(stegoData, stegoFileName);
            fail("Did not throw exception");
        } catch (OpenStegoException e) {
            assertEquals(OpenStegoErrors.CORRUPT_DATA, e.getErrorCode());
        }

        // Case - handled exception (e.g. invalid crypto algo)
        try {
            os.extractData(stegoData, stegoFileName);
            fail("Did not throw exception");
        } catch (OpenStegoException e) {
            assertEquals(OpenStegoErrors.INVALID_CRYPT_ALGO, e.getErrorCode());
        }

        // Case - unhandled exception (e.g. runtime exception)
        try {
            os.extractData(stegoData, stegoFileName);
            fail("Did not throw exception");
        } catch (OpenStegoException e) {
            assertEquals(OpenStegoException.UNHANDLED_EXCEPTION, e.getErrorCode());
        }
    }

    @Test
    public void testExtractDataFile() throws OpenStegoException, IOException {
        Path stegoFilePath = createTempFile("stego", ".out", "stego data");

        doReturn(Collections.singletonList(OpenStegoPlugin.Purpose.DATA_HIDING)).when(mockPlugin).getPurposes();

        OpenStegoConfig config = new OpenStegoConfig();
        config.setUseCompression(false);
        config.setUseEncryption(false);
        OpenStego os = new OpenStego(mockPlugin, config);
        os.extractData(stegoFilePath.toFile());

        // Plugin should be called with same values
        verify(mockPlugin, times(1)).extractData(
                argThat(v -> Arrays.compare(v, "stego data".getBytes(StandardCharsets.UTF_8)) == 0),
                argThat(v -> v.startsWith("stego") && v.endsWith(".out")),
                isNull());
    }

    @Test
    public void testCheckMark() throws OpenStegoException {
        byte[] stegoData = "stego data".getBytes(StandardCharsets.UTF_8);
        String stegoFileName = "stego.out";
        byte[] sigData = "signature".getBytes(StandardCharsets.UTF_8);

        doReturn(Collections.singletonList(OpenStegoPlugin.Purpose.WATERMARKING)).when(mockPlugin).getPurposes();
        doReturn(0.5, Double.NaN).when(mockPlugin).checkMark(any(byte[].class), anyString(), any(byte[].class));

        OpenStego os = new OpenStego(mockPlugin, new OpenStegoConfig());
        double correl = os.checkMark(stegoData, stegoFileName, sigData);

        // Plugin should be called with same values as-is
        verify(mockPlugin, times(1)).checkMark(stegoData, stegoFileName, sigData);
        assertEquals(0.5, correl);

        // Second call to plugin returns NaN, and in that case main class is supposed to return zero
        correl = os.checkMark(stegoData, stegoFileName, sigData);
        assertEquals(0.0, correl);
    }

    @Test
    public void testCheckMark_exception() throws OpenStegoException {
        byte[] stegoData = "stego data".getBytes(StandardCharsets.UTF_8);
        String stegoFileName = "stego.out";
        byte[] sigData = "signature".getBytes(StandardCharsets.UTF_8);

        doReturn(Collections.singletonList(OpenStegoPlugin.Purpose.DATA_HIDING)).when(mockPlugin).getPurposes();
        OpenStego os = new OpenStego(mockPlugin, new OpenStegoConfig());

        // Case - plugin does not support watermarking
        try {
            os.checkMark(stegoData, stegoFileName, sigData);
            fail("Did not throw exception");
        } catch (OpenStegoException e) {
            assertEquals(OpenStegoErrors.PLUGIN_DOES_NOT_SUPPORT_WM, e.getErrorCode());
        }
    }

    @Test
    public void testCheckMarkFile() throws OpenStegoException, IOException {
        Path stegoFilePath = createTempFile("stego", ".out", "stego data");
        Path sigFilePath = createTempFile("general", ".sig", "sig data");

        doReturn(Collections.singletonList(OpenStegoPlugin.Purpose.WATERMARKING)).when(mockPlugin).getPurposes();

        OpenStego os = new OpenStego(mockPlugin, new OpenStegoConfig());
        os.checkMark(stegoFilePath.toFile(), sigFilePath.toFile());

        // Plugin should be called with same values
        verify(mockPlugin, times(1)).checkMark(
                argThat(v -> Arrays.compare(v, "stego data".getBytes(StandardCharsets.UTF_8)) == 0),
                argThat(v -> v.startsWith("stego") && v.endsWith(".out")),
                argThat(v -> Arrays.compare(v, "sig data".getBytes(StandardCharsets.UTF_8)) == 0));
    }

    @Test
    public void testCGenerateSignature() throws OpenStegoException {
        doReturn(Collections.singletonList(OpenStegoPlugin.Purpose.WATERMARKING)).when(mockPlugin).getPurposes();

        OpenStegoConfig config = new OpenStegoConfig();
        config.setPassword("test");
        OpenStego os = new OpenStego(mockPlugin, config);
        os.generateSignature();

        // Plugin should be called
        verify(mockPlugin, times(1)).generateSignature();
    }

    @Test
    public void testCGenerateSignature_exception() throws OpenStegoException {
        doReturn(
                Collections.singletonList(OpenStegoPlugin.Purpose.DATA_HIDING),
                Collections.singletonList(OpenStegoPlugin.Purpose.WATERMARKING)
        ).when(mockPlugin).getPurposes();

        OpenStegoConfig config = new OpenStegoConfig();
        OpenStego os = new OpenStego(mockPlugin, config);

        // Case - plugin does not support watermarking
        try {
            os.generateSignature();
            fail("Did not throw exception");
        } catch (OpenStegoException e) {
            assertEquals(OpenStegoErrors.PLUGIN_DOES_NOT_SUPPORT_WM, e.getErrorCode());
        }

        // Case - null password
        try {
            config.setPassword(null);
            os.generateSignature();
            fail("Did not throw exception");
        } catch (OpenStegoException e) {
            assertEquals(OpenStegoErrors.PWD_MANDATORY_FOR_GENSIG, e.getErrorCode());
        }

        // Case - blank password
        try {
            config.setPassword(" ");
            os.generateSignature();
            fail("Did not throw exception");
        } catch (OpenStegoException e) {
            assertEquals(OpenStegoErrors.PWD_MANDATORY_FOR_GENSIG, e.getErrorCode());
        }
    }

    @Test
    public void testGetDiff() throws OpenStegoException {
        byte[] stegoData = "stego data".getBytes(StandardCharsets.UTF_8);
        String stegoFileName = "stego.out";
        byte[] coverData = "cover data".getBytes(StandardCharsets.UTF_8);
        String coverFileName = "cover.in";
        byte[] diffData = "diff data".getBytes(StandardCharsets.UTF_8);
        String diffFileName = "data.diff";

        doReturn(diffData).when(mockPlugin).getDiff(stegoData, stegoFileName, coverData, coverFileName, diffFileName);

        OpenStego os = new OpenStego(mockPlugin, new OpenStegoConfig());
        byte[] output = os.getDiff(stegoData, stegoFileName, coverData, coverFileName, diffFileName);

        // Plugin should be called with same values as-is
        verify(mockPlugin, times(1)).getDiff(stegoData, stegoFileName, coverData, coverFileName, diffFileName);
        assertArrayEquals(diffData, output);
    }

    @Test
    public void testGetDiffFile() throws OpenStegoException, IOException {
        Path stegoFilePath = createTempFile("stego", ".out", "stego data");
        Path coverFilePath = createTempFile("cover", ".in", "cover data");
        byte[] diffData = "diff data".getBytes(StandardCharsets.UTF_8);
        String diffFileName = "data.diff";

        doReturn(diffData).when(mockPlugin).getDiff(any(byte[].class), anyString(), any(byte[].class), anyString(), anyString());

        OpenStego os = new OpenStego(mockPlugin, new OpenStegoConfig());
        byte[] output = os.getDiff(stegoFilePath.toFile(), coverFilePath.toFile(), diffFileName);

        // Plugin should be called with same values
        verify(mockPlugin, times(1)).getDiff(
                argThat(v -> Arrays.compare(v, "stego data".getBytes(StandardCharsets.UTF_8)) == 0),
                argThat(v -> v.startsWith("stego") && v.endsWith(".out")),
                argThat(v -> Arrays.compare(v, "cover data".getBytes(StandardCharsets.UTF_8)) == 0),
                argThat(v -> v.startsWith("cover") && v.endsWith(".in")),
                argThat(v -> v.equals("data.diff")));
        assertArrayEquals(diffData, output);
    }

    /**
     * Helper method to create temporary file with given content
     */
    private Path createTempFile(String prefix, String extension, String content) throws IOException {
        Path path = Files.createTempFile(prefix, extension);
        Files.writeString(path, content, StandardCharsets.UTF_8);
        return path;
    }

}
