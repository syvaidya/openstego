/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.pwdattack;

/**
 * Listener interface for password attack progress updates
 */
public interface PasswordAttackListener {

    /**
     * Called periodically with progress updates.
     *
     * @param attemptCount     Number of passwords tried so far
     * @param currentPassword  The password currently being tried
     * @param elapsedTimeMs    Time elapsed in milliseconds
     * @param estimatedTotal   Estimated total number of passwords to try (-1 if unknown)
     */
    void onProgress(long attemptCount, String currentPassword, long elapsedTimeMs, long estimatedTotal);

    /**
     * Called when the attack is complete.
     *
     * @param result The result of the attack
     */
    void onComplete(PasswordAttackResult result);

    /**
     * Called when an error occurs during the attack.
     *
     * @param error The error that occurred
     */
    void onError(Exception error);

    /**
     * Check if the attack should be cancelled.
     *
     * @return true if the attack should be cancelled
     */
    boolean isCancelled();
}
