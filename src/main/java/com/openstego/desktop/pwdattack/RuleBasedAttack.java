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
 * Rule-based attack - applies transformation rules to wordlist words.
 * Similar to hashcat rule-based attacks.
 * <p>
 * Supported rules (subset of hashcat rules):
 * <ul>
 *   <li>: - do nothing (passthrough)</li>
 *   <li>l - lowercase all</li>
 *   <li>u - uppercase all</li>
 *   <li>c - capitalize first letter</li>
 *   <li>C - lowercase first, uppercase rest</li>
 *   <li>t - toggle case of all characters</li>
 *   <li>r - reverse string</li>
 *   <li>d - duplicate word</li>
 *   <li>f - reflect (append reversed)</li>
 *   <li>{ - rotate left</li>
 *   <li>} - rotate right</li>
 *   <li>$ - append character (e.g., $1 appends '1')</li>
 *   <li>^ - prepend character (e.g., ^! prepends '!')</li>
 *   <li>[ - delete first character</li>
 *   <li>] - delete last character</li>
 *   <li>@X - purge all instances of character X</li>
 *   <li>sXY - replace X with Y</li>
 *   <li>TN - toggle case at position N</li>
 *   <li>'N - truncate at position N</li>
 * </ul>
 */
public class RuleBasedAttack extends PasswordAttack {

    private final File wordlistFile;
    private final File rulesFile;
    private final List<String> rules;
    private long wordlistSize = -1;

    /**
     * Creates a rule-based attack.
     *
     * @param stegoFile    The stego file to attack
     * @param plugin       The steganography plugin to use
     * @param wordlistFile The wordlist file
     * @param rulesFile    The rules file (one rule per line)
     */
    public RuleBasedAttack(File stegoFile, OpenStegoPlugin<?> plugin, File wordlistFile, File rulesFile) {
        super(stegoFile, plugin);
        this.wordlistFile = wordlistFile;
        this.rulesFile = rulesFile;
        this.rules = new ArrayList<>();
        loadRules();
        countWordlistLines();
    }

    /**
     * Creates a rule-based attack with built-in rules.
     *
     * @param stegoFile    The stego file to attack
     * @param plugin       The steganography plugin to use
     * @param wordlistFile The wordlist file
     * @param rules        List of rules to apply
     */
    public RuleBasedAttack(File stegoFile, OpenStegoPlugin<?> plugin, File wordlistFile, List<String> rules) {
        super(stegoFile, plugin);
        this.wordlistFile = wordlistFile;
        this.rulesFile = null;
        this.rules = new ArrayList<>(rules);
        countWordlistLines();
    }

