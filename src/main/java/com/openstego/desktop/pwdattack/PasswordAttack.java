/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.pwdattack;

import com.openstego.desktop.OpenStego;
import com.openstego.desktop.OpenStegoConfig;
import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.OpenStegoPlugin;

import java.io.File;
import java.util.List;

/**
 * Abstract base class for password attacks on stego files.
 * Supports various attack modes similar to hashcat.
 */
public abstract class PasswordAttack {

    /**
     * Attack mode constants (similar to hashcat modes)
     */
    public enum AttackMode {
        /**
         * Dictionary attack - try passwords from a wordlist
         */
        DICTIONARY(0, "Dictionary", "Straight wordlist attack"),

        /**
         * Combinator attack - combine words from two wordlists
         */
        COMBINATOR(1, "Combinator", "Combine words from two wordlists"),

        /**
         * Brute force with mask patterns
         */
        MASK(3, "Mask", "Brute force with mask patterns"),

        /**
         * Hybrid wordlist + mask (append mask to wordlist)
         */
        HYBRID_WORDLIST_MASK(6, "Hybrid (Wordlist+Mask)", "Wordlist with mask appended"),

        /**
         * Hybrid mask + wordlist (prepend mask to wordlist)
         */
        HYBRID_MASK_WORDLIST(7, "Hybrid (Mask+Wordlist)", "Mask with wordlist appended"),

        /**
         * Rule-based attack - apply transformation rules to wordlist
         */
        RULE_BASED(8, "Rule-Based", "Apply transformation rules to wordlist");

        private final int code;
        private final String name;
        private final String description;

        AttackMode(int code, String name, String description) {
            this.code = code;
            this.name = name;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public static AttackMode fromCode(int code) {
            for (AttackMode mode : values()) {
                if (mode.code == code) {
                    return mode;
                }
            }
            return DICTIONARY;
        }
    }

    protected final File stegoFile;
    protected final OpenStegoPlugin<?> plugin;
    protected PasswordAttackListener listener;
    protected volatile boolean cancelled = false;

    // Progress tracking
    protected long attemptCount = 0;
    protected long startTime;
    protected static final int PROGRESS_INTERVAL = 1000; // Report progress every N attempts

    /**
     * Creates a new password attack.
     *
     * @param stegoFile The stego file to attack
     * @param plugin    The steganography plugin to use
     */
    protected PasswordAttack(File stegoFile, OpenStegoPlugin<?> plugin) {
        this.stegoFile = stegoFile;
        this.plugin = plugin;
    }

    /**
     * Sets the listener for progress updates.
     *
     * @param listener The listener
     */
    public void setListener(PasswordAttackListener listener) {
        this.listener = listener;
    }

    /**
     * Cancels the attack.
     */
    public void cancel() {
        this.cancelled = true;
    }

    /**
     * @return true if the attack has been cancelled
     */
    public boolean isCancelled() {
        return cancelled || (listener != null && listener.isCancelled());
    }

    /**
     * Executes the password attack.
     *
     * @return The result of the attack
     */
    public abstract PasswordAttackResult execute();

    /**
     * @return The attack mode of this attack
     */
    public abstract AttackMode getAttackMode();

    /**
     * @return Estimated total number of passwords to try, or -1 if unknown
     */
    public abstract long getEstimatedTotal();

    /**
     * Attempts to extract data using the given password.
     *
     * @param password The password to try
     * @return Array containing [filename, data] if successful, null otherwise
     */
    protected List<?> tryPassword(String password) {
        try {
            plugin.resetConfig();
            OpenStegoConfig config = plugin.getConfig();
            config.setPassword(password);

            OpenStego stego = new OpenStego(plugin, config);
            return stego.extractData(stegoFile);
        } catch (OpenStegoException e) {
            // Invalid password or other error - continue trying
            return null;
        }
    }

    /**
     * Reports progress to the listener.
     *
     * @param currentPassword The password currently being tried
     */
    protected void reportProgress(String currentPassword) {
        if (listener != null && attemptCount % PROGRESS_INTERVAL == 0) {
            long elapsed = System.currentTimeMillis() - startTime;
            listener.onProgress(attemptCount, currentPassword, elapsed, getEstimatedTotal());
        }
    }

    /**
     * Creates the appropriate attack instance based on mode.
     *
     * @param mode      The attack mode
     * @param stegoFile The stego file to attack
     * @param plugin    The steganography plugin
     * @param wordlist1 Primary wordlist file (for dictionary-based attacks)
     * @param wordlist2 Secondary wordlist file (for combinator attack)
     * @param mask      Mask pattern (for mask-based attacks)
     * @param rules     Rules file (for rule-based attacks)
     * @param customCharsets Custom character sets for mask attack
     * @return The password attack instance
     */
    public static PasswordAttack create(AttackMode mode, File stegoFile, OpenStegoPlugin<?> plugin,
                                        File wordlist1, File wordlist2, String mask, File rules,
                                        String[] customCharsets) {
        switch (mode) {
            case DICTIONARY:
                return new DictionaryAttack(stegoFile, plugin, wordlist1);
            case COMBINATOR:
                return new CombinatorAttack(stegoFile, plugin, wordlist1, wordlist2);
            case MASK:
                return new MaskAttack(stegoFile, plugin, mask, customCharsets);
            case HYBRID_WORDLIST_MASK:
                return new HybridAttack(stegoFile, plugin, wordlist1, mask, true, customCharsets);
            case HYBRID_MASK_WORDLIST:
                return new HybridAttack(stegoFile, plugin, wordlist1, mask, false, customCharsets);
            case RULE_BASED:
                return new RuleBasedAttack(stegoFile, plugin, wordlist1, rules);
            default:
                return new DictionaryAttack(stegoFile, plugin, wordlist1);
        }
    }
}
