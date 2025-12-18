/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.pwdattack;

/**
 * Class to hold the result of a password attack
 */
public class PasswordAttackResult {
    private final boolean found;
    private final String password;
    private final long attemptCount;
    private final long elapsedTimeMs;
    private final String extractedFileName;
    private final byte[] extractedData;

    /**
     * Constructor for failed/cancelled attack
     *
     * @param attemptCount  Number of passwords tried
     * @param elapsedTimeMs Time elapsed in milliseconds
     */
    public PasswordAttackResult(long attemptCount, long elapsedTimeMs) {
        this.found = false;
        this.password = null;
        this.attemptCount = attemptCount;
        this.elapsedTimeMs = elapsedTimeMs;
        this.extractedFileName = null;
        this.extractedData = null;
    }

    /**
     * Constructor for successful attack
     *
     * @param password          The found password
     * @param attemptCount      Number of passwords tried
     * @param elapsedTimeMs     Time elapsed in milliseconds
     * @param extractedFileName Name of the extracted file
     * @param extractedData     The extracted data
     */
    public PasswordAttackResult(String password, long attemptCount, long elapsedTimeMs,
                                String extractedFileName, byte[] extractedData) {
        this.found = true;
        this.password = password;
        this.attemptCount = attemptCount;
        this.elapsedTimeMs = elapsedTimeMs;
        this.extractedFileName = extractedFileName;
        this.extractedData = extractedData;
    }

    /**
     * @return true if password was found
     */
    public boolean isFound() {
        return found;
    }

    /**
     * @return the found password, or null if not found
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return number of passwords attempted
     */
    public long getAttemptCount() {
        return attemptCount;
    }

    /**
     * @return elapsed time in milliseconds
     */
    public long getElapsedTimeMs() {
        return elapsedTimeMs;
    }

    /**
     * @return name of extracted file, or null if not found
     */
    public String getExtractedFileName() {
        return extractedFileName;
    }

    /**
     * @return extracted data bytes, or null if not found
     */
    public byte[] getExtractedData() {
        return extractedData;
    }

    /**
     * @return passwords per second rate
     */
    public double getPasswordsPerSecond() {
        if (elapsedTimeMs == 0) {
            return 0;
        }
        return (attemptCount * 1000.0) / elapsedTimeMs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (found) {
            sb.append("Password found: ").append(password).append("\n");
            sb.append("Extracted file: ").append(extractedFileName).append("\n");
        } else {
            sb.append("Password not found\n");
        }
        sb.append("Attempts: ").append(attemptCount).append("\n");
        sb.append("Time: ").append(elapsedTimeMs / 1000.0).append(" seconds\n");
        sb.append("Speed: ").append(String.format("%.2f", getPasswordsPerSecond())).append(" passwords/sec");
        return sb.toString();
    }
}
