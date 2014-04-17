/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2014 Samir Vaidya
 */

package net.sourceforge.openstego;

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
public class OpenStegoCrypto
{
    /**
     * 8-byte Salt for Password-based cryptography
     */
    private final byte[] SALT = { (byte) 0x28, (byte) 0x5F, (byte) 0x71, (byte) 0xC9, (byte) 0x1E, (byte) 0x35,
            (byte) 0x0A, (byte) 0x62 };

    /**
     * Iteration count for Password-based cryptography
     */
    private final int ITER_COUNT = 7;

    /**
     * Cipher to use for encryption
     */
    private Cipher encryptCipher = null;

    /**
     * Cipher to use for decryption
     */
    private Cipher decryptCipher = null;

    /**
     * Default constructor
     * 
     * @param password Password to use for encryption
     * @throws OpenStegoException
     */
    public OpenStegoCrypto(String password) throws OpenStegoException
    {
        KeySpec keySpec = null;
        SecretKey secretKey = null;
        AlgorithmParameterSpec algoParamSpec = null;

        try
        {
            if(password == null)
            {
                password = "";
            }

            // Create the key
            keySpec = new PBEKeySpec(password.toCharArray(), this.SALT, this.ITER_COUNT);
            secretKey = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
            this.encryptCipher = Cipher.getInstance(secretKey.getAlgorithm());
            this.decryptCipher = Cipher.getInstance(secretKey.getAlgorithm());

            // Prepare cipher parameters
            algoParamSpec = new PBEParameterSpec(this.SALT, this.ITER_COUNT);

            // Initialize the ciphers
            this.encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey, algoParamSpec);
            this.decryptCipher.init(Cipher.DECRYPT_MODE, secretKey, algoParamSpec);
        }
        catch(Exception ex)
        {
            if(ex instanceof OpenStegoException)
            {
                throw (OpenStegoException) ex;
            }
            else
            {
                throw new OpenStegoException(ex);
            }
        }
    }

    /**
     * Method to encrypt the data
     * 
     * @param input Data to be encrypted
     * @return Encrypted data
     * @throws OpenStegoException
     */
    public byte[] encrypt(byte[] input) throws OpenStegoException
    {
        try
        {
            return this.encryptCipher.doFinal(input);
        }
        catch(Exception ex)
        {
            if(ex instanceof OpenStegoException)
            {
                throw (OpenStegoException) ex;
            }
            else
            {
                throw new OpenStegoException(ex);
            }
        }
    }

    /**
     * Method to decrypt the data
     * 
     * @param input Data to be decrypted
     * @return Decrypted data (returns <code>null</code> if password is invalid)
     * @throws OpenStegoException
     */
    public byte[] decrypt(byte[] input) throws OpenStegoException
    {
        try
        {
            return this.decryptCipher.doFinal(input);
        }
        catch(BadPaddingException bpEx)
        {
            throw new OpenStegoException(bpEx, OpenStego.NAMESPACE, OpenStegoException.INVALID_PASSWORD);
        }
        catch(Exception ex)
        {
            if(ex instanceof OpenStegoException)
            {
                throw (OpenStegoException) ex;
            }
            else
            {
                throw new OpenStegoException(ex);
            }
        }
    }
}
