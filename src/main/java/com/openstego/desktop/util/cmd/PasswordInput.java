/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.util.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.openstego.desktop.OpenStegoException;

/**
 * Utility class to handle console based password input
 */
public class PasswordInput {
    /**
     * Constructor is private so that this class is not instantiated
     */
    private PasswordInput() {
    }

    /**
     * Method to read password from the console
     *
     * @param prompt Prompt for the password input
     * @return The password as entered by the user
     * @throws OpenStegoException
     */
    public static String readPassword(String prompt) throws OpenStegoException {
        String password = "";
        EraserThread et = null;
        BufferedReader in = null;
        Thread mask = null;

        et = new EraserThread(prompt);
        mask = new Thread(et);
        mask.start();

        in = new BufferedReader(new InputStreamReader(System.in));
        try {
            password = in.readLine();
        } catch (IOException ioEx) {
            throw new OpenStegoException(ioEx);
        }

        // Stop masking
        et.stopMasking();
        System.out.println();

        return password;
    }

    /**
     * Thread to keep rewriting the input characters with blank space
     */
    static class EraserThread implements Runnable {
        /**
         * Flag for stop condition
         */
        private boolean stop = true;

        /**
         * Constructor
         *
         * @param prompt Prompt for the password input
         */
        public EraserThread(String prompt) {
            System.out.print(prompt);
        }

        /**
         * Implementation of <code>run</code> method
         */
        @Override
        public void run() {
            while (this.stop) {
                System.out.print("\b ");
            }
        }

        /**
         * Instruct the thread to stop masking
         */
        public void stopMasking() {
            this.stop = false;
        }
    }
}
