// Copyright 2020, David Cattle, All rights reserved.

package com.clarity.connectionsFileParser;

import java.io.*;

public class ConnectionsParser {

    private static final long FIVE_MINUTES = 5 * 60 * 1000;
    private static final int INPUT_BUFFER_SIZE = 4096 * 5;
    private static final int OUTPUT_BUFFER_SIZE = INPUT_BUFFER_SIZE;

    private File connectionsFile;
    private long startTime;
    private long endTime;
    private String hostName;

    public ConnectionsParser(File connectionsFile,
                             long startTime, long endTime, String hostName) {

        this.connectionsFile = connectionsFile;
        this.startTime = startTime;
        this.endTime = endTime;
        this.hostName = hostName;
    }

    /**
     * prints a list of client names that connected to the given host during the given window.
     */
    public void connectedClients() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(connectionsFile),
                INPUT_BUFFER_SIZE);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out), OUTPUT_BUFFER_SIZE)) {
            boolean done = false;
            String line;
            String[] tokens;
            while (!done && (line = reader.readLine()) != null) {
                tokens = line.split(" ");
                long tsc = Long.parseLong(tokens[0]);
                String currentHost = tokens[2];
                if ((tsc >= startTime && tsc <= endTime) && currentHost.equals(hostName)) {
                    writer.write(tokens[1] + ' ');
                }
                // Optimisation, we can stop as soon as we reach this bound.
                if (tsc > endTime + FIVE_MINUTES) {
                    done = true;
                }
            }
        }
    }
}
