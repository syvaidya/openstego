/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.pwdattack;

import com.openstego.desktop.OpenStegoPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Dictionary attack - tries passwords from a wordlist file.
 * This is the most basic attack mode, equivalent to hashcat mode 0.
 */
public class DictionaryAttack extends PasswordAttack {

    private final File wordlistFile;
    private long estimatedTotal = -1;

    /**
     * Creates a dictionary attack.
     *
     * @param stegoFile    The stego file to attack
     * @param plugin       The steganography plugin to use
     * @param wordlistFile The wordlist file containing passwords to try
     */
    public DictionaryAttack(File stegoFile, OpenStegoPlugin<?> plugin, File wordlistFile) {
        super(stegoFile, plugin);
        this.wordlistFile = wordlistFile;
        countLines();
    }

    /**
     * Counts the number of lines in the wordlist for progress estimation.
     */
    private void countLines() {
        try (BufferedReader reader = new BufferedReader(new FileReader(wordlistFile))) {
            long count = 0;
            while (reader.readLine() != null) {
                count++;
            }
            this.estimatedTotal = count;
        } catch (IOException e) {
            this.estimatedTotal = -1;
        }
    }

    @Override
    public PasswordAttackResult execute() {
        startTime = System.currentTimeMillis();
        attemptCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(wordlistFile))) {
            String password;
            while ((password = reader.readLine()) != null) {
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
        } catch (IOException e) {
            if (listener != null) {
                listener.onError(e);
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
        return AttackMode.DICTIONARY;
    }

    @Override
    public long getEstimatedTotal() {
        return estimatedTotal;
    }
}