    /**
     * Loads rules from the rules file.
     */
    private void loadRules() {
        if (rulesFile == null) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(rulesFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    rules.add(line);
                }
            }
        } catch (IOException e) {
            // If rules file fails to load, use passthrough rule
            if (rules.isEmpty()) {
                rules.add(":");
            }
        }
        // Ensure at least one rule
        if (rules.isEmpty()) {
            rules.add(":");
        }
    }

    /**
     * Counts the number of lines in the wordlist.
     */
    private void countWordlistLines() {
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

                // Apply each rule to the word
                for (String rule : rules) {
                    if (isCancelled()) {
                        break;
                    }

                    String password = applyRule(word, rule);
                    if (password == null) {
                        continue; // Rule rejected the word
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

    /**
     * Applies a rule string to a word.
     *
     * @param word The input word
     * @param rule The rule to apply
     * @return The transformed word, or null if rule rejects it
     */
    public static String applyRule(String word, String rule) {
        if (word == null || rule == null || rule.isEmpty()) {
            return word;
        }

        StringBuilder result = new StringBuilder(word);
        int i = 0;

        while (i < rule.length()) {
            char cmd = rule.charAt(i);

            switch (cmd) {
                case ':': // Do nothing
                    i++;
                    break;

                case 'l': // Lowercase all
                    result = new StringBuilder(result.toString().toLowerCase());
                    i++;
                    break;

                case 'u': // Uppercase all
                    result = new StringBuilder(result.toString().toUpperCase());
                    i++;
                    break;

                case 'c': // Capitalize first
                    if (result.length() > 0) {
                        result.setCharAt(0, Character.toUpperCase(result.charAt(0)));
                        for (int j = 1; j < result.length(); j++) {
                            result.setCharAt(j, Character.toLowerCase(result.charAt(j)));
                        }
                    }
                    i++;
                    break;

                case 'C': // Lowercase first, uppercase rest
                    if (result.length() > 0) {
                        result.setCharAt(0, Character.toLowerCase(result.charAt(0)));
                        for (int j = 1; j < result.length(); j++) {
                            result.setCharAt(j, Character.toUpperCase(result.charAt(j)));
                        }
                    }
                    i++;
                    break;

                case 't': // Toggle case
                    for (int j = 0; j < result.length(); j++) {
                        char c = result.charAt(j);
                        if (Character.isUpperCase(c)) {
                            result.setCharAt(j, Character.toLowerCase(c));
                        } else if (Character.isLowerCase(c)) {
                            result.setCharAt(j, Character.toUpperCase(c));
                        }
                    }
                    i++;
                    break;

                case 'r': // Reverse
                    result.reverse();
                    i++;
                    break;

                case 'd': // Duplicate
                    result.append(result.toString());
                    i++;
                    break;

                case 'f': // Reflect (append reversed)
                    result.append(new StringBuilder(result.toString()).reverse());
                    i++;
                    break;

                case '{': // Rotate left
                    if (result.length() > 0) {
                        char first = result.charAt(0);
                        result.deleteCharAt(0);
                        result.append(first);
                    }
                    i++;
                    break;

                case '}': // Rotate right
                    if (result.length() > 0) {
                        char last = result.charAt(result.length() - 1);
                        result.deleteCharAt(result.length() - 1);
                        result.insert(0, last);
                    }
                    i++;
                    break;

                case '$': // Append character
                    if (i + 1 < rule.length()) {
                        result.append(rule.charAt(i + 1));
                        i += 2;
                    } else {
                        i++;
                    }
                    break;

                case '^': // Prepend character
                    if (i + 1 < rule.length()) {
                        result.insert(0, rule.charAt(i + 1));
                        i += 2;
                    } else {
                        i++;
                    }
                    break;

                case '[': // Delete first
                    if (result.length() > 0) {
                        result.deleteCharAt(0);
                    }
                    i++;
                    break;

                case ']': // Delete last
                    if (result.length() > 0) {
                        result.deleteCharAt(result.length() - 1);
                    }
                    i++;
                    break;

                case '@': // Purge character
                    if (i + 1 < rule.length()) {
                        char purge = rule.charAt(i + 1);
                        int j = 0;
                        while (j < result.length()) {
                            if (result.charAt(j) == purge) {
                                result.deleteCharAt(j);
                            } else {
                                j++;
                            }
                        }
                        i += 2;
                    } else {
                        i++;
                    }
                    break;

                case 's': // Replace X with Y
                    if (i + 2 < rule.length()) {
                        char from = rule.charAt(i + 1);
                        char to = rule.charAt(i + 2);
                        for (int j = 0; j < result.length(); j++) {
                            if (result.charAt(j) == from) {
                                result.setCharAt(j, to);
                            }
                        }
                        i += 3;
                    } else {
                        i++;
                    }
                    break;

                case 'T': // Toggle at position
                    if (i + 1 < rule.length()) {
                        int pos = Character.digit(rule.charAt(i + 1), 10);
                        if (pos >= 0 && pos < result.length()) {
                            char c = result.charAt(pos);
                            if (Character.isUpperCase(c)) {
                                result.setCharAt(pos, Character.toLowerCase(c));
                            } else if (Character.isLowerCase(c)) {
                                result.setCharAt(pos, Character.toUpperCase(c));
                            }
                        }
                        i += 2;
                    } else {
                        i++;
                    }
                    break;

                case '\'': // Truncate at position
                    if (i + 1 < rule.length()) {
                        int pos = Character.digit(rule.charAt(i + 1), 10);
                        if (pos >= 0 && pos < result.length()) {
                            result.setLength(pos);
                        }
                        i += 2;
                    } else {
                        i++;
                    }
                    break;

                case ' ': // Skip whitespace
                    i++;
                    break;

                default:
                    // Unknown command, skip
                    i++;
                    break;
            }
        }

        return result.toString();
    }

    @Override
    public AttackMode getAttackMode() {
        return AttackMode.RULE_BASED;
    }

    @Override
    public long getEstimatedTotal() {
        if (wordlistSize > 0 && rules.size() > 0) {
            // Check for overflow
            if (wordlistSize > Long.MAX_VALUE / rules.size()) {
                return Long.MAX_VALUE;
            }
            return wordlistSize * rules.size();
        }
        return -1;
    }

    /**
     * Returns a list of default rules for common password transformations.
     *
     * @return List of default rules
     */
    public static List<String> getDefaultRules() {
        List<String> rules = new ArrayList<>();
        rules.add(":"); // Passthrough
        rules.add("l"); // Lowercase
        rules.add("u"); // Uppercase
        rules.add("c"); // Capitalize
        rules.add("r"); // Reverse
        rules.add("d"); // Duplicate
        rules.add("$1"); // Append 1
        rules.add("$!"); // Append !
        rules.add("$@"); // Append @
        rules.add("$#"); // Append #
        rules.add("^1"); // Prepend 1
        rules.add("c$1"); // Capitalize + append 1
        rules.add("c$!"); // Capitalize + append !
        rules.add("c$123"); // Capitalize + append 123
        rules.add("l$1"); // Lowercase + append 1
        rules.add("u$1"); // Uppercase + append 1
        return rules;
    }
}
