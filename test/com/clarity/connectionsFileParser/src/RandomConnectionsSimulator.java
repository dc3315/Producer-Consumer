// Copyright 2020, David Cattle, All rights reserved.

package com.clarity.connectionsFileParser.src;

import java.io.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is to generate files that simulate a log file but also
 * can potentially be used for integration testing as well, although I believe
 * it is fair for integration testing to be out of scope of this exercise.
 */
public class RandomConnectionsSimulator implements Runnable {

    private LinkedList<String> names;
    private static final int PERCENTAGE_OUT_OF_ORDER = 5;
    private File connectionsOutputFile;
    private long currentTime = 0;
    private AtomicBoolean run = new AtomicBoolean(true);
    private BufferedWriter writer;
    private long windowLo;
    private long windowHi;

    // WritesPerHour cannot be greater than 1M
    // For the purpose of this exercise this is ok
    // If this value needs to be higher, use nano seconds instead of milli in
    // the computations and in the sleep system call.
    RandomConnectionsSimulator(long writesPerHour, File nameFile,
                               File connectionsOutputFile) throws IOException {
        assert(writesPerHour <= (long) 1e6);
        this.connectionsOutputFile = connectionsOutputFile;
        double avgTime = 3.6e6 / writesPerHour;
        long half = (long) avgTime / 2;
        windowLo = (long) avgTime - half;
        windowHi = (long) avgTime + half;

        // Read in list of random names.
        names = new LinkedList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(nameFile))) {
            String line;
            String[] tokens;
            while ((line = reader.readLine()) != null) {
                tokens = line.split(" ");
                names.addFirst(tokens[0]);
            }
        }
        writer =
                new BufferedWriter(new OutputStreamWriter(new
                        FileOutputStream(connectionsOutputFile)));
    }

    /*
    Generate two random host names.
     */
    String[] getTwoRandomClients() {
        Collections.shuffle(names);
        return new String[]{names.get(0), names.get(1)};
    }


    /* To be called externally to gracefully terminate thread. */
    public void terminate() {
        run.set(false);
    }

    /* Uniform random distribution centered around average. */
    long getRandomSleepTime() {
        return ThreadLocalRandom.current().nextLong(windowLo,
                windowHi + 1);
    }

    /* Returns true PERCENTAGE_OUT_OF_ORDER% of the time. */
    boolean outOfOrder() {
        return ThreadLocalRandom.current().nextInt(0, 100) < PERCENTAGE_OUT_OF_ORDER;
    }

    /* Random time in [0,5]m interval */
    long randomTimeIntervalInFiveMinuteWindow() {
        return ThreadLocalRandom.current().nextLong(0,
                TimeUnit.MINUTES.toMillis(5));
    }

    /* Generates and write random connections with some out of order by a max
     of 5m. */
    @Override
    public void run() {
        String[] twoRandomNames;
        while (run.get()) {
            try {
                Thread.sleep(getRandomSleepTime());
                twoRandomNames = getTwoRandomClients();
                long tsc = (outOfOrder()) ?
                        (System.currentTimeMillis() - randomTimeIntervalInFiveMinuteWindow()) : System.currentTimeMillis();
                writer.write((tsc + " " + twoRandomNames[0] +
                        " " + twoRandomNames[1] + "\n"));
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
