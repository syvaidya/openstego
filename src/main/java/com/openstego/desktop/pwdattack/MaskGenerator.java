/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.pwdattack;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Generator for mask-based password attacks (similar to hashcat mask attack).
 * Supports the following character sets:
 * <ul>
 *   <li>?l - lowercase letters (a-z)</li>
 *   <li>?u - uppercase letters (A-Z)</li>
 *   <li>?d - digits (0-9)</li>
 *   <li>?s - special characters (!@#$%^&*()_+-=[]{}|;':\",./<>?`~)</li>
 *   <li>?a - all printable ASCII (combination of l, u, d, s)</li>
 *   <li>?b - byte values (0x00-0xff) - use with caution</li>
 *   <li>?1-?4 - custom character sets defined by user</li>
 *   <li>Any other character is treated as literal</li>
 * </ul>
 */
public class MaskGenerator implements Iterable<String> {

    public static final String CHARSET_LOWER = "abcdefghijklmnopqrstuvwxyz";
    public static final String CHARSET_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String CHARSET_DIGITS = "0123456789";
    public static final String CHARSET_SPECIAL = "!@#$%^&*()_+-=[]{}|;':\",./<>?`~ ";
    public static final String CHARSET_ALL = CHARSET_LOWER + CHARSET_UPPER + CHARSET_DIGITS + CHARSET_SPECIAL;

    private final String mask;
    private final String[] customCharsets;
    private final String[] charsets;
    private final long totalCombinations;

    /**
     * Creates a mask generator with the given mask pattern.
     *
     * @param mask The mask pattern
     */
    public MaskGenerator(String mask) {
        this(mask, new String[4]);
    }

    /**
     * Creates a mask generator with the given mask pattern and custom charsets.
     *
     * @param mask           The mask pattern
     * @param customCharsets Array of 4 custom charsets (?1 through ?4)
     */
    public MaskGenerator(String mask, String[] customCharsets) {
        this.mask = mask;
        this.customCharsets = customCharsets != null ? customCharsets : new String[4];
        this.charsets = parseMask();
        this.totalCombinations = calculateTotalCombinations();
    }

    /**
     * Parses the mask and returns an array of character sets for each position.
     */
    private String[] parseMask() {
        java.util.List<String> result = new java.util.ArrayList<>();
        int i = 0;
        while (i < mask.length()) {
            if (mask.charAt(i) == '?' && i + 1 < mask.length()) {
                char type = mask.charAt(i + 1);
                result.add(getCharsetForType(type));
                i += 2;
            } else {
                // Literal character
                result.add(String.valueOf(mask.charAt(i)));
                i++;
            }
        }
        return result.toArray(new String[0]);
    }

    /**
     * Gets the character set for a given type character.
     */
    private String getCharsetForType(char type) {
        switch (type) {
            case 'l':
                return CHARSET_LOWER;
            case 'u':
                return CHARSET_UPPER;
            case 'd':
                return CHARSET_DIGITS;
            case 's':
                return CHARSET_SPECIAL;
            case 'a':
                return CHARSET_ALL;
            case 'b':
                // Full byte range - use with extreme caution
                StringBuilder sb = new StringBuilder(256);
                for (int i = 0; i < 256; i++) {
                    sb.append((char) i);
                }
                return sb.toString();
            case '1':
                return customCharsets[0] != null ? customCharsets[0] : CHARSET_LOWER;
            case '2':
                return customCharsets[1] != null ? customCharsets[1] : CHARSET_UPPER;
            case '3':
                return customCharsets[2] != null ? customCharsets[2] : CHARSET_DIGITS;
            case '4':
                return customCharsets[3] != null ? customCharsets[3] : CHARSET_SPECIAL;
            case '?':
                return "?";
            default:
                return String.valueOf(type);
        }
    }

    /**
     * Calculates the total number of possible combinations.
     */
    private long calculateTotalCombinations() {
        if (charsets.length == 0) {
            return 0;
        }
        long total = 1;
        for (String charset : charsets) {
            // Check for overflow
            if (total > Long.MAX_VALUE / charset.length()) {
                return Long.MAX_VALUE;
            }
            total *= charset.length();
        }
        return total;
    }

    /**
     * @return Total number of combinations this mask can generate
     */
    public long getTotalCombinations() {
        return totalCombinations;
    }

    /**
     * @return The number of positions in the generated passwords
     */
    public int getPasswordLength() {
        return charsets.length;
    }

    /**
     * Gets a specific password by index.
     *
     * @param index The index of the password to generate
     * @return The password at the given index
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public String getPasswordAtIndex(long index) {
        if (index < 0 || index >= totalCombinations) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + totalCombinations);
        }

        char[] password = new char[charsets.length];
        long remaining = index;

        for (int i = charsets.length - 1; i >= 0; i--) {
            int charsetSize = charsets[i].length();
            int charIndex = (int) (remaining % charsetSize);
            password[i] = charsets[i].charAt(charIndex);
            remaining /= charsetSize;
        }

        return new String(password);
    }

    @Override
    public Iterator<String> iterator() {
        return new MaskIterator();
    }

    /**
     * Iterator implementation for mask generator.
     */
    private class MaskIterator implements Iterator<String> {
        private final int[] indices;
        private boolean hasNext;

        MaskIterator() {
            indices = new int[charsets.length];
            hasNext = charsets.length > 0;
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public String next() {
            if (!hasNext) {
                throw new NoSuchElementException();
            }

            // Build current password
            char[] password = new char[charsets.length];
            for (int i = 0; i < charsets.length; i++) {
                password[i] = charsets[i].charAt(indices[i]);
            }

            // Advance to next combination
            int pos = charsets.length - 1;
            while (pos >= 0) {
                indices[pos]++;
                if (indices[pos] < charsets[pos].length()) {
                    break;
                }
                indices[pos] = 0;
                pos--;
            }
            if (pos < 0) {
                hasNext = false;
            }

            return new String(password);
        }
    }

    /**
     * Parses a mask pattern and returns a description of what it represents.
     *
     * @param mask The mask pattern
     * @return Human-readable description
     */
    public static String describeMask(String mask) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < mask.length()) {
            if (mask.charAt(i) == '?' && i + 1 < mask.length()) {
                char type = mask.charAt(i + 1);
                switch (type) {
                    case 'l':
                        sb.append("[a-z]");
                        break;
                    case 'u':
                        sb.append("[A-Z]");
                        break;
                    case 'd':
                        sb.append("[0-9]");
                        break;
                    case 's':
                        sb.append("[special]");
                        break;
                    case 'a':
                        sb.append("[all]");
                        break;
                    case 'b':
                        sb.append("[byte]");
                        break;
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                        sb.append("[custom").append(type).append("]");
                        break;
                    case '?':
                        sb.append("?");
                        break;
                    default:
                        sb.append(type);
                }
                i += 2;
            } else {
                sb.append(mask.charAt(i));
                i++;
            }
        }
        return sb.toString();
    }
}
