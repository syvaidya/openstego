/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.pwdattack;

import com.openstego.desktop.OpenStegoPlugin;

import java.io.File;
import java.util.List;

/**
 * Mask attack - brute force attack using mask patterns.
 * Equivalent to hashcat mode 3.
 * <p>
 * Mask character sets:
 * <ul>
 *   <li>?l - lowercase letters (a-z)</li>
 *   <li>?u - uppercase letters (A-Z)</li>
 *   <li>?d - digits (0-9)</li>
 *   <li>?s - special characters</li>
 *   <li>?a - all printable ASCII</li>
 *   <li>?1-?4 - custom character sets</li>
 * </ul>
 * <p>
 * Example masks:
 * <ul>
 *   <li>?d?d?d?d - 4 digit PIN</li>
 *   <li>?u?l?l?l?l?l - Capitalized 6-letter word</li>
 *   <li>password?d?d - "password" followed by 2 digits</li>
 * </ul>
 */
public class MaskAttack extends PasswordAttack {

    private final MaskGenerator generator;

    /**
     * Creates a mask attack.
     *
     * @param stegoFile The stego file to attack
     * @param plugin    The steganography plugin to use
     * @param mask      The mask pattern
     */
    public MaskAttack(File stegoFile, OpenStegoPlugin<?> plugin, String mask) {
        this(stegoFile, plugin, mask, null);
    }

    /**
     * Creates a mask attack with custom character sets.
     *
     * @param stegoFile      The stego file to attack
     * @param plugin         The steganography plugin to use
     * @param mask           The mask pattern
     * @param customCharsets Custom character sets for ?1-?4
     */
    public MaskAttack(File stegoFile, OpenStegoPlugin<?> plugin, String mask, String[] customCharsets) {
        super(stegoFile, plugin);
        this.generator = new MaskGenerator(mask, customCharsets);
    }

    @Override
    public PasswordAttackResult execute() {
        startTime = System.currentTimeMillis();
        attemptCount = 0;

        for (String password : generator) {
            if (isCancelled()) {
                break;
            }

            attemptCount++;
            reportProgress(password);

            List<?> result = tryPassword(password);
            if (result != null) {
                long elapsed = System.currentTimeMillis() - startTime;
                PasswordAttackResult attackResult = new PasswordAttackResult(
                        password, attemptCount, elapsed,
                        (String) result.get(0), (byte[]) result.get(1));
                if (listener != null) {
                    listener.onComplete(attackResult);
                }
                return attackResult;
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        PasswordAttackResult result = new PasswordAttackResult(attemptCount, elapsed);
        if (listener != null) {
            listener.onComplete(result);
        }
        return result;
    }

    @Override
    public AttackMode getAttackMode() {
        return AttackMode.MASK;
    }

    @Override
    public long getEstimatedTotal() {
        return generator.getTotalCombinations();
    }
}
