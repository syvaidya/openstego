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
 * Hybrid attack - combines a wordlist with mask-based generation.
 * Equivalent to hashcat modes 6 (wordlist+mask) and 7 (mask+wordlist).
 * <p>
 * Examples:
 * <ul>
 *   <li>Wordlist+Mask: "password" + "?d?d" = "password00", "password01", ...</li>
 *   <li>Mask+Wordlist: "?d?d" + "password" = "00password", "01password", ...</li>
 * </ul>
 */
public class HybridAttack extends PasswordAttack {

    private final File wordlistFile;
    private final MaskGenerator maskGenerator;
    private final boolean wordlistFirst;
    private long wordlistSize = -1;

    /**
     * Creates a hybrid attack.
     *
     * @param stegoFile     The stego file to attack
     * @param plugin        The steganography plugin to use
     * @param wordlistFile  The wordlist file
     * @param mask          The mask pattern
     * @param wordlistFirst If true, wordlist is prepended to mask output;
     *                      if false, mask output is prepended to wordlist
     */
    public HybridAttack(File stegoFile, OpenStegoPlugin<?> plugin, File wordlistFile,
                        String mask, boolean wordlistFirst) {
        this(stegoFile, plugin, wordlistFile, mask, wordlistFirst, null);
    }

    /**
     * Creates a hybrid attack with custom character sets.
     *
     * @param stegoFile      The stego file to attack
     * @param plugin         The steganography plugin to use
     * @param wordlistFile   The wordlist file
     * @param mask           The mask pattern
     * @param wordlistFirst  If true, wordlist is prepended to mask output
     * @param customCharsets Custom character sets for ?1-?4
     */
    public HybridAttack(File stegoFile, OpenStegoPlugin<?> plugin, File wordlistFile,
                        String mask, boolean wordlistFirst, String[] customCharsets) {
        super(stegoFile, plugin);
        this.wordlistFile = wordlistFile;
        this.maskGenerator = new MaskGenerator(mask, customCharsets);
        this.wordlistFirst = wordlistFirst;
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
            this.wordlistSize = count;
        } catch (IOException e) {
            this.wordlistSize = -1;
        }
    }

    @Override
    public PasswordAttackResult execute() {
        startTime = System.currentTimeMillis();
        attemptCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(wordlistFile))) {
            String word;
            while ((word = reader.readLine()) != null) {
                if (isCancelled()) {
                    break;
                }

                // For each word, try all mask combinations
                for (String maskPart : maskGenerator) {
                    if (isCancelled()) {
                        break;
                    }

                    String password = wordlistFirst ? (word + maskPart) : (maskPart + word);
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
        return wordlistFirst ? AttackMode.HYBRID_WORDLIST_MASK : AttackMode.HYBRID_MASK_WORDLIST;
    }

    @Override
    public long getEstimatedTotal() {
        long maskTotal = maskGenerator.getTotalCombinations();
        if (wordlistSize > 0 && maskTotal > 0) {
            // Check for overflow
            if (wordlistSize > Long.MAX_VALUE / maskTotal) {
                return Long.MAX_VALUE;
            }
            return wordlistSize * maskTotal;
        }
        return -1;
    }
}
