/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop;

import java.security.AlgorithmParameters;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 * This is the class for providing cryptography support to OpenStego.
 */
public class OpenStegoCrypto {
    /**
     * Constant for algorithm - DES
     */
    public static final String ALGO_DES = "DES";
    /**
     * Constant for algorithm - AES128
     */
    public static final String ALGO_AES128 = "AES128";
    /**
     * Constant for algorithm - AES256
     */
    public static final String ALGO_AES256 = "AES256";

    /**
     * 8-byte Salt for Password-based cryptography
     */
    private final byte[] SALT = { (byte) 0x28, (byte) 0x5F, (byte) 0x71, (byte) 0xC9, (byte) 0x1E, (byte) 0x35, (byte) 0x0A, (byte) 0x62 };

    /**
     * Iteration count for Password-based cryptography
     */
    private final int ITER_COUNT = 7;

    /**
     * Secret key for encryption
     */
    private SecretKey secretKey = null;

    /**
     * Default constructor
     *
     * @param password Password to use for encryption
     * @param algorithm Cryptography algorithm to use. If null or blank value is provided, then it defaults to AES128
     * @throws OpenStegoException
     */
    public OpenStegoCrypto(String password, String algorithm) throws OpenStegoException {
        KeySpec keySpec = null;

        try {
            if (algorithm == null || algorithm.trim().equals("") || ALGO_AES128.equalsIgnoreCase(algorithm)) {
                algorithm = "PBEWithHmacSHA256AndAES_128";
            } else if (ALGO_AES256.equalsIgnoreCase(algorithm)) {
                algorithm = "PBEWithHmacSHA256AndAES_256";
            } else if (ALGO_DES.equalsIgnoreCase(algorithm)) {
                algorithm = "PBEWithMD5AndDES";
            } else {
                throw new OpenStegoException(null, OpenStego.NAMESPACE, OpenStegoException.INVALID_CRYPT_ALGO, algorithm);
            }

            // Create the key
            keySpec = new PBEKeySpec(password.toCharArray(), this.SALT, this.ITER_COUNT);
            this.secretKey = SecretKeyFactory.getInstance(algorithm).generateSecret(keySpec);
        } catch (Exception ex) {
            throw new OpenStegoException(ex);
        }
    }

    /**
     * Method to encrypt the data
     *
     * @param input Data to be encrypted
     * @return Encrypted data
     * @throws OpenStegoException
     */
    public byte[] encrypt(byte[] input) throws OpenStegoException {
        try {
            Cipher encryptCipher = Cipher.getInstance(this.secretKey.getAlgorithm());
            AlgorithmParameterSpec algoParamSpec = new PBEParameterSpec(this.SALT, this.ITER_COUNT);
            encryptCipher.init(Cipher.ENCRYPT_MODE, this.secretKey, algoParamSpec);

            byte[] algoParams = encryptCipher.getParameters().getEncoded();
            byte[] msg = encryptCipher.doFinal(input);
            byte paramLen = Byte.parseByte(Integer.toString(algoParams.length));

            byte[] out = new byte[1 + paramLen + msg.length];
            // First byte = length of algo params
            out[0] = paramLen;
            // Next is algorithm params
            System.arraycopy(algoParams, 0, out, 1, paramLen);
            // Next is encrypted message
            System.arraycopy(msg, 0, out, paramLen + 1, msg.length);

            return out;
        } catch (Exception ex) {
            throw new OpenStegoException(ex);
        }
    }

    /**
     * Method to decrypt the data
     *
     * @param input Data to be decrypted
     * @return Decrypted data (returns <code>null</code> if password is invalid)
     * @throws OpenStegoException
     */
    public byte[] decrypt(byte[] input) throws OpenStegoException {
        try {
            // First byte is algo params length
            byte paramLen = input[0];
            // Copy algorithm params
            byte[] algoParamData = new byte[paramLen];
            System.arraycopy(input, 1, algoParamData, 0, paramLen);
            // Copy encrypted message
            byte[] msg = new byte[input.length - paramLen - 1];
            System.arraycopy(input, paramLen + 1, msg, 0, msg.length);

            AlgorithmParameters algoParams = AlgorithmParameters.getInstance(this.secretKey.getAlgorithm());
            algoParams.init(algoParamData);
            Cipher decryptCipher = Cipher.getInstance(this.secretKey.getAlgorithm());
            decryptCipher.init(Cipher.DECRYPT_MODE, this.secretKey, algoParams);
            return decryptCipher.doFinal(msg);
        } catch (BadPaddingException bpEx) {
            throw new OpenStegoException(bpEx, OpenStego.NAMESPACE, OpenStegoException.INVALID_PASSWORD);
        } catch (Exception ex) {
            throw new OpenStegoException(ex);
        }
    }
}
