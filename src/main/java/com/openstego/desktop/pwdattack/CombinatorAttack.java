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
import java.util.ArrayList;
import java.util.List;

/**
 * Combinator attack - combines words from two wordlists.
 * Equivalent to hashcat mode 1.
 * <p>
 * For each word in wordlist1, it appends each word from wordlist2.
 * This is useful for passwords like "firstnamelastname" or "wordnumber".
 */
public class CombinatorAttack extends PasswordAttack {

    private final File wordlist1;
    private final File wordlist2;
    private long wordlist1Size = -1;
    private long wordlist2Size = -1;

    /**
     * Creates a combinator attack.
     *
     * @param stegoFile The stego file to attack
     * @param plugin    The steganography plugin to use
     * @param wordlist1 The first wordlist file
     * @param wordlist2 The second wordlist file
     */
    public CombinatorAttack(File stegoFile, OpenStegoPlugin<?> plugin, File wordlist1, File wordlist2) {
        super(stegoFile, plugin);
        this.wordlist1 = wordlist1;
        this.wordlist2 = wordlist2;
        countLines();
    }

    /**
     * Counts the number of lines in both wordlists for progress estimation.
     */
    private void countLines() {
        try (BufferedReader reader = new BufferedReader(new FileReader(wordlist1))) {
            long count = 0;
            while (reader.readLine() != null) {
                count++;
            }
            this.wordlist1Size = count;
        } catch (IOException e) {
            this.wordlist1Size = -1;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(wordlist2))) {
            long count = 0;
            while (reader.readLine() != null) {
                count++;
            }
            this.wordlist2Size = count;
        } catch (IOException e) {
            this.wordlist2Size = -1;
        }
    }

    @Override
    public PasswordAttackResult execute() {
        startTime = System.currentTimeMillis();
        attemptCount = 0;

        // Load wordlist2 into memory for repeated iteration
        List<String> list2 = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(wordlist2))) {
            String line;
            while ((line = reader.readLine()) != null) {
                list2.add(line);
            }
        } catch (IOException e) {
            if (listener != null) {
                listener.onError(e);
            }
            return new PasswordAttackResult(attemptCount, System.currentTimeMillis() - startTime);
        }

        try (BufferedReader reader1 = new BufferedReader(new FileReader(wordlist1))) {
            String word1;
            while ((word1 = reader1.readLine()) != null) {
                if (isCancelled()) {
                    break;
                }

                for (String word2 : list2) {
                    if (isCancelled()) {
                        break;
                    }

                    String password = word1 + word2;
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
        return AttackMode.COMBINATOR;
    }

    @Override
    public long getEstimatedTotal() {
        if (wordlist1Size > 0 && wordlist2Size > 0) {
            // Check for overflow
            if (wordlist1Size > Long.MAX_VALUE / wordlist2Size) {
                return Long.MAX_VALUE;
            }
            return wordlist1Size * wordlist2Size;
        }
        return -1;
    }
}
