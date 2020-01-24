 // Copyright 2020, David Cattle, All rights reserved.

package com.clarity.connectionsFileParser;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class TailingConnectionsParser implements Runnable {

    private static final long DEFAULT_BLOCKING_TIME_MS = 2000;
    private long downTime = DEFAULT_BLOCKING_TIME_MS;
    private int MAX_RETRIES_AT_TAIL = 5;
    private AtomicBoolean run = new AtomicBoolean(true);

    private long filePointer = 0;
    private long interval;
    private long now = 0;
    private long lastDumpTime = 0;
    private LinkedList<String> hostConnectedTo = new LinkedList<>();
    private LinkedList<String> connectedToHost = new LinkedList<>();
    private HashMap<String, Integer> connectionsPerClient =
            new HashMap<>();

    private File connectionsFile;
    private String hostName;


    public TailingConnectionsParser(long downTime, long interval,
                                    File connectionsFile,
                                    String hostName) {
        this.downTime = downTime;
        this.interval = interval;
        this.hostName = hostName;
        this.connectionsFile = connectionsFile;
    }

    public TailingConnectionsParser(File connectionsFile,
                                    String hostName) {
        this(DEFAULT_BLOCKING_TIME_MS, TimeUnit.HOURS.toMillis(1),
                connectionsFile, hostName);
    }


    /**
     * Runs forever, similar implementation to tail -f
     * in bash.
     *
     * Comments:
     * There is a problem here that is that the timestamps can be out of order
     * by up to five minutes.
     *
     * I believe there are two main options here. I believe in a both
     * can are valid and acceptable but I cannot make this call efficiently
     * without further information about the context.
     *
     *      1 - Ignore out of order connections past the one hour boundary
     *      (if OOO writes probability is low) and have the tailer run
     *      'relatively' in sync with producer publishing very close to an
     *      hour (although drift is inevitable) although the reported values
     *      are not 100% accurate.
     *
     *      2 - Have this consumer accommodate for late writes in a 5 minute
     *      window at the end of each hour interval using supplementary data
     *      structures, this is doable (by maintaining more data structures:
     *      HT1,HT2,LL1A,LL2A (HT -> Hashtable, LL -> linkedlist))
     *
     *      Example:
     *          • 0-1h:
     *              • modify HT1,LLA1,LLB1
     *          • 1h-1h+5:
     *              • keep on updating HT1,LLA1,LLB1 with late writes from 0-1h
     *              • modify HT2,LLA2,LLB2 with writes > 1h
     *          • 1h+5:
     *              • dump the HT1,LLA1,LLB1 data, and swap data structure
     *                  pointers: HT2,LLA2,LLB2 become HT1,LLA1,LLB1  and
     *                  vice versa.
     *
     *      The problem with this is that every hour that goes by, the tailer
     *      will run behind by 5m, and the drift will become large very quickly.
     *      However, this option is better is strict correctness is necessary.
     *
     *      I chose to implement option 1, although could have implemented 2
     *      if necessary.
     */
    @Override
    public void run() {
        try {
            /* Wait MAX_RETRIES_AT_TAIL * downTime MS
               to see if new content has arrived and otherwise assume
               producer has finished.  */
            int numTries = MAX_RETRIES_AT_TAIL;
            while (run.get()) {
                // Check if we need to log.
                if ((now - lastDumpTime) > interval) {
                    dumpResults(lastDumpTime, now);
                    lastDumpTime = now;
                    renewDataStructures();
                }
                /* Important for this thread to sleep an amount of time on
                * every loop instead of busy waiting, otherwise we would
                * WASTE CPU TIME. This downTime needs to be set accordingly. */
                Thread.sleep(downTime);
                long len = connectionsFile.length();
                if (len < filePointer) {
                    // Log was deleted or corrupted.
                    System.err.println("Log file has been deleted or reset. " +
                            "Restarting");
                    filePointer = len;
                } else if (len > filePointer) {
                    // Producer must have added content to log.
                    // Read a new chunk of data.
                    numTries = MAX_RETRIES_AT_TAIL;
                    filePointer = processFileChunk();
                } else {
                    if (--numTries == 0) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Fatal");
            e.printStackTrace();
        }
    }

    /**
     * Walks the hashtable to identify client who created the largest number
     * of connections.
     * NB: There may be more than one client that has this maximum, this
     * function can easily be modified to reflect that if necessary.
     * @return
     */
    private String findClientWhoCreatedMostConnections() {
        String clientWhoGeneratedMostConnections = null;
        int maxNumberOfConnections = 0;
        for (Map.Entry<String, Integer> entry :
                connectionsPerClient.entrySet()) {
            String client = entry.getKey();
            Integer numberOfConnections = entry.getValue();
            if (clientWhoGeneratedMostConnections == null) {
                clientWhoGeneratedMostConnections = client;
                maxNumberOfConnections = numberOfConnections;
            }
            if (numberOfConnections > maxNumberOfConnections) {
                maxNumberOfConnections = numberOfConnections;
                clientWhoGeneratedMostConnections = client;
            }
        }
        return (clientWhoGeneratedMostConnections + "(" + maxNumberOfConnections + ")");
    }

    /**
     * Reset the data structures. The previous objects will be eligible for
     * Garbage Collection and cleaned up automatically by GC.
     */
    private void renewDataStructures() {
        connectionsPerClient = new HashMap<>();
        connectedToHost = new LinkedList<>();
        hostConnectedTo = new LinkedList<>();
    }

    /**
     * Dump stats of the last interval window.
     * @param lastDumpTime
     * @param now
     */
    private void dumpResults(long lastDumpTime, long now) {
        System.out.println("Results for hour " + lastDumpTime + " to " + now +
                ":");
        System.out.println("\tMost connections generated by: " + findClientWhoCreatedMostConnections());
        System.out.println("\tClients who connected to: " + hostName + ":");
        for (String s : connectedToHost) {
            System.out.println("\t\t- " + s);
        }
        System.out.println("\tHosts who " + hostName + " connected to:");
        for (String s : hostConnectedTo) {
            System.out.println("\t\t- " + s);
        }
        System.out.println();
    }

    /**
     * Updates the data structures as file is processed.
     * @return
     * @throws IOException
     */
    private long processFileChunk() throws IOException {
        RandomAccessFile connectionsFileHandle =
                new RandomAccessFile(connectionsFile, "r");
        connectionsFileHandle.seek(filePointer);
        String line = null;
        while ((line = connectionsFileHandle.readLine()) != null) {
            String[] tokens = line.split(" ");
            long tsc = Long.parseLong(tokens[0]);
            String client = tokens[1];
            String host = tokens[2];

            /* Update data structures */

            if (host.equals(hostName)) {
                // a client connected to hostname
                connectedToHost.addFirst(client);
            }
            if (client.equals(hostName)) {
                // hostname connected to host
                hostConnectedTo.addFirst(host);
            }

            // Keep track of connections per client.
            int count = connectionsPerClient.getOrDefault(client, 0);
            connectionsPerClient.put(client, count + 1);

            // First round
            if (now == 0) {
                lastDumpTime = tsc;
            }
            now = tsc;

            /* Check if we have overrun the interval */
            // We need to do this here because we have already processed the
            // line.
            if (now - lastDumpTime > interval) {
                break;
            }
        }

        /* Update file pointers */
        long pointer = connectionsFileHandle.getFilePointer();
        connectionsFileHandle.close();
        return pointer;
    }


    /* To be called externally to gracefully terminate thread. */
    public void terminate() {
        run.set(false);
    }
}
