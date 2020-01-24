// Copyright 2020, David Cattle, All rights reserved.

package com.clarity.connectionsFileParser;

import java.io.*;

public class Main {

    public static final String usage = """
            usage: java Main <filename> -f <host_name>
                   java Main <filename> <start_time> <end_time> <host_name>
            """;

    public static final int NON_FOLLOW_ARGS = 4;
    public static final int FOLLOW_ARGS = 3;

    public static void printUsage() {
        System.err.println(usage);
    }

    /**
     * Main contains the boilerplate code to do argument parsing and invoke
     * correct object.
     * @param args
     * @throws FileNotFoundException
     * @throws IllegalArgumentException
     */
    public static void main(String[] args) throws FileNotFoundException, IllegalArgumentException {
        if ((args.length != NON_FOLLOW_ARGS) && (args.length != FOLLOW_ARGS)) {
            printUsage();
            throw new IllegalArgumentException();
        }

        String fileName = args[0];
        File connectionsFile = new File(fileName);

        if (!connectionsFile.exists()) {
            printUsage();
            throw new FileNotFoundException(fileName + ": " + "no such file");
        }

        boolean follow = false;
        String hostName;
        long startTime = 0;
        long endTime = 0;

        // We are following a file
        if (args[1].equals("-f")) {
            follow = true;
            if (args.length != FOLLOW_ARGS) {
                printUsage();
                throw new IllegalArgumentException();
            }
            hostName = args[2];
        } else {
            if (args.length != NON_FOLLOW_ARGS) {
                printUsage();
                throw new IllegalArgumentException();
            }
            try {
                startTime = Long.parseLong(args[1]);
                endTime = Long.parseLong(args[2]);
            } catch (NumberFormatException e) {
                printUsage();
                throw new NumberFormatException(args[1] + " or " + args[2] +
                        " is not a " +
                        "number");
            }
            hostName = args[3];
        }

        /*
         * Separation of concerns/functionality.
         * I created two classes because I think it is far _clearer_ to
         * separate the two classes as their respective objectives are far
         * different than one may expect and merging them would result in very
         * very bloated / convoluted, and unreadable code.
         *
         * I believe that BufferedReader is more efficient at reading
         * sequentially and randomAccessFile better if the file is being
         * modified at same time.
         */
        if (!follow) {
            ConnectionsParser cp =
                    new ConnectionsParser(connectionsFile, startTime,
                            endTime, hostName);
            try {
                cp.connectedClients();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            TailingConnectionsParser tcp =
                    new TailingConnectionsParser(connectionsFile, hostName);
            new Thread(tcp).start(); // runs forever until terminated or
            // until waited too long.
        }
    }
}
